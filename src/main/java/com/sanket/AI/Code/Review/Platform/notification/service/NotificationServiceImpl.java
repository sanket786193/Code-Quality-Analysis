package com.sanket.AI.Code.Review.Platform.notification.service;

import com.sanket.AI.Code.Review.Platform.exception.ResourceNotFoundException;
import com.sanket.AI.Code.Review.Platform.exception.UnauthorizedAccessException;
import com.sanket.AI.Code.Review.Platform.exception.UserNotFoundException;
import com.sanket.AI.Code.Review.Platform.notification.dto.NotificationCountDto;
import com.sanket.AI.Code.Review.Platform.notification.dto.NotificationResponse;
import com.sanket.AI.Code.Review.Platform.notification.dto.SendNotificationRequest;
import com.sanket.AI.Code.Review.Platform.notification.email.EmailService;
import com.sanket.AI.Code.Review.Platform.notification.entity.*;
import com.sanket.AI.Code.Review.Platform.notification.exception.NotificationNotFoundException;
import com.sanket.AI.Code.Review.Platform.notification.exception.NotificationSendException;
import com.sanket.AI.Code.Review.Platform.notification.mapper.NotificationMapper;
import com.sanket.AI.Code.Review.Platform.notification.rabbitmq.message.NotificationEventMessage;
import com.sanket.AI.Code.Review.Platform.notification.rabbitmq.producer.NotificationProducer;
import com.sanket.AI.Code.Review.Platform.notification.repository.NotificationEventRepository;
import com.sanket.AI.Code.Review.Platform.notification.repository.NotificationRepository;
import com.sanket.AI.Code.Review.Platform.project.entity.Project;
import com.sanket.AI.Code.Review.Platform.project.repository.ProjectRepository;
import com.sanket.AI.Code.Review.Platform.security.UserPrincipal;
import com.sanket.AI.Code.Review.Platform.user.entity.User;
import com.sanket.AI.Code.Review.Platform.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final NotificationEventRepository eventRepository;
    private final NotificationMapper notificationMapper;
    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;
    private final EmailService emailService;
    private final NotificationProducer notificationProducer;

    @Override
    public NotificationResponse sendNotification(SendNotificationRequest request, UserPrincipal currentUser) {
        log.info("Sending notification of type: {} to recipient ID: {}", request.getType(), request.getRecipientId());

        User recipient = userRepository.findById(request.getRecipientId())
                .orElseThrow(() -> new UserNotFoundException("Recipient user not found with ID: " + request.getRecipientId()));

        User sender = null;
        if (currentUser != null) {
            sender = userRepository.findById(currentUser.getId()).orElse(null);
        }

        Project project = null;
        if (request.getProjectId() != null) {
            project = projectRepository.findById(request.getProjectId()).orElse(null);
        }

        try {
            Notification notification = notificationMapper.toEntity(request, recipient, sender, project);
            Notification savedNotification = notificationRepository.save(notification);

            // Create Event Audit Log
            String eventId = "EVT-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
            NotificationEvent event = NotificationEvent.builder()
                    .eventId(eventId)
                    .type(request.getType())
                    .recipientUserId(recipient.getId())
                    .senderUserId(sender != null ? sender.getId() : null)
                    .projectId(project != null ? project.getId() : null)
                    .reportReference(request.getReportReference())
                    .title(request.getTitle())
                    .message(request.getMessage())
                    .severity(request.getSeverity())
                    .sentViaEmail(Boolean.TRUE.equals(request.getSendEmail()))
                    .build();
            eventRepository.save(event);

            // Optional RabbitMQ / Event Bus dispatch
            NotificationEventMessage message = NotificationEventMessage.builder()
                    .eventId(eventId)
                    .recipientUserId(recipient.getId())
                    .recipientEmail(recipient.getEmail())
                    .senderUserId(sender != null ? sender.getId() : null)
                    .projectId(project != null ? project.getId() : null)
                    .reportReference(request.getReportReference())
                    .type(request.getType())
                    .severity(request.getSeverity())
                    .title(request.getTitle())
                    .message(request.getMessage())
                    .actionUrl(request.getActionUrl())
                    .sendEmail(Boolean.TRUE.equals(request.getSendEmail()))
                    .timestamp(LocalDateTime.now())
                    .build();
            notificationProducer.publishNotificationEvent(message);

            // Optional direct email dispatch
            if (Boolean.TRUE.equals(request.getSendEmail()) && recipient.getEmail() != null) {
                emailService.sendNotificationEmail(recipient.getEmail(), request.getTitle(), request.getMessage());
            }

            return notificationMapper.toResponse(savedNotification);
        } catch (Exception ex) {
            log.error("Failed to persist and send notification: {}", ex.getMessage(), ex);
            throw new NotificationSendException("Failed to dispatch notification: " + ex.getMessage(), ex);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Page<NotificationResponse> getNotifications(
            NotificationStatus status,
            NotificationType type,
            Pageable pageable,
            UserPrincipal currentUser
    ) {
        Long userId = currentUser.getId();

        Page<Notification> page;
        if (status != null) {
            page = notificationRepository.findByRecipientIdAndStatusOrderByCreatedAtDesc(userId, status, pageable);
        } else if (type != null) {
            page = notificationRepository.findByRecipientIdAndTypeOrderByCreatedAtDesc(userId, type, pageable);
        } else {
            page = notificationRepository.findByRecipientIdOrderByCreatedAtDesc(userId, pageable);
        }

        return page.map(notificationMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public NotificationResponse getNotificationById(Long id, UserPrincipal currentUser) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new NotificationNotFoundException("Notification not found with ID: " + id));

        validateOwnership(notification, currentUser);
        return notificationMapper.toResponse(notification);
    }

    @Override
    public NotificationResponse markAsRead(Long id, UserPrincipal currentUser) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new NotificationNotFoundException("Notification not found with ID: " + id));

        validateOwnership(notification, currentUser);

        if (notification.getStatus() == NotificationStatus.UNREAD) {
            notification.setStatus(NotificationStatus.READ);
            notification.setReadAt(LocalDateTime.now());
            notification = notificationRepository.save(notification);
        }

        return notificationMapper.toResponse(notification);
    }

    @Override
    public int markAllAsRead(UserPrincipal currentUser) {
        return notificationRepository.markAllAsReadForUser(
                currentUser.getId(),
                NotificationStatus.READ,
                LocalDateTime.now()
        );
    }

    @Override
    public void deleteNotification(Long id, UserPrincipal currentUser) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new NotificationNotFoundException("Notification not found with ID: " + id));

        validateOwnership(notification, currentUser);
        notificationRepository.delete(notification);
    }

    @Override
    public void clearAllNotifications(UserPrincipal currentUser) {
        notificationRepository.deleteAllByRecipientId(currentUser.getId());
    }

    @Override
    @Transactional(readOnly = true)
    public NotificationCountDto getNotificationCounts(UserPrincipal currentUser) {
        Long userId = currentUser.getId();
        long unread = notificationRepository.countByRecipientIdAndStatus(userId, NotificationStatus.UNREAD);
        long total = notificationRepository.countByRecipientId(userId);
        long critical = notificationRepository.countByRecipientIdAndSeverityAndStatus(
                userId,
                NotificationSeverity.CRITICAL,
                NotificationStatus.UNREAD
        );

        return NotificationCountDto.builder()
                .unreadCount(unread)
                .totalCount(total)
                .criticalCount(critical)
                .build();
    }

    @Override
    public void notifyReviewCompleted(
            String reportReference,
            Long projectId,
            String authorUsername,
            String prTitle,
            Double score,
            int criticalCount,
            int highCount
    ) {
        userRepository.findByUsername(authorUsername).ifPresent(developer -> {
            NotificationSeverity severity = criticalCount > 0 ? NotificationSeverity.CRITICAL :
                    (highCount > 0 ? NotificationSeverity.WARNING : NotificationSeverity.SUCCESS);

            String title = "AI Review Completed: " + prTitle;
            String message = String.format("Automated review completed with overall quality score %.0f/100. " +
                            "Found %d critical and %d high findings. Report reference: %s.",
                    score, criticalCount, highCount, reportReference);

            SendNotificationRequest request = SendNotificationRequest.builder()
                    .recipientId(developer.getId())
                    .type(criticalCount > 0 ? NotificationType.ISSUES_FOUND : NotificationType.REVIEW_COMPLETED)
                    .title(title)
                    .message(message)
                    .severity(severity)
                    .projectId(projectId)
                    .reportReference(reportReference)
                    .actionUrl("/reports/" + reportReference)
                    .sendEmail(criticalCount > 0)
                    .build();

            sendNotification(request, null);
        });
    }

    @Override
    public void notifyDecisionMade(
            String reportReference,
            Long projectId,
            String authorUsername,
            String decision,
            String comment,
            String leadUsername
    ) {
        userRepository.findByUsername(authorUsername).ifPresent(developer -> {
            NotificationType type;
            NotificationSeverity severity;
            String title;

            if ("APPROVED".equalsIgnoreCase(decision)) {
                type = NotificationType.PR_APPROVED;
                severity = NotificationSeverity.SUCCESS;
                title = "Pull Request Approved: " + reportReference;
            } else if ("CHANGES_REQUESTED".equalsIgnoreCase(decision)) {
                type = NotificationType.CHANGES_REQUESTED;
                severity = NotificationSeverity.WARNING;
                title = "Changes Requested: " + reportReference;
            } else {
                type = NotificationType.PR_REJECTED;
                severity = NotificationSeverity.CRITICAL;
                title = "Pull Request Rejected: " + reportReference;
            }

            String message = String.format("Team Lead @%s recorded decision [%s]. Note: %s",
                    leadUsername, decision, (comment != null && !comment.isBlank()) ? comment : "No comment provided.");

            SendNotificationRequest request = SendNotificationRequest.builder()
                    .recipientId(developer.getId())
                    .type(type)
                    .title(title)
                    .message(message)
                    .severity(severity)
                    .projectId(projectId)
                    .reportReference(reportReference)
                    .actionUrl("/reports/" + reportReference)
                    .sendEmail(true)
                    .build();

            sendNotification(request, null);
        });
    }

    @Override
    public void notifyDecisionPending(String reportReference, Long projectId, String prTitle, String authorUsername) {
        // Query users with ROLE_TEAM_LEAD or ROLE_ADMIN associated with this project or system
        userRepository.findAll().stream()
                .filter(u -> u.getRoles().stream().anyMatch(r ->
                        "ROLE_TEAM_LEAD".equals(r.getRoleName()) || "ROLE_ADMIN".equals(r.getRoleName())))
                .forEach(teamLead -> {
                    String title = "Governance Gate Pending: " + prTitle;
                    String message = String.format("AI Review for PR by @%s is ready for team lead approval. Reference: %s",
                            authorUsername, reportReference);

                    SendNotificationRequest request = SendNotificationRequest.builder()
                            .recipientId(teamLead.getId())
                            .type(NotificationType.DECISION_PENDING)
                            .title(title)
                            .message(message)
                            .severity(NotificationSeverity.INFO)
                            .projectId(projectId)
                            .reportReference(reportReference)
                            .actionUrl("/reports/" + reportReference)
                            .sendEmail(false)
                            .build();

                    try {
                        sendNotification(request, null);
                    } catch (Exception e) {
                        log.warn("Failed to dispatch pending decision notification to user {}: {}", teamLead.getUsername(), e.getMessage());
                    }
                });
    }

    private void validateOwnership(Notification notification, UserPrincipal currentUser) {
        boolean isAdmin = currentUser.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        if (!isAdmin && !notification.getRecipient().getId().equals(currentUser.getId())) {
            throw new UnauthorizedAccessException("You are not authorized to access or modify this notification");
        }
    }
}

package com.sanket.AI.Code.Review.Platform.notification.email;

import com.sanket.AI.Code.Review.Platform.notification.entity.NotificationTemplate;
import com.sanket.AI.Code.Review.Platform.notification.repository.NotificationTemplateRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailServiceImpl implements EmailService {

    private final NotificationTemplateRepository templateRepository;

    @Override
    public void sendNotificationEmail(String toEmail, String subject, String content) {
        if (toEmail == null || toEmail.isBlank()) {
            log.warn("Skipping email notification: Recipient email is empty");
            return;
        }

        log.info("================ EMAIL DISPATCH ================");
        log.info("To: {}", toEmail);
        log.info("Subject: {}", subject);
        log.info("Content Preview: {}", content != null && content.length() > 100 ? content.substring(0, 100) + "..." : content);
        log.info("================================================");
    }

    @Override
    public void sendTemplatedEmail(String toEmail, String templateCode, Map<String, String> variables) {
        NotificationTemplate template = templateRepository.findByTemplateCode(templateCode)
                .orElse(null);

        if (template == null) {
            log.warn("Notification template [{}] not found for email dispatch to {}", templateCode, toEmail);
            return;
        }

        String subject = substitutePlaceholders(template.getEmailSubjectTemplate(), variables);
        String body = substitutePlaceholders(template.getBodyTemplate(), variables);

        sendNotificationEmail(toEmail, subject, body);
    }

    private String substitutePlaceholders(String text, Map<String, String> variables) {
        if (text == null || variables == null) return text;
        String result = text;
        for (Map.Entry<String, String> entry : variables.entrySet()) {
            result = result.replace("{{" + entry.getKey() + "}}", entry.getValue() != null ? entry.getValue() : "");
        }
        return result;
    }
}

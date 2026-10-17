package com.sanket.AI.Code.Review.Platform.review.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sanket.AI.Code.Review.Platform.exception.*;
import com.sanket.AI.Code.Review.Platform.project.entity.Project;
import com.sanket.AI.Code.Review.Platform.project.entity.RepositoryMapping;
import com.sanket.AI.Code.Review.Platform.project.repository.ProjectRepository;
import com.sanket.AI.Code.Review.Platform.project.repository.RepositoryMappingRepository;
import com.sanket.AI.Code.Review.Platform.review.dto.request.CreateManualReviewRequest;
import com.sanket.AI.Code.Review.Platform.review.dto.response.ReviewInsightsResponse;
import com.sanket.AI.Code.Review.Platform.review.dto.response.ReviewJobDetailResponse;
import com.sanket.AI.Code.Review.Platform.review.dto.response.ReviewJobResponse;
import com.sanket.AI.Code.Review.Platform.review.entity.*;
import com.sanket.AI.Code.Review.Platform.review.mapper.ReviewMapper;
import com.sanket.AI.Code.Review.Platform.review.rabbitmq.message.ReviewJobMessage;
import com.sanket.AI.Code.Review.Platform.review.rabbitmq.producer.ReviewJobProducer;
import com.sanket.AI.Code.Review.Platform.review.repository.ReviewFindingRepository;
import com.sanket.AI.Code.Review.Platform.review.repository.ReviewJobRepository;
import com.sanket.AI.Code.Review.Platform.review.repository.ReviewSummaryRepository;
import com.sanket.AI.Code.Review.Platform.review.service.ReviewJobService;
import com.sanket.AI.Code.Review.Platform.review.sonar.service.SonarQubeAnalysisService;
import com.sanket.AI.Code.Review.Platform.ruleengine.entity.RuleSeverity;
import com.sanket.AI.Code.Review.Platform.ruleengine.entity.RuleViolation;
import com.sanket.AI.Code.Review.Platform.ruleengine.service.RuleEngineService;
import com.sanket.AI.Code.Review.Platform.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReviewJobServiceImpl implements ReviewJobService {

    private final ReviewJobRepository reviewJobRepository;
    private final ReviewFindingRepository reviewFindingRepository;
    private final ReviewSummaryRepository reviewSummaryRepository;
    private final ProjectRepository projectRepository;
    private final RepositoryMappingRepository repositoryMappingRepository;
    private final SonarQubeAnalysisService sonarQubeAnalysisService;
    private final RuleEngineService ruleEngineService;
    private final ReviewJobProducer reviewJobProducer;
    private final ReviewMapper reviewMapper;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public ReviewJob createReviewJobFromWebhook(
            RepositoryMapping mapping,
            ReviewEventType eventType,
            String commitSha,
            Integer pullRequestNumber,
            String pullRequestTitle,
            String branchName,
            String senderUsername
    ) {
        String jobRef = "REV-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        ReviewJob job = ReviewJob.builder()
                .jobReference(jobRef)
                .project(mapping.getProject())
                .repositoryMapping(mapping)
                .eventType(eventType)
                .status(ReviewJobStatus.PENDING)
                .commitSha(commitSha != null ? commitSha : "unknown-sha")
                .pullRequestNumber(pullRequestNumber)
                .pullRequestTitle(pullRequestTitle)
                .branchName(branchName != null ? branchName : "main")
                .senderUsername(senderUsername != null ? senderUsername : "github-actions")
                .queuedAt(LocalDateTime.now())
                .findingsCount(0)
                .build();

        ReviewJob savedJob = reviewJobRepository.save(job);
        log.info("Created review job {} for project '{}' on {} event", jobRef, mapping.getProject().getName(), eventType);

        // Enqueue via asynchronous RabbitMQ producer
        reviewJobProducer.publishReviewJob(ReviewJobMessage.builder()
                .reviewJobId(savedJob.getId())
                .jobReference(savedJob.getJobReference())
                .projectId(mapping.getProject().getId())
                .commitSha(savedJob.getCommitSha())
                .branchName(savedJob.getBranchName())
                .pullRequestNumber(savedJob.getPullRequestNumber())
                .eventType(eventType.name())
                .build());

        return savedJob;
    }

    @Override
    @Transactional
    public ReviewJobResponse createManualReviewJob(Long projectId, CreateManualReviewRequest request, UserPrincipal currentUser) {
        log.info("Manual review requested for project ID: {}, branch: {}", projectId, request.getBranchName());

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new InvalidProjectException("Project not found with id: " + projectId));

        validateProjectAccess(project, currentUser);

        RepositoryMapping mapping = repositoryMappingRepository.findByProjectId(projectId)
                .orElseThrow(() -> new RepositoryNotLinkedException("Cannot start review: Project has no connected repository."));

        String jobRef = "REV-MAN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        ReviewJob job = ReviewJob.builder()
                .jobReference(jobRef)
                .project(project)
                .repositoryMapping(mapping)
                .eventType(ReviewEventType.MANUAL)
                .status(ReviewJobStatus.PENDING)
                .commitSha(request.getCommitSha() != null ? request.getCommitSha() : "HEAD")
                .pullRequestNumber(request.getPullRequestNumber())
                .pullRequestTitle(request.getPullRequestTitle() != null ? request.getPullRequestTitle() : "Manual Branch Review: " + request.getBranchName())
                .branchName(request.getBranchName())
                .senderUsername(currentUser.getUsername())
                .queuedAt(LocalDateTime.now())
                .findingsCount(0)
                .build();

        ReviewJob saved = reviewJobRepository.save(job);

        reviewJobProducer.publishReviewJob(ReviewJobMessage.builder()
                .reviewJobId(saved.getId())
                .jobReference(saved.getJobReference())
                .projectId(project.getId())
                .commitSha(saved.getCommitSha())
                .branchName(saved.getBranchName())
                .pullRequestNumber(saved.getPullRequestNumber())
                .eventType("MANUAL")
                .build());

        return reviewMapper.toReviewJobResponse(saved);
    }

    @Override
    @Transactional
    public void executeReviewPipeline(Long reviewJobId) {
        log.info("Executing review pipeline for job ID: {}", reviewJobId);

        ReviewJob job = reviewJobRepository.findById(reviewJobId).orElse(null);
        if (job == null) {
            log.error("Review job ID {} not found", reviewJobId);
            return;
        }

        try {
            job.setStatus(ReviewJobStatus.IN_PROGRESS);
            job.setStartedAt(LocalDateTime.now());
            reviewJobRepository.save(job);

            List<ReviewFinding> allFindings = new ArrayList<>();

            // 1. Fetch changed files and diff information
            Map<String, String> changedFiles = simulateChangedFilesDiff(job);

            // 2. Execute SonarQube analysis
            List<ReviewFinding> sonarFindings = sonarQubeAnalysisService.analyzeProject(job);
            allFindings.addAll(sonarFindings);
            String qualityGate = sonarQubeAnalysisService.getQualityGateStatus(job);
            job.setSonarQualityGateStatus(qualityGate);

            // 3. Execute Rule Engine on changed files
            for (Map.Entry<String, String> entry : changedFiles.entrySet()) {
                String fileName = entry.getKey();
                String content = entry.getValue();

                List<RuleViolation> violations = ruleEngineService.evaluateFile(fileName, content);
                for (RuleViolation violation : violations) {
                    allFindings.add(ReviewFinding.builder()
                            .reviewJob(job)
                            .filePath(fileName)
                            .lineNumber(violation.getLineNumber())
                            .severity(mapRuleSeverityToFindingSeverity(violation.getSeverity()))
                            .category(violation.getCategory().name())
                            .title(violation.getRuleName())
                            .description(violation.getMessage())
                            .suggestedFix(violation.getRemediation())
                            .ruleKey(violation.getRuleKey())
                            .ruleName(violation.getRuleName())
                            .source("RULE_ENGINE")
                            .build());
                }
            }

            // 4. Save findings to database
            for (ReviewFinding finding : allFindings) {
                reviewFindingRepository.save(finding);
            }

            // 5. Prepare context for subsequent AI module review
            String aiContext = prepareAiReviewContext(job, changedFiles, allFindings);
            job.setAiReviewContext(aiContext);

            // 6. Calculate summary metrics and save ReviewSummary
            int critical = (int) allFindings.stream().filter(f -> f.getSeverity() == FindingSeverity.CRITICAL).count();
            int high = (int) allFindings.stream().filter(f -> f.getSeverity() == FindingSeverity.HIGH).count();
            int medium = (int) allFindings.stream().filter(f -> f.getSeverity() == FindingSeverity.MEDIUM).count();
            int low = (int) allFindings.stream().filter(f -> f.getSeverity() == FindingSeverity.LOW || f.getSeverity() == FindingSeverity.INFO).count();

            ReviewSummary summary = ReviewSummary.builder()
                    .reviewJob(job)
                    .totalFilesAnalyzed(changedFiles.size())
                    .criticalCount(critical)
                    .highCount(high)
                    .mediumCount(medium)
                    .lowCount(low)
                    .sonarQualityGateStatus(qualityGate)
                    .executiveSummary(String.format("Analysis completed for %s. Found %d issues across %d files.",
                            job.getBranchName(), allFindings.size(), changedFiles.size()))
                    .actionableAdvice(critical > 0 ? "Resolve CRITICAL security and governance issues prior to merge." : "Code is in acceptable health.")
                    .build();

            reviewSummaryRepository.save(summary);

            job.setFindingsCount(allFindings.size());
            job.setSummary(summary.getExecutiveSummary());
            job.setStatus(ReviewJobStatus.COMPLETED);
            job.setCompletedAt(LocalDateTime.now());
            reviewJobRepository.save(job);

            log.info("Review pipeline completed for job {} with {} findings", job.getJobReference(), allFindings.size());
        } catch (Exception ex) {
            log.error("Pipeline failure for job {}: {}", job.getJobReference(), ex.getMessage(), ex);
            job.setStatus(ReviewJobStatus.FAILED);
            job.setCompletedAt(LocalDateTime.now());
            job.setSummary("Pipeline failed: " + ex.getMessage());
            reviewJobRepository.save(job);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ReviewJobResponse> getProjectReviewJobs(Long projectId, Pageable pageable, UserPrincipal currentUser) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new InvalidProjectException("Project not found with id: " + projectId));

        validateProjectAccess(project, currentUser);

        return reviewJobRepository.findByProjectIdOrderByQueuedAtDesc(projectId, pageable)
                .map(reviewMapper::toReviewJobResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReviewJobResponse> getPullRequestReviews(Long projectId, Integer prNumber, UserPrincipal currentUser) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new InvalidProjectException("Project not found with id: " + projectId));

        validateProjectAccess(project, currentUser);

        return reviewJobRepository.findByProjectIdAndPullRequestNumberOrderByQueuedAtDesc(projectId, prNumber).stream()
                .map(reviewMapper::toReviewJobResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ReviewJobResponse> getDeveloperReviews(UserPrincipal currentUser, Pageable pageable) {
        log.info("Fetching reviews for developer: {}", currentUser.getUsername());
        return reviewJobRepository.findBySenderUsernameOrderByQueuedAtDesc(currentUser.getUsername(), pageable)
                .map(reviewMapper::toReviewJobResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public ReviewJobDetailResponse getReviewJobDetails(String jobReference, UserPrincipal currentUser) {
        ReviewJob job = reviewJobRepository.findByJobReference(jobReference)
                .orElseThrow(() -> new InvalidReviewJobException("Review job not found for reference: " + jobReference));

        validateProjectAccess(job.getProject(), currentUser);

        List<ReviewFinding> findings = reviewFindingRepository.findByReviewJobIdOrderBySeverityAsc(job.getId());
        ReviewSummary summary = reviewSummaryRepository.findByReviewJobId(job.getId()).orElse(null);

        return reviewMapper.toReviewJobDetailResponse(job, findings, summary);
    }

    @Override
    @Transactional(readOnly = true)
    public ReviewInsightsResponse getProjectReviewInsights(Long projectId, UserPrincipal currentUser) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new InvalidProjectException("Project not found with id: " + projectId));

        validateProjectAccess(project, currentUser);

        long total = reviewJobRepository.countByProjectId(projectId);
        long completed = reviewJobRepository.countByProjectIdAndStatus(projectId, ReviewJobStatus.COMPLETED);
        long failed = reviewJobRepository.countByProjectIdAndStatus(projectId, ReviewJobStatus.FAILED);

        long critical = reviewFindingRepository.countByReviewJobProjectIdAndSeverity(projectId, FindingSeverity.CRITICAL);
        long high = reviewFindingRepository.countByReviewJobProjectIdAndSeverity(projectId, FindingSeverity.HIGH);
        long medium = reviewFindingRepository.countByReviewJobProjectIdAndSeverity(projectId, FindingSeverity.MEDIUM);

        double passRate = total > 0 ? ((double) completed / total) * 100.0 : 100.0;
        List<ReviewJobResponse> recent = reviewJobRepository.findTop5ByProjectIdOrderByQueuedAtDesc(projectId).stream()
                .map(reviewMapper::toReviewJobResponse)
                .collect(Collectors.toList());

        return ReviewInsightsResponse.builder()
                .projectId(project.getId())
                .projectName(project.getName())
                .totalReviews(total)
                .completedReviews(completed)
                .failedReviews(failed)
                .criticalFindingsTotal(critical)
                .highFindingsTotal(high)
                .mediumFindingsTotal(medium)
                .qualityGatePassRate(Math.round(passRate * 10.0) / 10.0)
                .recentReviews(recent)
                .build();
    }

    private Map<String, String> simulateChangedFilesDiff(ReviewJob job) {
        Map<String, String> files = new HashMap<>();

        files.put("src/main/java/com/sanket/platform/user/UserController.java",
                "package com.sanket.platform.user;\n" +
                "import org.springframework.web.bind.annotation.*;\n" +
                "@RestController\n" +
                "public class UserController {\n" +
                "    @PostMapping(\"/users\")\n" +
                "    public ResponseEntity<User> createUser(@RequestBody CreateUserDto dto) {\n" +
                "        System.out.println(\"Creating user: \" + dto.getUsername());\n" +
                "        return ResponseEntity.ok(userService.create(dto));\n" +
                "    }\n" +
                "}\n"
        );

        files.put("src/main/java/com/sanket/platform/service/PaymentService.java",
                "package com.sanket.platform.service;\n" +
                "@Service\n" +
                "public class PaymentService {\n" +
                "    private String apiKey = \"secret_key_prod_123456\";\n" +
                "    public void processPayment() {\n" +
                "        try {\n" +
                "            doCharge();\n" +
                "        } catch (Exception e) {\n" +
                "            e.printStackTrace();\n" +
                "        }\n" +
                "    }\n" +
                "}\n"
        );

        return files;
    }

    private String prepareAiReviewContext(ReviewJob job, Map<String, String> changedFiles, List<ReviewFinding> findings) {
        try {
            Map<String, Object> context = new LinkedHashMap<>();
            context.put("jobReference", job.getJobReference());
            context.put("projectName", job.getProject().getName());
            context.put("repository", job.getRepositoryMapping().getRepoFullName());
            context.put("branch", job.getBranchName());
            context.put("commitSha", job.getCommitSha());
            context.put("pullRequestNumber", job.getPullRequestNumber());
            context.put("filesAnalyzed", changedFiles.keySet());
            context.put("sonarQualityGate", job.getSonarQualityGateStatus());
            context.put("staticRuleFindingsCount", findings.size());
            context.put("instructions", "Perform deep architectural reasoning, semantic bug detection, and LLM code refactoring on the attached diffs.");

            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(context);
        } catch (Exception ex) {
            return "{\"jobReference\":\"" + job.getJobReference() + "\"}";
        }
    }

    private FindingSeverity mapRuleSeverityToFindingSeverity(RuleSeverity severity) {
        return switch (severity) {
            case CRITICAL -> FindingSeverity.CRITICAL;
            case HIGH -> FindingSeverity.HIGH;
            case MEDIUM -> FindingSeverity.MEDIUM;
            case LOW -> FindingSeverity.LOW;
        };
    }

    private void validateProjectAccess(Project project, UserPrincipal currentUser) {
        if (hasRole(currentUser, "ROLE_ADMIN")) return;
        if (project.getOwner() != null && project.getOwner().getId().equals(currentUser.getId())) return;
        if (project.getTeamLead() != null && project.getTeamLead().getId().equals(currentUser.getId())) return;

        throw new UnauthorizedProjectAccessException("You do not have permission to access reviews for project: " + project.getName());
    }

    private boolean hasRole(UserPrincipal user, String roleName) {
        if (user == null || user.getAuthorities() == null) return false;
        return user.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(auth -> auth.equals(roleName));
    }
}

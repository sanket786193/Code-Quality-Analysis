package com.sanket.AI.Code.Review.Platform.review.sonar.service;

import com.sanket.AI.Code.Review.Platform.review.entity.ReviewFinding;
import com.sanket.AI.Code.Review.Platform.review.entity.ReviewJob;
import com.sanket.AI.Code.Review.Platform.review.sonar.client.SonarQubeClient;
import com.sanket.AI.Code.Review.Platform.review.sonar.model.SonarIssueDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class SonarQubeAnalysisService {

    private final SonarQubeClient sonarQubeClient;

    public List<ReviewFinding> analyzeProject(ReviewJob job) {
        String projectKey = job.getProject().getName().toLowerCase().replace(" ", "-");
        String branch = job.getBranchName();

        log.info("Running SonarQube analysis for job: {}, project: {}", job.getJobReference(), projectKey);

        List<SonarIssueDto> issues = sonarQubeClient.fetchProjectIssues(projectKey, branch);
        List<ReviewFinding> findings = new ArrayList<>();

        for (SonarIssueDto issue : issues) {
            findings.add(ReviewFinding.builder()
                    .reviewJob(job)
                    .filePath(issue.getComponent())
                    .lineNumber(issue.getLine())
                    .severity(issue.getSeverity())
                    .category("SONAR_" + issue.getType())
                    .title(issue.getMessage())
                    .description("Rule violation: " + issue.getRule() + ". " + issue.getMessage())
                    .suggestedFix("Refer to SonarQube rule guidance: " + issue.getRule())
                    .ruleKey(issue.getKey())
                    .ruleName(issue.getRule())
                    .source("SONARQUBE")
                    .build());
        }

        return findings;
    }

    public String getQualityGateStatus(ReviewJob job) {
        String projectKey = job.getProject().getName().toLowerCase().replace(" ", "-");
        return sonarQubeClient.fetchQualityGateStatus(projectKey);
    }
}

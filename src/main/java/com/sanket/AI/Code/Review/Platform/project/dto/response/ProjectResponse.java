package com.sanket.AI.Code.Review.Platform.project.dto.response;

import com.sanket.AI.Code.Review.Platform.project.entity.ProjectStatus;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProjectResponse {
    private Long id;
    private String name;
    private String description;
    private ProjectStatus status;
    private UserSummaryDto owner;
    private UserSummaryDto teamLead;
    private RepositoryMappingResponse repositoryMapping;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

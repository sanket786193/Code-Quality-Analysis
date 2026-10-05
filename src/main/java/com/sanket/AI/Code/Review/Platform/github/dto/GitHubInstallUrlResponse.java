package com.sanket.AI.Code.Review.Platform.github.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GitHubInstallUrlResponse {
    private String installUrl;
    private String appName;
    private String state;
}

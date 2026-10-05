package com.sanket.AI.Code.Review.Platform.auth.dto.response;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GitHubOAuthUrlResponse {

    private String authorizationUrl;
}

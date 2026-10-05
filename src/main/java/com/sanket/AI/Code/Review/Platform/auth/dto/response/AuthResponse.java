package com.sanket.AI.Code.Review.Platform.auth.dto.response;

import lombok.*;

import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthResponse {

    private String accessToken;

    private String refreshToken;

    @Builder.Default
    private String tokenType = "Bearer";

    private Long expiresIn;

    private Long userId;

    private String username;

    private String email;

    private String firstName;

    private String lastName;

    private String profileImage;

    private Set<String> roles;
}

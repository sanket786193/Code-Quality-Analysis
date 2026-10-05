package com.sanket.AI.Code.Review.Platform.user.dto.Response;

import lombok.*;

import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginResponse {

    private String accessToken;

    private String refreshToken;

    private String tokenType;

    private Long expiresIn;

    private Long userId;

    private String username;

    private String email;

    private Set<String> roles;
}
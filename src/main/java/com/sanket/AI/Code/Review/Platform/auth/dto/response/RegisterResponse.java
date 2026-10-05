package com.sanket.AI.Code.Review.Platform.auth.dto.response;

import lombok.*;

import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegisterResponse {

    private Long userId;

    private String username;

    private String email;

    private String message;

    private Set<String> roles;
}

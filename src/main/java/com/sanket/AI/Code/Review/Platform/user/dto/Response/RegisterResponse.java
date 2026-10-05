package com.sanket.AI.Code.Review.Platform.user.dto.Response;

import lombok.*;

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
}
package com.sanket.AI.Code.Review.Platform.user.dto.Response;

import com.sanket.AI.Code.Review.Platform.user.entity.AuthProvider;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponse {

    private Long id;
    private String firstName;
    private String lastName;
    private String username;
    private String email;
    private String profileImage;
    private String githubUsername;
    private String githubId;
    private AuthProvider provider;
    private String company;
    private String designation;
    private Boolean active;
    private Boolean accountLocked;
    private Boolean emailVerified;
    private Set<String> roles;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

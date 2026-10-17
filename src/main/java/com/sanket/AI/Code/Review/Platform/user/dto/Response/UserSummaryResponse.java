package com.sanket.AI.Code.Review.Platform.user.dto.Response;

import lombok.*;

import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserSummaryResponse {

    private Long id;
    private String firstName;
    private String lastName;
    private String username;
    private String email;
    private String designation;
    private String company;
    private String profileImage;
    private Boolean active;
    private Set<String> roles;
}

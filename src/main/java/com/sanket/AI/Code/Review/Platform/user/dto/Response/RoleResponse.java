package com.sanket.AI.Code.Review.Platform.user.dto.Response;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoleResponse {

    private Long id;
    private String roleName;
    private String description;
}

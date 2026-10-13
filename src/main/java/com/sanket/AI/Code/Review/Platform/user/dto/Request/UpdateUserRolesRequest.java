package com.sanket.AI.Code.Review.Platform.user.dto.Request;

import jakarta.validation.constraints.NotEmpty;
import lombok.*;

import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateUserRolesRequest {

    @NotEmpty(message = "At least one role must be provided")
    private Set<String> roles;
}

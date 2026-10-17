package com.sanket.AI.Code.Review.Platform.user.dto.Request;

import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateUserStatusRequest {

    @NotNull(message = "Active status is required")
    private Boolean active;
}

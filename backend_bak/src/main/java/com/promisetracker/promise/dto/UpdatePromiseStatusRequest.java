package com.promisetracker.promise.dto;

import com.promisetracker.promise.PromiseStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdatePromiseStatusRequest {

    @NotNull(message = "Status is required")
    private PromiseStatus status;
}

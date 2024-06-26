package org.jolly.oracle.map.web.rest.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

@Value
@Builder
@Jacksonized
public class ScheduleJobRequest {
    @NotNull
    @NotBlank
    String jobName;
    @NotNull
    @NotBlank
    String cronExpression;
}

package org.jolly.oracle.map.web.rest.dto;

import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

import java.time.LocalDateTime;

@Value
@Builder
@Jacksonized
public class JobDetailResponse {
    String name;
    String cronExpression;
    LocalDateTime prevFireTime;
}

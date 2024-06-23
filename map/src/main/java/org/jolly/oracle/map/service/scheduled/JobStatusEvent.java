package org.jolly.oracle.map.service.scheduled;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class JobStatusEvent {
    String jobName;
    JobStatus status;
    String exceptionMessage;
}

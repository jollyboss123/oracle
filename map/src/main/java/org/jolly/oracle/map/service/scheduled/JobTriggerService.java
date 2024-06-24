package org.jolly.oracle.map.service.scheduled;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jolly.oracle.map.domain.JobTrigger;
import org.jolly.oracle.map.repository.JobTriggerRepository;
import org.springframework.data.domain.Limit;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
public class JobTriggerService {
    private final JobTriggerRepository jobTriggerRepository;

    @Transactional
    public Optional<JobTrigger> save(@Nullable JobTrigger jobTrigger) {
        if (jobTrigger == null) {
            throw new IllegalArgumentException("jobTrigger");
        }

        return Optional.of(jobTriggerRepository.save(jobTrigger));
    }

    public Optional<JobStatus> findLatestStatusByName(@Nullable String jobName) {
        if (StringUtils.isBlank(jobName)) {
            throw new IllegalArgumentException("jobName");
        }

        return jobTriggerRepository.findLatestStatusByName(jobName);
    }

    public Optional<JobTrigger> findLatestByNameAndStatus(@Nullable String jobName, @Nullable JobStatus status) {
        if (StringUtils.isBlank(jobName)) {
            throw new IllegalArgumentException("jobName");
        }
        if (status == null) {
            throw new IllegalArgumentException("status");
        }

        return jobTriggerRepository.findLatestByNameAndStatus(jobName, status);
    }
}

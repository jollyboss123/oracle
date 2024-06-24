package org.jolly.oracle.map.service.scheduled;

import lombok.RequiredArgsConstructor;
import org.jolly.oracle.map.domain.JobTrigger;
import org.jolly.oracle.map.repository.JobTriggerRepository;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class JobTriggerService {
    private final JobTriggerRepository jobTriggerRepository;

    @Transactional
    public Optional<JobTrigger> save(@Nullable JobTrigger jobTrigger) {
        if (jobTrigger == null) {
            return Optional.empty();
        }

        return Optional.of(jobTriggerRepository.save(jobTrigger));
    }
}

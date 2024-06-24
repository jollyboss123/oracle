package org.jolly.oracle.map.service.scheduled;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.jolly.oracle.map.domain.JobDetail;
import org.jolly.oracle.map.domain.JobTrigger;
import org.jolly.oracle.map.repository.JobDetailRepository;
import org.jolly.oracle.map.web.rest.JobController;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class JobDetailService {
    private final JobDetailRepository jobDetailRepository;
    private final JobTriggerService jobTriggerService;

    public Optional<JobDetail> findByName(@Nullable String name) {
        if (StringUtils.isBlank(name)) {
            throw new IllegalArgumentException("job name");
        }

        return jobDetailRepository.findByName(name);
    }

    @Transactional
    public Optional<JobDetail> save(@Nullable JobDetail jobDetail) {
        if (jobDetail == null) {
            return Optional.empty();
        }

        return Optional.of(jobDetailRepository.save(jobDetail));
    }

    public Optional<JobController.JobDetailResponse> getDetails(@Nullable String name) {
        if (StringUtils.isBlank(name)) {
            throw new IllegalArgumentException("job name");
        }

        return findByName(name)
                .map(jobDetail -> {
                    JobStatus latestStatus = jobTriggerService.findLatestStatusByName(jobDetail.getName())
                            .orElse(null);
                    JobTrigger latestTrigger = jobTriggerService.findLatestByNameAndStatus(jobDetail.getName(),
                            JobStatus.RUNNING)
                            .orElse(null);

                    return JobController.JobDetailResponse.builder()
                            .name(jobDetail.getName())
                            .cronExpression(jobDetail.getCronExpression())
                            .latestStatus(latestStatus)
                            .prevFireTime(latestTrigger != null ? latestTrigger.getAudit().getCreatedOn() : null)
                            .build();
                });
    }
}

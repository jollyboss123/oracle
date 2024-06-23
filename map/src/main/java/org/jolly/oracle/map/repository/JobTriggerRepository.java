package org.jolly.oracle.map.repository;

import org.jolly.oracle.map.domain.JobTrigger;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JobTriggerRepository extends JpaRepository<JobTrigger, Long> {
}

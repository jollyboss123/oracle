package org.jolly.oracle.map.repository;

import org.jolly.oracle.map.domain.JobTrigger;
import org.jolly.oracle.map.service.scheduled.JobStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

public interface JobTriggerRepository extends JpaRepository<JobTrigger, Long> {
    @Transactional(readOnly = true)
    @Query("""
        select jt.status
        from JobTrigger jt
        where jt.name = :name
        order by jt.audit.createdOn desc
        limit 1
        """)
    Optional<JobStatus> findLatestStatusByName(@Param("name") String name);

    @Transactional(readOnly = true)
    @Query("""
        select jt
        from JobTrigger jt
        where jt.name = :name
        and jt.status = :status
        order by jt.audit.createdOn desc
        limit 1
        """)
    Optional<JobTrigger> findLatestByNameAndStatus(@Param("name") String name, @Param("status")JobStatus status);
}

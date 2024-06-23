package org.jolly.oracle.map.repository;

import org.jolly.oracle.map.domain.JobDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

public interface JobDetailRepository extends JpaRepository<JobDetail, Long> {
    @Transactional(readOnly = true)
    @Query("""
        select jd
        from JobDetail jd
        where jd.name = :name
        """)
    Optional<JobDetail> findByName(@Param("name") String name);
}

package org.jolly.oracle.map.domain;

import jakarta.persistence.*;
import lombok.Getter;
import org.hibernate.proxy.HibernateProxy;
import org.jolly.oracle.map.service.scheduled.JobStatus;

import java.util.Objects;

// append-only
@Getter
@Entity
@Table(name = "job_triggers")
@EntityListeners(AuditListener.class)
public class JobTrigger implements Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "job_triggers_seq")
    @SequenceGenerator(name = "job_triggers_seq", allocationSize = 50)
    @Column(name = "id")
    private Long id;

//    @Column(name = "trace_id")
//    private String traceId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_details_id")
    private JobDetail detail;

    @Column(name = "name")
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private JobStatus status;

    @Embedded
    private Audit audit;

    public JobTrigger setId(Long id) {
        this.id = id;
        return this;
    }

//    public JobTrigger setTraceId(String traceId) {
//        this.traceId = traceId;
//        return this;
//    }

    public JobTrigger setDetail(JobDetail detail) {
        this.detail = detail;
        return this;
    }

    public JobTrigger setName(String name) {
        this.name = name;
        return this;
    }

    public JobTrigger setStatus(JobStatus status) {
        this.status = status;
        return this;
    }

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        Class<?> oEffectiveClass = o instanceof HibernateProxy hp ? hp.getHibernateLazyInitializer().getPersistentClass() : o.getClass();
        Class<?> thisEffectiveClass = this instanceof HibernateProxy hp ? hp.getHibernateLazyInitializer().getPersistentClass() : this.getClass();
        if (thisEffectiveClass != oEffectiveClass) return false;
        JobTrigger jobTrigger = (JobTrigger) o;
        return getId() != null && Objects.equals(getId(), jobTrigger.getId());
    }

    @Override
    public final int hashCode() {
        return this instanceof HibernateProxy hp ? hp.getHibernateLazyInitializer().getPersistentClass().hashCode() : getClass().hashCode();
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "(" +
                "id = " + id + ", " +
                "name = " + name + ", " +
                "status = " + status + ")";
    }

    @Override
    public void setAudit(Audit audit) {
        this.audit = audit;
    }
}

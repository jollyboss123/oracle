package org.jolly.oracle.map.domain;

import jakarta.persistence.*;
import lombok.Getter;
import org.hibernate.proxy.HibernateProxy;

import java.time.LocalDateTime;
import java.util.Objects;

@Getter
@Entity
@Table(name = "job_details")
@EntityListeners(AuditListener.class)
public class JobDetail implements Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "job_details_seq")
    @SequenceGenerator(name = "job_details_seq", allocationSize = 50)
    @Column(name = "id")
    private Long id;

    @Column(name = "name")
    private String name;

    @Column(name = "cron_expression")
    private String cronExpression;

    @Column(name = "is_active")
    private boolean isActive = true;

//    @Column(name = "next_fire_time")
//    private LocalDateTime nextFireTime;

    public JobDetail setId(Long id) {
        this.id = id;
        return this;
    }

    public JobDetail setName(String name) {
        this.name = name;
        return this;
    }

    public JobDetail setCronExpression(String cronExpression) {
        this.cronExpression = cronExpression;
        return this;
    }

    public JobDetail setIsActive(boolean isActive) {
        this.isActive = isActive;
        return this;
    }

//    public JobDetail setNextFireTime(LocalDateTime nextFireTime) {
//        this.nextFireTime = nextFireTime;
//        return this;
//    }

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        Class<?> oEffectiveClass = o instanceof HibernateProxy hp ? hp.getHibernateLazyInitializer().getPersistentClass() : o.getClass();
        Class<?> thisEffectiveClass = this instanceof HibernateProxy hp ? hp.getHibernateLazyInitializer().getPersistentClass() : this.getClass();
        if (thisEffectiveClass != oEffectiveClass) return false;
        JobDetail jobDetail = (JobDetail) o;
        return getId() != null && Objects.equals(getId(), jobDetail.getId());
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
                "cronExpression = " + cronExpression;
    }

    @Embedded
    private Audit audit;

    @Override
    public void setAudit(Audit audit) {
        this.audit = audit;
    }
}

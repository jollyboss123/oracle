package org.jolly.oracle.map.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.proxy.HibernateProxy;

import java.util.Objects;

@Getter
@Entity
@Table(name = "stocks")
public class Stock {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "stocks_seq")
    @SequenceGenerator(name = "stocks_seq", allocationSize = 50)
    @Column(name = "id")
    private Long id;

    //TODO: enforce unique and length constraint with a check constraint externally
    // https://www.postgresql.org/docs/current/ddl-constraints.html#DDL-CONSTRAINTS-CHECK-CONSTRAINTS
    // this way we can alter it without interfering the application
    // but we should alter it by dropping and re-creating the check constraint
    // because `alter table` may require exclusive table lock
    // https://dba.stackexchange.com/questions/20974/should-i-add-an-arbitrary-length-limit-to-varchar-columns
    //TODO: remove unique = true
    @Column(name = "ticker", nullable = false)
    private String ticker;

    @Column(name = "name")
    private String name;

    @Embedded
    private Audit audit;

    public Stock setId(Long id) {
        this.id = id;
        return this;
    }

    public Stock setTicker(String ticker) {
        this.ticker = ticker;
        return this;
    }

    public Stock setName(String name) {
        this.name = name;
        return this;
    }

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        Class<?> oEffectiveClass = o instanceof HibernateProxy hp ? hp.getHibernateLazyInitializer().getPersistentClass() : o.getClass();
        Class<?> thisEffectiveClass = this instanceof HibernateProxy hp ? hp.getHibernateLazyInitializer().getPersistentClass() : this.getClass();
        if (thisEffectiveClass != oEffectiveClass) return false;
        Stock stock = (Stock) o;
        return getId() != null && Objects.equals(getId(), stock.getId());
    }

    @Override
    public final int hashCode() {
        return this instanceof HibernateProxy hp ? hp.getHibernateLazyInitializer().getPersistentClass().hashCode() : getClass().hashCode();
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "(" +
                "id = " + id + ", " +
                "ticker = " + ticker + ", " +
                "name = " + name + ")";
    }
}

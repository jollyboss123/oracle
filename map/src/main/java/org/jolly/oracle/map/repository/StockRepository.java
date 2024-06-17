package org.jolly.oracle.map.repository;

import org.jolly.oracle.map.domain.Stock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

public interface StockRepository extends JpaRepository<Stock, Long> {
//    @Transactional
//    @Modifying
//    @Query("""
//        insert into Stock (ticker, name, createdOn)
//        values (:ticker, :name, local datetime)
//        on conflict(ticker)
//        do update set
//        name = excluded.name,
//        updatedOn = local datetime
//        """)
//    void upsert(@Param("ticker") String ticker, @Param("name") String name);

    @Transactional(readOnly = true)
    @Query("""
        select s
        from Stock s
        where s.ticker = :ticker
        """)
    Optional<Stock> findByTicker(@Param("ticker") String ticker);
}

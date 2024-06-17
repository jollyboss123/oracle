package org.jolly.oracle.map.repository;

import org.jolly.oracle.map.domain.Stock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

public interface StockRepository extends JpaRepository<Stock, Long> {
    @Transactional
    @Modifying
    @Query("""
        insert into Stock (ticker, name)
        values (:ticker, :name)
        on conflict(id)
        do update set
        ticker = excluded.ticker,
        name = excluded.name
        """)
    void upsert(String ticker, String name);
}

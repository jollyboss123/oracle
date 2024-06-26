package org.jolly.oracle.map.repository;

import org.jolly.oracle.map.domain.Stock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;

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
        where s.ticker in (:tickers)
        """)
    Collection<Stock> findByTickersIn(@Param("tickers") Collection<String> tickers);

    @Transactional(readOnly = true)
    @Query(value = """
        select v.*
        from (values (:tickers)) v(ticker)
        where not exists(
            select 1
            from stocks s
            where s.ticker = v.ticker
        )
        """,
    nativeQuery = true)
    Collection<String> findAllTickersNotExist(@Param("tickers") Collection<String> tickers);
}

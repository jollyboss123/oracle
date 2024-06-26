package org.jolly.oracle.map.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jolly.oracle.map.web.rest.dto.VarRequest;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ValidationService {
    private final NamedParameterJdbcTemplate jdbcTemplate;

    public boolean validateTickers(List<VarRequest.Asset> assets) {
        if (CollectionUtils.isEmpty(assets)) {
            return true;
        }

        String values = IntStream.range(0, assets.size())
                .mapToObj(i -> "(:ticker_" + i + ")")
                .collect(Collectors.joining(", "));

        String sql = """
                select v.*
                from (values 
                """ + values +
                """
                ) as v(ticker)
                where not exists (
                    select 1
                    from stocks s
                    where s.ticker = v.ticker
                )
                """;

        MapSqlParameterSource params = new MapSqlParameterSource();
        for (int i = 0; i < assets.size(); i++) {
            params.addValue("ticker_" + i, assets.get(i).getTicker());
        }

        List<String> invalidTickers = jdbcTemplate.queryForList(sql, params, String.class);
        if (!invalidTickers.isEmpty()) {
            log.info("invalid tickers: {}", invalidTickers);
        }
        return invalidTickers.isEmpty();
    }
}

package org.jolly.oracle.map.service.scheduled;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.jolly.oracle.map.domain.Stock;
import org.jolly.oracle.map.repository.StockRepository;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Component
@Slf4j
@RequiredArgsConstructor
public class FetchStocksInfoTask implements Runnable {
    private final StockRepository stockRepository;
    public static final String TASK_NAME = "fetch-stocks-info";

    private record StockRecord(String ticker, String name) {}

    @Override
    public void run() {
        log.info("starting fetch stocks scheduled task");

        URL url;
        try {
            url = UriComponentsBuilder.newInstance()
                    .scheme("http")
                    .host("datasrv.ddfplus.com")
                    .path("/names/nasd.txt")
                    .build()
                    .toUri().toURL();
        } catch (MalformedURLException e) {
            log.error("malformed fund uri", e);
            throw new FetchStocksInfoTaskException("Malformed fund uri", e);
        }

        // 3. parse
        try (InputStreamReader is = new InputStreamReader(url.openStream());
             BufferedReader br = new BufferedReader(is)) {
            Iterable<CSVRecord> records;
            try {
                records = CSVFormat.RFC4180.builder()
                        .setDelimiter(":")
                        .build()
                        .parse(br);
            } catch (IOException e) {
                log.error("parse csv error", e);
                throw new FetchStocksInfoTaskException("Parse csv error", e);
            }

            List<StockRecord> stocks = StreamSupport.stream(records.spliterator(), true)
                    .map(rec -> new StockRecord(rec.get(0), rec.get(1)))
                    .toList();
            log.debug("stocks size: {}", stocks.size());

            Map<String, Stock> existing;
            if (stocks.isEmpty()) {
                existing = Collections.emptyMap();
            } else {
                existing = stockRepository.findByTickersIn(stocks.stream().map(StockRecord::ticker).toList()).stream()
                        .collect(Collectors.toMap(Stock::getTicker, Function.identity()));
            }
            log.debug("existing size: {}", existing.size());

            List<Stock> update = stocks.parallelStream()
                    .map(rec -> {
                        String ticker = rec.ticker;
                        String name = rec.name;

                        Stock stock = existing.getOrDefault(ticker, new Stock().setTicker(ticker));
                        stock.setName(name);
                        return stock;
                    })
                    .toList();

            // 4. save to db by batches
            List<Stock> updated = stockRepository.saveAll(update);
            log.info("saved/updated {} records", updated.size());
        } catch (IOException e) {
            log.error("open input stream error", e);
            throw new FetchStocksInfoTaskException("open input stream error", e);
        }

        log.info("fetch stocks scheduled task completed");
    }
}

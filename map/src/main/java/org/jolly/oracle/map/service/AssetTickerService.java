package org.jolly.oracle.map.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.jolly.oracle.map.domain.Stock;
import org.jolly.oracle.map.repository.StockRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

@Service
@Slf4j
@RequiredArgsConstructor
public class AssetTickerService {
    private final StockRepository stockRepository;

    // should be a daily cron job
    public void fetchStocks() throws IOException {
        // 1. check from db if updated today

        // 2. check if there is ongoing task of updating in db today

        // 3. if false, pull from /datasrv
        URL url;
        try {
            url = UriComponentsBuilder.newInstance()
                    .scheme("http")
                    .host("datasrv.ddfplus.com")
                    .path("/names/funds.txt")
                    .build()
                    .toUri().toURL();
        } catch (MalformedURLException e) {
            log.error("malformed fund uri", e);
            throw new IOException("Malformed fund uri", e);
        }

        // 4. parse
        Set<Stock> stocks = new HashSet<>();

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
                throw new IOException("Parse csv error", e);
            }
            for (CSVRecord r : records) {
                String ticker = r.get(0);
                String name = r.get(1);
                stocks.add(new Stock()
                        .setTicker(ticker)
                        .setName(name));
            }
        }

        // 5. save to db by batches
        stocks.forEach(s -> stockRepository.upsert(s.getTicker(), s.getName()));
    }
}

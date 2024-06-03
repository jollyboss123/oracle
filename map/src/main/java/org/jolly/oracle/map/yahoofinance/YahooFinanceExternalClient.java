package org.jolly.oracle.map.yahoofinance;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.net.URL;
import java.net.URLConnection;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class YahooFinanceExternalClient {

    public QuotesResponse fetchHistory(QuotesRequest request) {
        try (InputStreamReader is = new InputStreamReader(getUrlStream(request));
             BufferedReader br = new BufferedReader(is)) {
            return QuotesResponse.builder()
                    .ticker(request.getTicker())
                    .results(parse(br))
                    .build();
        } catch (IOException e) {
            //TODO: throw specific exception
            throw new RuntimeException(e);
        }
    }

    private InputStream getUrlStream(QuotesRequest request) throws IOException {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("period1", Long.toString(request.getFrom().atStartOfDay(ZoneOffset.UTC).toEpochSecond()));
        params.add("period2", Long.toString(request.getTo().atStartOfDay(ZoneOffset.UTC).toEpochSecond()));
        params.add("interval", request.getInterval().getValue());

        URL url = UriComponentsBuilder.newInstance()
                .scheme("https")
                .host("query1.finance.yahoo.com")
                .path("/v7/finance/download/{ticker}")
                .queryParams(params)
                .buildAndExpand(request.getTicker())
                .toUri().toURL();
        //TODO: set connect and read timeout from properties
        RedirectableRequest redirectableRequest = RedirectableRequest.of(url, 5);
        URLConnection connection = redirectableRequest.openConnection();
        return connection.getInputStream();
    }

    private static List<QuotesResponse.Result> parse(BufferedReader br) throws IOException {
        Iterable<CSVRecord> records = CSVFormat.RFC4180.builder()
                .setHeader()
                .setSkipHeaderRecord(true)
                .build()
                .parse(br);

        List<QuotesResponse.Result> results = new ArrayList<>();
        for (CSVRecord r : records) {
            BigDecimal adjClose = new BigDecimal(r.get("Adj Close"));
            results.add(QuotesResponse.Result.builder()
                    .adjustedClose(adjClose)
                    .build());
        }

        return results;
    }
}

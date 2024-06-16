package org.jolly.oracle.map.service.yahoofinance;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.jolly.oracle.map.service.IQuoteResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class YahooFinanceExternalClient {
    @Value("${app.yahoo-finance.connect-timeout}")
    private final int connectTimeout;
    @Value("${app.yahoo-finance.read-timeout}")
    private final int readTimeout;

    public IQuoteResponse fetchHistory(QuotesRequest request) {
        try (InputStreamReader is = new InputStreamReader(getUrlStream(request));
             BufferedReader br = new BufferedReader(is)) {
            return QuotesResponse.builder()
                    .ticker(request.getTicker())
                    .results(parse(br))
                    .build();
        } catch (IOException e) {
            log.error("connection error, likely due to failure to get input stream from connection", e);
            throw new YahooFinanceClientException("Connection error", e);
        }
    }

    private InputStream getUrlStream(QuotesRequest request) throws IOException {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("period1", Long.toString(request.getFrom().atStartOfDay(ZoneOffset.UTC).toEpochSecond()));
        params.add("period2", Long.toString(request.getTo().atStartOfDay(ZoneOffset.UTC).toEpochSecond()));
        params.add("interval", request.getInterval().getValue());

        URL url;
        try {
            url = UriComponentsBuilder.newInstance()
                    .scheme("https")
                    .host("query1.finance.yahoo.com")
                    .path("/v7/finance/download/{ticker}")
                    .queryParams(params)
                    .buildAndExpand(request.getTicker())
                    .toUri().toURL();
        } catch (MalformedURLException e) {
            log.error("malformed yahoo finance uri", e);
            throw new YahooFinanceClientException("Malformed yahoo finance uri", e);
        }

        RedirectableRequest redirectableRequest = RedirectableRequest.of(url, 5, connectTimeout, readTimeout);
        URLConnection connection;
        try {
            connection = redirectableRequest.openConnection();
        } catch (IOException e) {
            log.error("redirectable request open connection error", e);
            throw new YahooFinanceClientException("Open connection error", e);
        } catch (URISyntaxException e) {
            log.error("request not formatted correctly", e);
            throw new YahooFinanceClientException("Request not formatted correctly", e);
        }

        //TODO: handle FileNotFoundException i.e. ticker does not exist
        return connection.getInputStream();
    }

    private static List<QuotesResponse.Result> parse(BufferedReader br) {
        Iterable<CSVRecord> records;
        try {
            records = CSVFormat.RFC4180.builder()
                    .setHeader()
                    .setSkipHeaderRecord(true)
                    .build()
                    .parse(br);
        } catch (IOException e) {
            log.error("parse csv error", e);
            throw new YahooFinanceClientException("Parse csv error", e);
        }

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

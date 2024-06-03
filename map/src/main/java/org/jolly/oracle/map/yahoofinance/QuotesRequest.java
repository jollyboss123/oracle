package org.jolly.oracle.map.yahoofinance;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Value;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneOffset;

@Value
@Builder
public class QuotesRequest {
    String ticker;
    @DateTimeFormat(pattern = "YYYY-MM-DD", fallbackPatterns = "yyyy-MM-dd'T'HH:mm:ss.SSS")
    LocalDate from;
    @DateTimeFormat(pattern = "YYYY-MM-DD", fallbackPatterns = "yyyy-MM-dd'T'HH:mm:ss.SSS")
    LocalDate to;
    Interval interval;

    @AllArgsConstructor
    @Getter
    public enum Interval {
        DAILY("1d"),
        WEEKLY("5d"),
        MONTHLY("1mo")
        ;

        private final String value;
    }

    public static QuotesRequestBuilder builder() {
        return new CustomHistoryQuotesRequestBuilder();
    }

    private static class CustomHistoryQuotesRequestBuilder extends QuotesRequestBuilder {
        @Override
        public QuotesRequest build() {
            Validate.notBlank(super.ticker, "Missing ticker");
            Validate.notNull(super.interval, "Missing interval");
            Validate.notNull(super.to, "Missing to");
            super.to = super.to.atTime(LocalTime.MIDNIGHT)
                    .atOffset(ZoneOffset.UTC)
                    .toLocalDate();
            if (super.from == null) {
                super.from = super.to.minusYears(1);
            }
            if (super.to.isBefore(super.from)) {
                throw new IllegalArgumentException("From-date should not be after to-date");
            }

            StringUtils.toRootUpperCase(super.ticker);

            return super.build();
        }
    }
}

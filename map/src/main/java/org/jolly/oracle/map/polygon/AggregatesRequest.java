package org.jolly.oracle.map.polygon;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneOffset;

@Value
@Builder
@Jacksonized
public class AggregatesRequest {
    /**
     * ticker is case-sensitive.
     */
    @JsonProperty("stocksTicker")
    String ticker;
    /**
     * multiplier is the size of the {@link AggregatesRequest#timespan}.
     */
    @Builder.Default
    int multiplier = 1;
    /**
     * timespan is the size of the window.
     */
    Timespan timespan;
    /**
     * from is the start of the aggregate time window.
     */
    @DateTimeFormat(pattern = "YYYY-MM-DD", fallbackPatterns = "yyyy-MM-dd'T'HH:mm:ss.SSS")
    LocalDate from;
    /**
     * to is the end of the aggregate time window.
     */
    @DateTimeFormat(pattern = "YYYY-MM-DD", fallbackPatterns = "yyyy-MM-dd'T'HH:mm:ss.SSS")
    LocalDate to;
    /**
     * adjusted is whether the results are adjusted for splits.
     */
    @Builder.Default
    boolean adjusted = true;
    /**
     * limit is the number of base aggregates queried to create the aggregate results.
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    Integer limit;

    @AllArgsConstructor
    @Getter
    public enum Timespan {
        SECOND("second"),
        MINUTE("minute"),
        HOUR("hour"),
        DAY("day"),
        WEEK("week"),
        MONTH("month"),
        QUARTER("quarter"),
        YEAR("year")
        ;

        @JsonValue
        private final String value;
    }

    public static AggregatesRequestBuilder builder() {
        return new CustomAggregatesRequestBuilder();
    }

    private static class CustomAggregatesRequestBuilder extends AggregatesRequestBuilder {
        @Override
        public AggregatesRequest build() {
            Validate.notBlank(super.ticker, "Missing ticker");
            Validate.notNull(super.timespan, "Missing timespan");
            Validate.notNull(super.to, "Missing to");
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

package org.jolly.oracle.reduce.service;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.stream.Collector;

@Getter
@NoArgsConstructor
public class ReturnsMeanCollector {
    private BigDecimal sum = BigDecimal.ZERO;
    private BigDecimal count = BigDecimal.ZERO;

    private void add(BigDecimal val) {
        this.sum = this.sum.add(val);
        this.count = this.count.add(BigDecimal.ONE);
    }

    private ReturnsMeanCollector combine(ReturnsMeanCollector other) {
        this.sum = this.sum.add(other.getSum());
        this.count = this.count.add(other.getCount());
        return this;
    }

    public static Collector<BigDecimal, ReturnsMeanCollector, ReturnsMeanCollector> collector() {
        return Collector.of(
                ReturnsMeanCollector::new,
                ReturnsMeanCollector::add,
                ReturnsMeanCollector::combine
        );
    }
}

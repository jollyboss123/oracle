package org.jolly.oracle.map.service;

import org.springframework.lang.NonNull;

import java.util.List;

public interface IQuoteResponse {
    String getTicker();
    List<IResult> getResults();
    List<QuotesMessage.Quote> toQuotes();
}

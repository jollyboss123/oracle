package org.jolly.oracle.map.service;

import java.util.List;

public interface IQuoteResponse {
    String getTicker();
    List<IResult> getResults();
}

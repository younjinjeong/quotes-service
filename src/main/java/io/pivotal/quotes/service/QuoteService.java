package io.pivotal.quotes.service;

import io.pivotal.quotes.domain.CompanyInfo;
import io.pivotal.quotes.domain.Quote;
import io.pivotal.quotes.exception.SymbolNotFoundException;

import java.util.List;

public interface QuoteService {
    List<Quote>  getQuotes(String quote);
    List<CompanyInfo> getCompanyInfo(String name);
    Quote getQuote(String symbol) throws SymbolNotFoundException;
}
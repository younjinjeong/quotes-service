package io.pivotal.quotes.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixProperty;
import io.pivotal.quotes.domain.*;
import io.pivotal.quotes.exception.SymbolNotFoundException;
import io.pivotal.quotes.repository.CompanyOfflineRepository;
import io.pivotal.quotes.repository.QuoteOfflineRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
@Qualifier("offlineQuoteServiceImpl")
@RefreshScope
@Slf4j
public class OfflineQuoteServiceImpl implements QuoteService {

    StringBuilder str = new StringBuilder();

    @Autowired
    CompanyOfflineRepository companyOfflineRepository;

    @Autowired
    QuoteOfflineRepository quoteOfflineRepository;

    /*
     * cannot autowire as don't want ribbon here.
     */
    private RestTemplate restTemplate = new RestTemplate();

    /**
     * Retrieves an up to date quote for the given symbol.
     *
     * @param symbol The symbol to retrieve the quote for.
     * @return The quote object or null if not found.
     * @throws SymbolNotFoundException
     */
    @HystrixCommand(fallbackMethod = "getQuoteFallback")
    public Quote getQuote(String symbol) throws SymbolNotFoundException {
        log.debug("QuoteService.getQuote: retrieving quote for: " + symbol);

        Quote quote = quoteOfflineRepository.getOne(symbol);

        if (quote.getSymbol() == null) {
            throw new SymbolNotFoundException("Symbol not found: " + symbol);
        }

        log.debug("QuoteService.getQuote: retrieved quote: " + quote);
        return quote;
    }

    @SuppressWarnings("unused")
    private Quote getQuoteFallback(String symbol)
            throws SymbolNotFoundException {
        log.debug("QuoteService.getQuoteFallback: circuit opened for symbol: "
                + symbol);
        Quote quote = new Quote();
        quote.setSymbol(symbol);
        quote.setStatus("FAILED");
        return quote;
    }

    /**
     * Retrieves a list of CompanyInfor objects. Given the name parameters, the
     * return list will contain objects that match the search both on company
     * name as well as symbol.
     *
     * @param name The search parameter for company name or symbol.
     * @return The list of company information.
     */
    @HystrixCommand(fallbackMethod = "getCompanyInfoFallback",
            commandProperties = {
                    @HystrixProperty(name = "execution.timeout.enabled", value = "false")
            })
    public List<CompanyInfo> getCompanyInfo(String symbol) {
        log.debug("QuoteService.getCompanyInfo: retrieving info for: "
                + symbol);

        CompanyInfo companyInfo = companyOfflineRepository.getOne(symbol);
        List<CompanyInfo> companies = new ArrayList<>();
        companies.add(companyInfo);

        log.debug("QuoteService.getCompanyInfo: retrieved info: "
                + companies);
        return companies;
    }

    /**
     * Retrieve multiple quotes at once.
     *
     * @param symbols comma delimeted list of symbols.
     * @return a list of quotes.
     */
    public List<Quote> getQuotes(String symbols) {
        log.debug("retrieving multiple quotes for: " + symbols);

        final List<Quote> quotes = new ArrayList<>();

        Arrays.asList(symbols.split(",")).forEach(symbol -> {
            try {
                quotes.add(getQuote(symbol));
            } catch (SymbolNotFoundException e) {
                Quote quote = new Quote();
                quote.setSymbol(symbol);
                quote.setStatus("FAILED");
                quotes.add(quote);

                e.printStackTrace();
            }
        });
        System.out.println(str.toString());
        return quotes;
    }

    @SuppressWarnings("unused")
    private List<CompanyInfo> getCompanyInfoFallback(String symbol)
            throws SymbolNotFoundException {
        log.debug("QuoteService.getCompanyInfoFallback: circuit opened for symbol: "
                + symbol);
        List<CompanyInfo> companies = new ArrayList<>();
        return companies;
    }


}

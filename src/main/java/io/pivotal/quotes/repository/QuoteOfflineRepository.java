package io.pivotal.quotes.repository;

import io.pivotal.quotes.domain.Quote;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuoteOfflineRepository extends JpaRepository<Quote, String> {
}
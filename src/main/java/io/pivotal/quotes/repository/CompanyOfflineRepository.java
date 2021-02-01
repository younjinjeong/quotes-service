package io.pivotal.quotes.repository;

import io.pivotal.quotes.domain.CompanyInfo;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CompanyOfflineRepository extends JpaRepository<CompanyInfo, String> {
}

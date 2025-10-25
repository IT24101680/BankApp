package com.banksystem.webbasedbankingsystem.repository;

import com.banksystem.webbasedbankingsystem.entity.LoanDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface LoanDocumentRepository extends JpaRepository<LoanDocument, Long> {
    List<LoanDocument> findByLoan_LoanId(Long loanId);
}
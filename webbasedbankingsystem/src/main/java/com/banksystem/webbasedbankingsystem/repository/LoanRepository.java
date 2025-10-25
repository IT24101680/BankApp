package com.banksystem.webbasedbankingsystem.repository;

import com.banksystem.webbasedbankingsystem.entity.Loan;
import com.banksystem.webbasedbankingsystem.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface LoanRepository extends JpaRepository<Loan, Long> {
    List<Loan> findByApplicant(User user);
    List<Loan> findByStatus(String status);
}
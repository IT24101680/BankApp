package com.banksystem.webbasedbankingsystem.service;

import com.banksystem.webbasedbankingsystem.entity.*;
import com.banksystem.webbasedbankingsystem.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
public class LoanService {
    @Autowired
    private LoanRepository loanRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private LoanDocumentRepository loanDocumentRepository;

    public Loan applyLoan(Long userId, Double amount, String purpose) {
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
        Loan loan = new Loan();
        loan.setApplicant(user);
        loan.setAmount(amount);
        loan.setPurpose(purpose);
        return loanRepository.save(loan);
    }

    public void saveLoanDocument(Long loanId, String category, MultipartFile file) throws Exception {
        Loan loan = loanRepository.findById(loanId).orElseThrow(() -> new RuntimeException("Loan not found"));
        LoanDocument doc = new LoanDocument();
        doc.setLoan(loan);
        doc.setCategory(category);
        doc.setFileName(file.getOriginalFilename());
        doc.setFileType(file.getContentType());
        doc.setData(file.getBytes());
        loanDocumentRepository.save(doc);
    }

    public List<LoanDocument> getLoanDocuments(Long loanId) {
        return loanDocumentRepository.findByLoan_LoanId(loanId);
    }

    public List<Loan> getLoansByUser(Long userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
        return loanRepository.findByApplicant(user);
    }

    public List<Loan> getAllLoans() {
        return loanRepository.findAll();
    }


    public Loan reviewLoan(Long loanId, String status, String comment) {
        Loan loan = loanRepository.findById(loanId).orElseThrow(() -> new RuntimeException("Loan not found"));
        loan.setStatus(status); // "APPROVED", "REJECTED", "UNDER_REVIEW"
        loan.setAdminComment(comment);
        loan.setReviewedAt(java.time.LocalDateTime.now());
        return loanRepository.save(loan);
    }

    public void deleteLoan(Long loanId) {
        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new RuntimeException("Loan not found"));

        // Only allow deletion if status is PENDING or UNDER_REVIEW
        if (!"PENDING".equals(loan.getStatus()) && !"UNDER_REVIEW".equals(loan.getStatus())) {
            throw new RuntimeException("Cannot delete a loan that is already " + loan.getStatus().toLowerCase() + ".");
        }

        loanRepository.delete(loan);
    }
}
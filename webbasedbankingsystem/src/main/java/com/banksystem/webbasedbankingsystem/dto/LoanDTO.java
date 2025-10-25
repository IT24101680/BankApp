package com.banksystem.webbasedbankingsystem.dto;

import java.time.LocalDateTime;
import java.util.List;

public class LoanDTO {
    private Long loanId;
    private UserDTO applicant;
    private Double amount;
    private String purpose;
    private String status;
    private LocalDateTime appliedAt;
    private LocalDateTime reviewedAt;
    private String adminComment;
    private List<LoanDocumentDTO> documents;

    public LoanDTO() {}

    public LoanDTO(Long loanId, UserDTO applicant, Double amount, String purpose, String status, LocalDateTime appliedAt, LocalDateTime reviewedAt, String adminComment, List<LoanDocumentDTO> documents) {
        this.loanId = loanId;
        this.applicant = applicant;
        this.amount = amount;
        this.purpose = purpose;
        this.status = status;
        this.appliedAt = appliedAt;
        this.reviewedAt = reviewedAt;
        this.adminComment = adminComment;
        this.documents = documents;
    }

    // Getters and Setters
    public Long getLoanId() { return loanId; }
    public void setLoanId(Long loanId) { this.loanId = loanId; }
    public UserDTO getApplicant() { return applicant; }
    public void setApplicant(UserDTO applicant) { this.applicant = applicant; }
    public Double getAmount() { return amount; }
    public void setAmount(Double amount) { this.amount = amount; }
    public String getPurpose() { return purpose; }
    public void setPurpose(String purpose) { this.purpose = purpose; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDateTime getAppliedAt() { return appliedAt; }
    public void setAppliedAt(LocalDateTime appliedAt) { this.appliedAt = appliedAt; }
    public LocalDateTime getReviewedAt() { return reviewedAt; }
    public void setReviewedAt(LocalDateTime reviewedAt) { this.reviewedAt = reviewedAt; }
    public String getAdminComment() { return adminComment; }
    public void setAdminComment(String adminComment) { this.adminComment = adminComment; }
    public List<LoanDocumentDTO> getDocuments() { return documents; }
    public void setDocuments(List<LoanDocumentDTO> documents) { this.documents = documents; }
}
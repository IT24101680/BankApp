package com.banksystem.webbasedbankingsystem.controller;

import com.banksystem.webbasedbankingsystem.dto.LoanDTO;
import com.banksystem.webbasedbankingsystem.dto.LoanDocumentDTO;
import com.banksystem.webbasedbankingsystem.entity.Loan;
import com.banksystem.webbasedbankingsystem.entity.LoanDocument;
import com.banksystem.webbasedbankingsystem.entity.User;
import com.banksystem.webbasedbankingsystem.mapper.LoanMapper;
import com.banksystem.webbasedbankingsystem.repository.LoanDocumentRepository;
import com.banksystem.webbasedbankingsystem.service.LoanService;
import com.banksystem.webbasedbankingsystem.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/loans")
@CrossOrigin(originPatterns = "*", allowCredentials = "true")
public class LoanController {
    @Autowired
    private LoanService loanService;
    @Autowired
    private UserService userService;

    @Autowired
    private LoanDocumentRepository loanDocumentRepository;


    // Customer applies for a loan (step 1: create loan)
    @PostMapping("/apply")
    public ResponseEntity<?> applyLoan(@RequestBody Map<String, Object> payload, HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null || !"CUSTOMER".equals(user.getRole())) {
            return ResponseEntity.status(403).body(Map.of("success", false, "message", "Only customers can apply for loans."));
        }
        Double amount = Double.valueOf(payload.get("amount").toString());
        String purpose = payload.get("purpose").toString();
        Loan loan = loanService.applyLoan(user.getUserId(), amount, purpose);
        return ResponseEntity.ok(Map.of("success", true, "loanId", loan.getLoanId()));
    }

    // Customer uploads documents for a loan
    @PostMapping("/{loanId}/documents")
    public ResponseEntity<?> uploadDocument(
            @PathVariable Long loanId,
            @RequestParam("category") String category,
            @RequestParam("file") MultipartFile file,
            HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null || !"CUSTOMER".equals(user.getRole())) {
            return ResponseEntity.status(403).body(Map.of("success", false, "message", "Only customers can upload documents."));
        }
        try {
            loanService.saveLoanDocument(loanId, category, file);
            return ResponseEntity.ok(Map.of("success", true, "message", "Document uploaded"));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    // Get documents for a loan (admin or owner)
    @GetMapping("/{loanId}/documents")
    public ResponseEntity<?> getDocuments(@PathVariable Long loanId, HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return ResponseEntity.status(403).body(Map.of("success", false, "message", "Login required."));
        }
        List<LoanDocument> docs = loanService.getLoanDocuments(loanId);
        List<LoanDocumentDTO> docDTOs = docs.stream()
                .map(LoanMapper::toLoanDocumentDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(docDTOs);
    }

    // Customer views their loans
    @GetMapping("/my")
    public ResponseEntity<?> myLoans(HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null || !"CUSTOMER".equals(user.getRole())) {
            return ResponseEntity.status(403).body(Map.of("success", false, "message", "Only customers can view their loans."));
        }
        List<Loan> loans = loanService.getLoansByUser(user.getUserId());
        List<LoanDTO> loanDTOs = loans.stream()
                .map(LoanMapper::toLoanDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(loanDTOs);
    }

    // Admin views all loans
    @GetMapping("/all")
    public ResponseEntity<?> allLoans(HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null || !"ADMIN".equals(user.getRole())) {
            return ResponseEntity.status(403).body(Map.of("success", false, "message", "Admin access required."));
        }
        List<Loan> loans = loanService.getAllLoans();
        List<LoanDTO> loanDTOs = loans.stream()
                .map(LoanMapper::toLoanDTO)
                .toList();
        return ResponseEntity.ok(loanDTOs);
    }

    // Admin reviews a loan


    @PutMapping("/{loanId}/review")
    public ResponseEntity<?> reviewLoan(@PathVariable Long loanId, @RequestBody Map<String, String> payload, HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null || !"ADMIN".equals(user.getRole())) {
            return ResponseEntity.status(403).body(Map.of("success", false, "message", "Admin access required."));
        }
        String status = payload.get("status"); // "APPROVED", "REJECTED", "UNDER_REVIEW"
        String comment = payload.getOrDefault("comment", "");
        Loan loan = loanService.reviewLoan(loanId, status, comment);
        return ResponseEntity.ok(Map.of("success", true, "loan", LoanMapper.toLoanDTO(loan)));
    }

    @GetMapping("/documents/{docId}/download")
    public ResponseEntity<byte[]> downloadDocument(@PathVariable Long docId) {
        LoanDocument doc = loanDocumentRepository.findById(docId)
                .orElseThrow(() -> new RuntimeException("Document not found"));
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + doc.getFileName() + "\"")
                .contentType(MediaType.parseMediaType(doc.getFileType()))
                .body(doc.getData());
    }

    @DeleteMapping("/{loanId}")
    public ResponseEntity<?> deleteLoan(@PathVariable Long loanId, HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null || !"ADMIN".equals(user.getRole())) {
            return ResponseEntity.status(403).body(Map.of("success", false, "message", "Admin access required."));
        }
        try {
            loanService.deleteLoan(loanId);
            return ResponseEntity.ok(Map.of("success", true, "message", "Loan application deleted successfully."));
        } catch (Exception e) {
            return ResponseEntity.status(400).body(Map.of("success", false, "message", e.getMessage()));
        }
    }


}
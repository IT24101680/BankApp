package com.banksystem.webbasedbankingsystem.mapper;

import com.banksystem.webbasedbankingsystem.entity.Loan;
import com.banksystem.webbasedbankingsystem.entity.LoanDocument;
import com.banksystem.webbasedbankingsystem.entity.User;
import com.banksystem.webbasedbankingsystem.dto.*;

import java.util.List;
import java.util.stream.Collectors;

public class LoanMapper {

    public static UserDTO toUserDTO(User user) {
        if (user == null) return null;
        return new UserDTO(
                user.getUserId(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                user.getUsername(),
                user.getPhoneNumber(),
                user.getAddress(),
                user.getAccountNumber()
        );
    }

    public static LoanDocumentDTO toLoanDocumentDTO(LoanDocument doc) {
        if (doc == null) return null;
        return new LoanDocumentDTO(
                doc.getId(),
                doc.getCategory(),
                doc.getFileName(),
                doc.getFileType()
        );
    }

    public static LoanDTO toLoanDTO(Loan loan) {
        if (loan == null) return null;
        UserDTO applicantDTO = toUserDTO(loan.getApplicant());
        List<LoanDocumentDTO> docDTOs = loan.getDocuments() != null
                ? loan.getDocuments().stream().map(LoanMapper::toLoanDocumentDTO).collect(Collectors.toList())
                : null;
        return new LoanDTO(
                loan.getLoanId(),
                applicantDTO,
                loan.getAmount(),
                loan.getPurpose(),
                loan.getStatus(),
                loan.getAppliedAt(),
                loan.getReviewedAt(),
                loan.getAdminComment(),
                docDTOs
        );
    }
}
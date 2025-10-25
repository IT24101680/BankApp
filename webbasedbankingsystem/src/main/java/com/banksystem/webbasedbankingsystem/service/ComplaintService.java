package com.banksystem.webbasedbankingsystem.service;

import com.banksystem.webbasedbankingsystem.entity.Complaint;
import com.banksystem.webbasedbankingsystem.entity.User;
import com.banksystem.webbasedbankingsystem.repository.ComplaintRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class ComplaintService {
    @Autowired
    private ComplaintRepository complaintRepository;

    // Submit a new complaint
    public Complaint submitComplaint(String subject, String message, User currentUser) {
        // Optional: Validate user via UserService if needed
        // userService.getUserById(currentUser.getUserId()); // Throws if invalid
        Complaint complaint = new Complaint(subject, message, currentUser);
        return complaintRepository.save(complaint);
    }

    // Get user's complaints
    public List<Complaint> getUserComplaints(Long userId) {
        return complaintRepository.findByUserUserIdOrderBySubmittedAtDesc(userId);
    }

    // Get all pending complaints (for admin)
    public List<Complaint> getPendingComplaints() {
        return complaintRepository.findByStatusOrderBySubmittedAtDesc("PENDING");
    }

    // Update complaint status/comment (for admin)
    public Complaint updateComplaint(Long id, String status, String adminComment) {
        Complaint complaint = complaintRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Complaint not found"));
        complaint.setStatus(status);
        complaint.setAdminComment(adminComment);
        return complaintRepository.save(complaint);
    }
}
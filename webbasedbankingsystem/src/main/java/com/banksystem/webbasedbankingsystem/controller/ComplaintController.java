package com.banksystem.webbasedbankingsystem.controller;

import com.banksystem.webbasedbankingsystem.entity.Complaint;
import com.banksystem.webbasedbankingsystem.entity.User;
import com.banksystem.webbasedbankingsystem.service.ComplaintService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin(originPatterns = "*", allowCredentials = "true")  // Matches your HomeController's CORS
public class ComplaintController {
    @Autowired
    private ComplaintService complaintService;

    // POST /api/complaints - Submit a new complaint
    @PostMapping("/complaints")
    public ResponseEntity<Map<String, Object>> submitComplaint(
            @RequestBody Map<String, String> request,
            HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        try {
            String subject = request.get("subject");
            String message = request.get("message");

            if (subject == null || message == null || subject.trim().isEmpty() || message.trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "Subject and message are required.");
                return ResponseEntity.badRequest().body(response);
            }

            // Retrieve current user from session (matches HomeController)
            User currentUser = (User) session.getAttribute("user");
            if (currentUser == null) {
                response.put("success", false);
                response.put("message", "User not authenticated. Please log in.");
                return ResponseEntity.status(401).body(response);
            }

            Complaint complaint = complaintService.submitComplaint(subject, message, currentUser);

            response.put("success", true);
            response.put("message", "Complaint submitted successfully!");
            response.put("complaintId", complaint.getId());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error submitting complaint: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    // GET /api/complaints/my - Get user's complaints (optional, for displaying history in frontend)
    @GetMapping("/complaints/my")
    public ResponseEntity<Map<String, Object>> getMyComplaints(HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        User currentUser = (User) session.getAttribute("user");
        if (currentUser == null) {
            response.put("success", false);
            response.put("message", "User not authenticated. Please log in.");
            return ResponseEntity.status(401).body(response);
        }
        List<Complaint> complaints = complaintService.getUserComplaints(currentUser.getUserId());
        response.put("success", true);
        response.put("complaints", complaints);
        return ResponseEntity.ok(response);
    }

    // GET /api/complaints/pending - For admin (optional; add to admin dashboard later)
    @GetMapping("/complaints/pending")
    public ResponseEntity<Map<String, Object>> getPendingComplaints(HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        User currentUser = (User) session.getAttribute("user");
        if (currentUser == null || !"ADMIN".equals(currentUser.getRole())) {
            response.put("success", false);
            response.put("message", "Admin access required.");
            return ResponseEntity.status(403).body(response);
        }
        List<Complaint> complaints = complaintService.getPendingComplaints();
        response.put("success", true);
        response.put("complaints", complaints);
        return ResponseEntity.ok(response);
    }

    // PUT /api/complaints/{id} - Update complaint (for admin, optional)
    @PutMapping("/complaints/{id}")
    public ResponseEntity<Map<String, Object>> updateComplaint(
            @PathVariable Long id,
            @RequestBody Map<String, String> request,
            HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        try {
            User currentUser = (User) session.getAttribute("user");
            if (currentUser == null || !"ADMIN".equals(currentUser.getRole())) {
                response.put("success", false);
                response.put("message", "Admin access required.");
                return ResponseEntity.status(403).body(response);
            }

            String status = request.get("status");
            String adminComment = request.get("adminComment");

            if (status == null || status.trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "Status is required.");
                return ResponseEntity.badRequest().body(response);
            }

            Complaint updated = complaintService.updateComplaint(id, status, adminComment);
            response.put("success", true);
            response.put("message", "Complaint updated successfully!");
            response.put("updatedComplaint", updated);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error updating complaint: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
}
package com.banksystem.webbasedbankingsystem.controller;

import com.banksystem.webbasedbankingsystem.entity.User;
import com.banksystem.webbasedbankingsystem.entity.Transaction;
import com.banksystem.webbasedbankingsystem.service.UserService;
import com.banksystem.webbasedbankingsystem.service.TransactionService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(originPatterns = "*", allowCredentials = "true")
public class AdminController {

    @Autowired
    private UserService userService;

    @Autowired
    private TransactionService transactionService;

    // Middleware to check admin authentication
    private boolean isAdmin(HttpSession session) {
        User user = (User) session.getAttribute("user");
        return user != null && "ADMIN".equals(user.getRole());
    }

    @GetMapping("/users")
    public ResponseEntity<?> getAllUsers(HttpSession session) {
        if (!isAdmin(session)) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Access denied. Admin privileges required.");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
        }

        try {
            List<User> users = userService.getAllUsers();
            return ResponseEntity.ok(users);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error fetching users: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/users/{userId}")
    public ResponseEntity<?> getUserById(@PathVariable Long userId, HttpSession session) {
        if (!isAdmin(session)) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Access denied. Admin privileges required.");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
        }

        try {
            User user = userService.getUserById(userId);
            return ResponseEntity.ok(user);
        } catch (RuntimeException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }

    @GetMapping("/users/role/{role}")
    public ResponseEntity<?> getUsersByRole(@PathVariable String role, HttpSession session) {
        if (!isAdmin(session)) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Access denied. Admin privileges required.");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
        }

        try {
            List<User> users = userService.getUsersByRole(role.toUpperCase());
            return ResponseEntity.ok(users);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error fetching users: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @DeleteMapping("/users/{userId}")
    public ResponseEntity<Map<String, Object>> deleteUser(@PathVariable Long userId, HttpSession session) {
        Map<String, Object> response = new HashMap<>();

        if (!isAdmin(session)) {
            response.put("success", false);
            response.put("message", "Access denied. Admin privileges required.");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
        }

        User admin = (User) session.getAttribute("user");
        if (admin.getUserId().equals(userId)) {
            response.put("success", false);
            response.put("message", "You cannot delete your own account.");
            return ResponseEntity.badRequest().body(response);
        }

        try {
            userService.deleteUser(userId);
            response.put("success", true);
            response.put("message", "User deleted successfully");
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }

    @PutMapping("/users/{userId}")
    public ResponseEntity<Map<String, Object>> updateUser(
            @PathVariable Long userId,
            @RequestBody User updatedUser,
            HttpSession session) {

        Map<String, Object> response = new HashMap<>();

        if (!isAdmin(session)) {
            response.put("success", false);
            response.put("message", "Access denied. Admin privileges required.");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
        }

        try {
            User existingUser = userService.getUserById(userId);

            if (updatedUser.getFirstName() != null) {
                existingUser.setFirstName(updatedUser.getFirstName());
            }
            if (updatedUser.getLastName() != null) {
                existingUser.setLastName(updatedUser.getLastName());
            }
            if (updatedUser.getEmail() != null) {
                existingUser.setEmail(updatedUser.getEmail());
            }
            if (updatedUser.getPhoneNumber() != null) {
                existingUser.setPhoneNumber(updatedUser.getPhoneNumber());
            }
            if (updatedUser.getAddress() != null) {
                existingUser.setAddress(updatedUser.getAddress());
            }
            if (updatedUser.getRole() != null) {
                existingUser.setRole(updatedUser.getRole());
            }

            User savedUser = userService.updateUser(existingUser);

            response.put("success", true);
            response.put("message", "User updated successfully");
            response.put("user", savedUser);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PutMapping("/users/{userId}/balance")
    public ResponseEntity<Map<String, Object>> updateUserBalance(
            @PathVariable Long userId,
            @RequestParam Double amount,
            HttpSession session) {

        Map<String, Object> response = new HashMap<>();

        if (!isAdmin(session)) {
            response.put("success", false);
            response.put("message", "Access denied. Admin privileges required.");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
        }

        try {
            User user = userService.updateUserBalance(userId, amount);
            response.put("success", true);
            response.put("message", "Balance updated successfully");
            response.put("newBalance", user.getAccountBalance());
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/statistics")
    public ResponseEntity<?> getStatistics(HttpSession session) {
        if (!isAdmin(session)) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Access denied. Admin privileges required.");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
        }

        try {
            List<User> allUsers = userService.getAllUsers();
            List<User> customers = userService.getUsersByRole("CUSTOMER");
            List<User> employees = userService.getUsersByRole("EMPLOYEE");
            List<User> admins = userService.getUsersByRole("ADMIN");

            double totalBalance = customers.stream()
                    .mapToDouble(u -> u.getAccountBalance() != null ? u.getAccountBalance() : 0.0)
                    .sum();

            Map<String, Object> stats = new HashMap<>();
            stats.put("totalUsers", allUsers.size());
            stats.put("totalCustomers", customers.size());
            stats.put("totalEmployees", employees.size());
            stats.put("totalAdmins", admins.size());
            stats.put("totalBalance", totalBalance);
            stats.put("averageBalance", customers.isEmpty() ? 0 : totalBalance / customers.size());

            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error fetching statistics: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PostMapping("/users")
    public ResponseEntity<Map<String, Object>> createUser(
            @RequestBody User newUser,
            HttpSession session) {

        Map<String, Object> response = new HashMap<>();

        if (!isAdmin(session)) {
            response.put("success", false);
            response.put("message", "Access denied. Admin privileges required.");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
        }

        try {
            if (newUser.getRole() == null || newUser.getRole().isEmpty()) {
                newUser.setRole("CUSTOMER");
            }
            if ("CUSTOMER".equals(newUser.getRole()) && newUser.getAccountBalance() == null) {
                newUser.setAccountBalance(0.0);
            }

            User savedUser = userService.registerUser(newUser);

            response.put("success", true);
            response.put("message", "User created successfully");
            response.put("user", savedUser);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (RuntimeException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/search")
    public ResponseEntity<?> searchUsers(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String role,
            HttpSession session) {

        if (!isAdmin(session)) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Access denied. Admin privileges required.");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
        }

        try {
            List<User> users;

            if (role != null && !role.isEmpty()) {
                users = userService.getUsersByRole(role.toUpperCase());
            } else {
                users = userService.getAllUsers();
            }

            if (query != null && !query.isEmpty()) {
                String lowerQuery = query.toLowerCase();
                users = users.stream()
                        .filter(u ->
                                u.getUsername().toLowerCase().contains(lowerQuery) ||
                                        u.getEmail().toLowerCase().contains(lowerQuery) ||
                                        u.getFirstName().toLowerCase().contains(lowerQuery) ||
                                        u.getLastName().toLowerCase().contains(lowerQuery) ||
                                        (u.getAccountNumber() != null && u.getAccountNumber().toLowerCase().contains(lowerQuery))
                        )
                        .toList();
            }

            return ResponseEntity.ok(users);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error searching users: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // 🚀 NEW METHODS

    @GetMapping("/users/{userId}/transactions")
    public ResponseEntity<?> getUserTransactions(@PathVariable Long userId, HttpSession session) {
        if (!isAdmin(session)) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Access denied. Admin privileges required.");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
        }

        try {
            User user = userService.getUserById(userId);
            List<Transaction> transactions = transactionService.getUserTransactions(userId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("user", user);
            response.put("transactions", transactions);
            response.put("totalTransactions", transactions.size());

            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }

    @GetMapping("/users/{userId}/transactions/analytics")
    public ResponseEntity<?> getUserTransactionAnalytics(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "3") int months,
            HttpSession session) {
        if (!isAdmin(session)) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Access denied. Admin privileges required.");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
        }

        try {
            userService.getUserById(userId);
            Map<String, Object> analytics = transactionService.getAnalytics(userId, months);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("analytics", analytics);

            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }

    @GetMapping("/transactions/all")
    public ResponseEntity<?> getAllTransactions(
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String category,
            HttpSession session) {
        if (!isAdmin(session)) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Access denied. Admin privileges required.");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
        }

        try {
            List<User> users = userService.getAllUsers();
            List<Transaction> allTransactions = new ArrayList<>();

            for (User user : users) {
                List<Transaction> userTransactions = transactionService.getUserTransactions(user.getUserId());
                allTransactions.addAll(userTransactions);
            }

            if (type != null && !type.isEmpty()) {
                allTransactions = allTransactions.stream()
                        .filter(t -> t.getTransactionType().equals(type))
                        .collect(Collectors.toList());
            }

            if (category != null && !category.isEmpty()) {
                allTransactions = allTransactions.stream()
                        .filter(t -> category.equals(t.getCategory()))
                        .collect(Collectors.toList());
            }

            allTransactions.sort((t1, t2) -> t2.getCreatedAt().compareTo(t1.getCreatedAt()));

            return ResponseEntity.ok(allTransactions);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error fetching transactions: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}

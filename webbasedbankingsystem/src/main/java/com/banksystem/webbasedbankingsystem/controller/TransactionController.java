package com.banksystem.webbasedbankingsystem.controller;

import com.banksystem.webbasedbankingsystem.dto.TransactionDTO;
import com.banksystem.webbasedbankingsystem.entity.Transaction;
import com.banksystem.webbasedbankingsystem.entity.User;
import com.banksystem.webbasedbankingsystem.service.TransactionService;
import com.banksystem.webbasedbankingsystem.service.UserService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/transactions")
@CrossOrigin(originPatterns = "*", allowCredentials = "true")
public class TransactionController {

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private UserService userService;

    // Get all transactions for the logged-in user
    @GetMapping
    public ResponseEntity<List<Transaction>> getUserTransactions(HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        List<Transaction> transactions = transactionService.getUserTransactions(user.getUserId());
        return ResponseEntity.ok(transactions);
    }

    // Get transactions with filters
    @GetMapping("/filter")
    public ResponseEntity<List<Transaction>> filterTransactions(
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String method,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            HttpSession session) {

        User user = (User) session.getAttribute("user");
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        List<Transaction> transactions = transactionService.filterTransactions(
                user.getUserId(), type, category, method, startDate, endDate);

        return ResponseEntity.ok(transactions);
    }

    // Get transactions by date range
    @GetMapping("/range")
    public ResponseEntity<List<Transaction>> getTransactionsByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            HttpSession session) {

        User user = (User) session.getAttribute("user");
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        List<Transaction> transactions = transactionService.getTransactionsByDateRange(
                user.getUserId(), startDate, endDate);

        return ResponseEntity.ok(transactions);
    }

    // Get analytics
    @GetMapping("/analytics")
    public ResponseEntity<Map<String, Object>> getAnalytics(
            @RequestParam(defaultValue = "3") int months,
            HttpSession session) {

        User user = (User) session.getAttribute("user");
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Map<String, Object> analytics = transactionService.getAnalytics(user.getUserId(), months);
        return ResponseEntity.ok(analytics);
    }

    // Get spending by category
    @GetMapping("/analytics/spending-by-category")
    public ResponseEntity<Map<String, Double>> getSpendingByCategory(
            @RequestParam(defaultValue = "3") int months,
            HttpSession session) {

        User user = (User) session.getAttribute("user");
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Map<String, Double> spending = transactionService.getSpendingByCategory(user.getUserId(), months);
        return ResponseEntity.ok(spending);
    }

    // Get transactions by method
    @GetMapping("/analytics/by-method")
    public ResponseEntity<Map<String, Long>> getTransactionsByMethod(
            @RequestParam(defaultValue = "3") int months,
            HttpSession session) {

        User user = (User) session.getAttribute("user");
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Map<String, Long> methods = transactionService.getTransactionsByMethod(user.getUserId(), months);
        return ResponseEntity.ok(methods);
    }

    // Get transactions by type
    @GetMapping("/analytics/by-type")
    public ResponseEntity<Map<String, Long>> getTransactionsByType(
            @RequestParam(defaultValue = "3") int months,
            HttpSession session) {

        User user = (User) session.getAttribute("user");
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Map<String, Long> types = transactionService.getTransactionsByType(user.getUserId(), months);
        return ResponseEntity.ok(types);
    }

    // Get totals (credits, debits, net)
    @GetMapping("/analytics/totals")
    public ResponseEntity<Map<String, Double>> getTotals(
            @RequestParam(defaultValue = "3") int months,
            HttpSession session) {

        User user = (User) session.getAttribute("user");
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Map<String, Double> totals = transactionService.getTotals(user.getUserId(), months);
        return ResponseEntity.ok(totals);
    }

    // Get balance trend
    @GetMapping("/analytics/balance-trend")
    public ResponseEntity<List<Map<String, Object>>> getBalanceTrend(
            @RequestParam(defaultValue = "3") int months,
            HttpSession session) {

        User user = (User) session.getAttribute("user");
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        List<Map<String, Object>> trend = transactionService.getBalanceTrend(user.getUserId(), months);
        return ResponseEntity.ok(trend);
    }

    // Create a transaction (for testing purposes)
    @PostMapping
    public ResponseEntity<Map<String, Object>> createTransaction(
            @Valid @RequestBody TransactionDTO transactionDTO,
            BindingResult bindingResult,
            HttpSession session) {

        Map<String, Object> response = new HashMap<>();

        User user = (User) session.getAttribute("user");
        if (user == null) {
            response.put("success", false);
            response.put("message", "User not authenticated");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }

        if (bindingResult.hasErrors()) {
            response.put("success", false);
            response.put("message", "Invalid input data");
            return ResponseEntity.badRequest().body(response);
        }

        try {
            Double currentBalance = user.getAccountBalance();

            Transaction transaction = new Transaction();
            transaction.setUserId(user.getUserId());
            transaction.setAccountNumber(user.getAccountNumber());
            transaction.setTransactionType(transactionDTO.getTransactionType());
            transaction.setAmount(transactionDTO.getAmount());
            transaction.setBalanceBefore(currentBalance);
            transaction.setDescription(transactionDTO.getDescription());
            transaction.setCategory(transactionDTO.getCategory());
            transaction.setRecipientAccount(transactionDTO.getRecipientAccount());
            transaction.setRecipientName(transactionDTO.getRecipientName());
            transaction.setTransactionMethod(transactionDTO.getTransactionMethod());

            // Update balance based on transaction type
            Double newBalance = currentBalance;
            if (transactionDTO.getTransactionType().equals("DEPOSIT") ||
                    transactionDTO.getTransactionType().equals("TRANSFER_IN")) {
                newBalance += transactionDTO.getAmount();
            } else {
                newBalance -= transactionDTO.getAmount();
            }

            transaction.setBalanceAfter(newBalance);

            Transaction savedTransaction = transactionService.createTransaction(transaction);

            // Update user balance
            user.setAccountBalance(newBalance);
            userService.updateUser(user);
            session.setAttribute("user", user);

            response.put("success", true);
            response.put("message", "Transaction created successfully");
            response.put("transaction", savedTransaction);

            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error creating transaction: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}
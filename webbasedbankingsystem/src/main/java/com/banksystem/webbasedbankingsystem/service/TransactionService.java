package com.banksystem.webbasedbankingsystem.service;

import com.banksystem.webbasedbankingsystem.entity.Transaction;
import com.banksystem.webbasedbankingsystem.entity.User;
import com.banksystem.webbasedbankingsystem.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class TransactionService {

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private UserService userService;

    // Create a new transaction
    @Transactional
    public Transaction createTransaction(Transaction transaction) {
        return transactionRepository.save(transaction);
    }

    // Get all transactions for a user
    public List<Transaction> getUserTransactions(Long userId) {
        return transactionRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    // Get transactions by date range
    public List<Transaction> getTransactionsByDateRange(Long userId, LocalDateTime startDate, LocalDateTime endDate) {
        return transactionRepository.findByUserIdAndCreatedAtBetweenOrderByCreatedAtDesc(userId, startDate, endDate);
    }

    // Get transactions by type
    public List<Transaction> getTransactionsByType(Long userId, String type) {
        return transactionRepository.findByUserIdAndTransactionTypeOrderByCreatedAtDesc(userId, type);
    }

    // Get transactions by category
    public List<Transaction> getTransactionsByCategory(Long userId, String category) {
        return transactionRepository.findByUserIdAndCategoryOrderByCreatedAtDesc(userId, category);
    }

    // Get transactions by method
    public List<Transaction> getTransactionsByMethod(Long userId, String method) {
        return transactionRepository.findByUserIdAndTransactionMethodOrderByCreatedAtDesc(userId, method);
    }

    // Analytics: Get spending by category
    public Map<String, Double> getSpendingByCategory(Long userId, int months) {
        LocalDateTime startDate = LocalDateTime.now().minusMonths(months);
        List<Object[]> results = transactionRepository.findSpendingByCategory(userId, startDate);

        Map<String, Double> spending = new LinkedHashMap<>();
        for (Object[] result : results) {
            String category = result[0] != null ? (String) result[0] : "UNCATEGORIZED";
            Double amount = result[1] != null ? ((Number) result[1]).doubleValue() : 0.0;
            spending.put(category, amount);
        }
        return spending;
    }

    // Analytics: Get transactions by method
    public Map<String, Long> getTransactionsByMethod(Long userId, int months) {
        LocalDateTime startDate = LocalDateTime.now().minusMonths(months);
        List<Object[]> results = transactionRepository.findTransactionsByMethod(userId, startDate);

        Map<String, Long> methodCounts = new LinkedHashMap<>();
        for (Object[] result : results) {
            String method = result[0] != null ? (String) result[0] : "UNKNOWN";
            Long count = result[1] != null ? ((Number) result[1]).longValue() : 0L;
            methodCounts.put(method, count);
        }
        return methodCounts;
    }

    // Analytics: Get transactions by type
    public Map<String, Long> getTransactionsByType(Long userId, int months) {
        LocalDateTime startDate = LocalDateTime.now().minusMonths(months);
        List<Object[]> results = transactionRepository.findTransactionsByType(userId, startDate);

        Map<String, Long> typeCounts = new LinkedHashMap<>();
        for (Object[] result : results) {
            String type = result[0] != null ? (String) result[0] : "UNKNOWN";
            Long count = result[1] != null ? ((Number) result[1]).longValue() : 0L;
            typeCounts.put(type, count);
        }
        return typeCounts;
    }

    // Get total credits and debits
    public Map<String, Double> getTotals(Long userId, int months) {
        LocalDateTime startDate = LocalDateTime.now().minusMonths(months);

        Double credits = transactionRepository.getTotalCredits(userId, startDate);
        Double debits = transactionRepository.getTotalDebits(userId, startDate);

        Map<String, Double> totals = new HashMap<>();
        totals.put("credits", credits != null ? credits : 0.0);
        totals.put("debits", debits != null ? debits : 0.0);
        totals.put("net", (credits != null ? credits : 0.0) - (debits != null ? debits : 0.0));

        return totals;
    }

    // Get balance trend over time
    public List<Map<String, Object>> getBalanceTrend(Long userId, int months) {
        LocalDateTime startDate = LocalDateTime.now().minusMonths(months);
        List<Object[]> results = transactionRepository.getBalanceTrend(userId, startDate);

        List<Map<String, Object>> trend = new ArrayList<>();
        for (Object[] result : results) {
            Map<String, Object> point = new HashMap<>();
            point.put("date", result[0]);
            point.put("balance", result[1] != null ? ((Number) result[1]).doubleValue() : 0.0);
            trend.add(point);
        }

        return trend;
    }

    // Get comprehensive analytics
    public Map<String, Object> getAnalytics(Long userId, int months) {
        Map<String, Object> analytics = new HashMap<>();

        analytics.put("spendingByCategory", getSpendingByCategory(userId, months));
        analytics.put("transactionsByMethod", getTransactionsByMethod(userId, months));
        analytics.put("transactionsByType", getTransactionsByType(userId, months));
        analytics.put("totals", getTotals(userId, months));
        analytics.put("balanceTrend", getBalanceTrend(userId, months));

        // Get top spending category
        Map<String, Double> spending = getSpendingByCategory(userId, months);
        if (!spending.isEmpty()) {
            String topCategory = spending.entrySet().stream()
                    .max(Map.Entry.comparingByValue())
                    .map(Map.Entry::getKey)
                    .orElse("NONE");
            analytics.put("topSpendingCategory", topCategory);
        } else {
            analytics.put("topSpendingCategory", "NONE");
        }

        return analytics;
    }

    // Filter transactions by multiple criteria
    public List<Transaction> filterTransactions(Long userId, String type, String category,
                                                String method, LocalDateTime startDate,
                                                LocalDateTime endDate) {
        List<Transaction> transactions = getUserTransactions(userId);

        return transactions.stream()
                .filter(t -> type == null || type.isEmpty() || t.getTransactionType().equals(type))
                .filter(t -> category == null || category.isEmpty() ||
                        (t.getCategory() != null && t.getCategory().equals(category)))
                .filter(t -> method == null || method.isEmpty() ||
                        (t.getTransactionMethod() != null && t.getTransactionMethod().equals(method)))
                .filter(t -> startDate == null || t.getCreatedAt().isAfter(startDate) || t.getCreatedAt().isEqual(startDate))
                .filter(t -> endDate == null || t.getCreatedAt().isBefore(endDate) || t.getCreatedAt().isEqual(endDate))
                .collect(Collectors.toList());
    }
}
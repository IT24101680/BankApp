package com.banksystem.webbasedbankingsystem.repository;

import com.banksystem.webbasedbankingsystem.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    // Find all transactions for a user
    List<Transaction> findByUserIdOrderByCreatedAtDesc(Long userId);

    // Find transactions by account number
    List<Transaction> findByAccountNumberOrderByCreatedAtDesc(String accountNumber);

    // Find transactions by user and date range
    List<Transaction> findByUserIdAndCreatedAtBetweenOrderByCreatedAtDesc(
            Long userId, LocalDateTime startDate, LocalDateTime endDate);

    // Find transactions by type
    List<Transaction> findByUserIdAndTransactionTypeOrderByCreatedAtDesc(
            Long userId, String transactionType);

    // Find transactions by category
    List<Transaction> findByUserIdAndCategoryOrderByCreatedAtDesc(
            Long userId, String category);

    // Find transactions by method
    List<Transaction> findByUserIdAndTransactionMethodOrderByCreatedAtDesc(
            Long userId, String transactionMethod);

    // Custom queries for analytics
    @Query("SELECT t.category, SUM(t.amount) FROM Transaction t " +
            "WHERE t.userId = :userId AND t.transactionType IN ('WITHDRAWAL', 'TRANSFER_OUT', 'ONLINE_PAYMENT', 'BILL_PAYMENT', 'ATM_WITHDRAWAL') " +
            "AND t.createdAt >= :startDate " +
            "GROUP BY t.category ORDER BY SUM(t.amount) DESC")
    List<Object[]> findSpendingByCategory(@Param("userId") Long userId, @Param("startDate") LocalDateTime startDate);

    @Query("SELECT t.transactionMethod, COUNT(t) FROM Transaction t " +
            "WHERE t.userId = :userId AND t.createdAt >= :startDate " +
            "GROUP BY t.transactionMethod")
    List<Object[]> findTransactionsByMethod(@Param("userId") Long userId, @Param("startDate") LocalDateTime startDate);

    @Query("SELECT t.transactionType, COUNT(t) FROM Transaction t " +
            "WHERE t.userId = :userId AND t.createdAt >= :startDate " +
            "GROUP BY t.transactionType")
    List<Object[]> findTransactionsByType(@Param("userId") Long userId, @Param("startDate") LocalDateTime startDate);

    @Query("SELECT SUM(t.amount) FROM Transaction t " +
            "WHERE t.userId = :userId AND t.transactionType IN ('DEPOSIT', 'TRANSFER_IN') " +
            "AND t.createdAt >= :startDate")
    Double getTotalCredits(@Param("userId") Long userId, @Param("startDate") LocalDateTime startDate);

    @Query("SELECT SUM(t.amount) FROM Transaction t " +
            "WHERE t.userId = :userId AND t.transactionType IN ('WITHDRAWAL', 'TRANSFER_OUT', 'ONLINE_PAYMENT', 'BILL_PAYMENT', 'ATM_WITHDRAWAL') " +
            "AND t.createdAt >= :startDate")
    Double getTotalDebits(@Param("userId") Long userId, @Param("startDate") LocalDateTime startDate);

    // Get monthly balance trend
    @Query("SELECT DATE_FORMAT(t.createdAt, '%Y-%m-%d'), t.balanceAfter FROM Transaction t " +
            "WHERE t.userId = :userId AND t.createdAt >= :startDate " +
            "ORDER BY t.createdAt ASC")
    List<Object[]> getBalanceTrend(@Param("userId") Long userId, @Param("startDate") LocalDateTime startDate);
}
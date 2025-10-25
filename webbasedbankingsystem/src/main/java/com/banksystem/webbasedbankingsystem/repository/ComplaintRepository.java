package com.banksystem.webbasedbankingsystem.repository;

import com.banksystem.webbasedbankingsystem.entity.Complaint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ComplaintRepository extends JpaRepository<Complaint, Long> {
    List<Complaint> findByUserUserIdOrderBySubmittedAtDesc(Long userId);

    List<Complaint> findByStatusOrderBySubmittedAtDesc(String status);
}
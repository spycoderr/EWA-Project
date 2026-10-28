package com.example.expenseapproval.repository;

import com.example.expenseapproval.model.ApprovalAction;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ApprovalActionRepository extends JpaRepository<ApprovalAction, Long> {
    List<ApprovalAction> findByRequestIdOrderByActedAtAscIdAsc(Long requestId);
}

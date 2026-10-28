package com.example.expenseapproval.repository;

import com.example.expenseapproval.model.ApprovalRule;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface ApprovalRuleRepository extends JpaRepository<ApprovalRule, Long> {
    Optional<ApprovalRule> findByCompanyId(Long companyId);
}

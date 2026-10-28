package com.example.expenseapproval.repository;

import com.example.expenseapproval.model.ExpenseRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ExpenseRequestRepository extends JpaRepository<ExpenseRequest, Long> {
    List<ExpenseRequest> findByCompanyIdOrderByCreatedAtDescIdDesc(Long companyId);
    List<ExpenseRequest> findByCompanyIdAndCreatedByIdOrderByCreatedAtDescIdDesc(Long companyId, Long userId);
    Optional<ExpenseRequest> findByIdAndCompanyId(Long id, Long companyId);

    // Lock this row until the decision and history are saved, preventing two clicks from reviewing the same step.
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select r from ExpenseRequest r where r.id = :id and r.company.id = :companyId")
    Optional<ExpenseRequest> findForReview(@Param("id") Long id, @Param("companyId") Long companyId);
}

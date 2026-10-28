package com.example.expenseapproval.dto;

import com.example.expenseapproval.model.ExpenseRequest;
import com.example.expenseapproval.model.RequestStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ExpenseResponseDto(Long id, String title, String description, BigDecimal amount,
        RequestStatus status, boolean requiresFinance, String createdByName,
        LocalDateTime createdAt, LocalDateTime updatedAt) {
    public static ExpenseResponseDto from(ExpenseRequest request) {
        return new ExpenseResponseDto(request.getId(), request.getTitle(), request.getDescription(),
                request.getAmount(), request.getStatus(), request.isRequiresFinance(),
                request.getCreatedBy().getName(), request.getCreatedAt(), request.getUpdatedAt());
    }
}

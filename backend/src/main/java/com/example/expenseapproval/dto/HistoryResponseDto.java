package com.example.expenseapproval.dto;

import com.example.expenseapproval.model.ApprovalAction;
import com.example.expenseapproval.model.ApprovalActionType;
import java.time.LocalDateTime;

public record HistoryResponseDto(ApprovalActionType action, String actorName, String comment, LocalDateTime actedAt) {
    public static HistoryResponseDto from(ApprovalAction action) {
        return new HistoryResponseDto(action.getAction(), action.getActedBy().getName(),
                action.getComment(), action.getActedAt());
    }
}

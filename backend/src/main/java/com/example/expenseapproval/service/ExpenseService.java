package com.example.expenseapproval.service;

import com.example.expenseapproval.dto.*;
import com.example.expenseapproval.exception.ApiException;
import com.example.expenseapproval.model.*;
import com.example.expenseapproval.repository.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ExpenseService {
    private final ExpenseRequestRepository requests;
    private final ApprovalRuleRepository rules;
    private final ApprovalActionRepository actions;

    public ExpenseService(ExpenseRequestRepository requests, ApprovalRuleRepository rules,
                          ApprovalActionRepository actions) {
        this.requests = requests;
        this.rules = rules;
        this.actions = actions;
    }

    @Transactional
    public ExpenseResponseDto createRequest(AppUser user, CreateExpenseRequestDto body) {
        AuthService.requireRole(user, Role.EMPLOYEE);
        ApprovalRule rule = rules.findByCompanyId(user.getCompany().getId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Company approval rule not found."));
        boolean requiresFinance = body.amount().compareTo(rule.getFinanceThreshold()) > 0;
        LocalDateTime now = LocalDateTime.now();
        ExpenseRequest request = requests.save(new ExpenseRequest(user.getCompany(), user,
                body.title().trim(), body.description() == null ? "" : body.description().trim(),
                body.amount(), RequestStatus.PENDING_MANAGER, requiresFinance, now, now));
        actions.save(new ApprovalAction(request, user, ApprovalActionType.SUBMITTED, "", now));
        return ExpenseResponseDto.from(request);
    }

    @Transactional(readOnly = true)
    public List<ExpenseResponseDto> listVisibleRequests(AppUser user) {
        List<ExpenseRequest> visible;
        if (user.getRole() == Role.EMPLOYEE) {
            visible = requests.findByCompanyIdAndCreatedByIdOrderByCreatedAtDescIdDesc(
                    user.getCompany().getId(), user.getId());
        } else {
            visible = requests.findByCompanyIdOrderByCreatedAtDescIdDesc(user.getCompany().getId());
        }

        List<ExpenseResponseDto> result = new ArrayList<>();
        for (ExpenseRequest request : visible) {
            result.add(ExpenseResponseDto.from(request));
        }
        return result;
    }

    @Transactional(readOnly = true)
    public ExpenseResponseDto getRequest(AppUser user, Long id) {
        return ExpenseResponseDto.from(visibleRequest(user, id));
    }

    @Transactional(readOnly = true)
    public List<HistoryResponseDto> getHistory(AppUser user, Long id) {
        visibleRequest(user, id);
        List<ApprovalAction> history = actions.findByRequestIdOrderByActedAtAscIdAsc(id);
        List<HistoryResponseDto> result = new ArrayList<>();
        for (ApprovalAction action : history) {
            result.add(HistoryResponseDto.from(action));
        }
        return result;
    }

    @Transactional
    public ExpenseResponseDto approve(AppUser user, Long id, String comment) {
        return review(user, id, comment, true);
    }

    @Transactional
    public ExpenseResponseDto reject(AppUser user, Long id, String comment) {
        if (comment == null || comment.isBlank()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "A rejection reason is required.");
        }
        return review(user, id, comment, false);
    }

    private ExpenseResponseDto review(AppUser user, Long id, String comment, boolean approved) {
        if (user.getRole() != Role.MANAGER && user.getRole() != Role.FINANCE) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Only the correct reviewer can approve or reject requests.");
        }
        ExpenseRequest request = requests.findForReview(id, user.getCompany().getId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Request not found."));
        if (request.getStatus() == RequestStatus.APPROVED || request.getStatus() == RequestStatus.REJECTED) {
            throw new ApiException(HttpStatus.CONFLICT, "This request is already finalized.");
        }

        ApprovalActionType action;
        RequestStatus nextStatus;
        if (user.getRole() == Role.MANAGER) {
            if (request.getStatus() != RequestStatus.PENDING_MANAGER) {
                throw new ApiException(HttpStatus.CONFLICT, "This request is not waiting for manager approval.");
            }
            if (!approved) {
                nextStatus = RequestStatus.REJECTED;
                action = ApprovalActionType.MANAGER_REJECTED;
            } else {
                if (request.isRequiresFinance()) {
                    nextStatus = RequestStatus.PENDING_FINANCE;
                } else {
                    nextStatus = RequestStatus.APPROVED;
                }
                action = ApprovalActionType.MANAGER_APPROVED;
            }
        } else {
            if (request.getStatus() != RequestStatus.PENDING_FINANCE) {
                throw new ApiException(HttpStatus.CONFLICT, "This request is not waiting for finance approval.");
            }
            if (approved) {
                nextStatus = RequestStatus.APPROVED;
                action = ApprovalActionType.FINANCE_APPROVED;
            } else {
                nextStatus = RequestStatus.REJECTED;
                action = ApprovalActionType.FINANCE_REJECTED;
            }
        }
        request.updateStatus(nextStatus);
        requests.save(request);
        actions.save(new ApprovalAction(request, user, action, comment == null ? "" : comment.trim(), LocalDateTime.now()));
        return ExpenseResponseDto.from(request);
    }

    private ExpenseRequest visibleRequest(AppUser user, Long id) {
        ExpenseRequest request = requests.findByIdAndCompanyId(id, user.getCompany().getId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Request not found."));
        if (user.getRole() == Role.EMPLOYEE && !request.getCreatedBy().getId().equals(user.getId())) {
            throw new ApiException(HttpStatus.NOT_FOUND, "Request not found.");
        }
        return request;
    }
}

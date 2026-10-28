package com.example.expenseapproval.service;

import com.example.expenseapproval.dto.RuleResponseDto;
import com.example.expenseapproval.dto.UpdateRuleRequestDto;
import com.example.expenseapproval.exception.ApiException;
import com.example.expenseapproval.model.*;
import com.example.expenseapproval.repository.ApprovalRuleRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RuleService {
    private final ApprovalRuleRepository rules;

    public RuleService(ApprovalRuleRepository rules) { this.rules = rules; }

    @Transactional(readOnly = true)
    public RuleResponseDto getRule(AppUser user) {
        ApprovalRule rule = ownRule(user);
        return new RuleResponseDto(user.getCompany().getName(), rule.getFinanceThreshold());
    }

    @Transactional
    public RuleResponseDto updateRule(AppUser user, UpdateRuleRequestDto body) {
        ApprovalRule rule = ownRule(user);
        rule.setFinanceThreshold(body.financeThreshold());
        rules.save(rule);
        return new RuleResponseDto(user.getCompany().getName(), rule.getFinanceThreshold());
    }

    private ApprovalRule ownRule(AppUser user) {
        AuthService.requireRole(user, Role.ADMIN);
        return rules.findByCompanyId(user.getCompany().getId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Company approval rule not found."));
    }
}

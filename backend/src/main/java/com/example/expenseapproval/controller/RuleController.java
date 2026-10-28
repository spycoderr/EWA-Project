package com.example.expenseapproval.controller;

import com.example.expenseapproval.dto.*;
import com.example.expenseapproval.service.AuthService;
import com.example.expenseapproval.service.RuleService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/rules")
public class RuleController {
    private final AuthService auth;
    private final RuleService rules;

    public RuleController(AuthService auth, RuleService rules) {
        this.auth = auth;
        this.rules = rules;
    }

    @GetMapping
    public RuleResponseDto get(HttpServletRequest request) {
        return rules.getRule(auth.currentUser(request.getSession(false)));
    }

    @PutMapping
    public RuleResponseDto update(@Valid @RequestBody UpdateRuleRequestDto body, HttpServletRequest request) {
        return rules.updateRule(auth.currentUser(request.getSession(false)), body);
    }
}

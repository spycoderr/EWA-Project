package com.example.expenseapproval.controller;

import com.example.expenseapproval.dto.*;
import com.example.expenseapproval.service.AuthService;
import com.example.expenseapproval.service.ExpenseService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/requests")
public class ExpenseController {
    private final AuthService auth;
    private final ExpenseService expenses;

    public ExpenseController(AuthService auth, ExpenseService expenses) {
        this.auth = auth;
        this.expenses = expenses;
    }

    @GetMapping
    public List<ExpenseResponseDto> list(HttpServletRequest request) {
        return expenses.listVisibleRequests(auth.currentUser(request.getSession(false)));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ExpenseResponseDto create(@Valid @RequestBody CreateExpenseRequestDto body, HttpServletRequest request) {
        return expenses.createRequest(auth.currentUser(request.getSession(false)), body);
    }

    @GetMapping("/{id}")
    public ExpenseResponseDto detail(@PathVariable Long id, HttpServletRequest request) {
        return expenses.getRequest(auth.currentUser(request.getSession(false)), id);
    }

    @GetMapping("/{id}/history")
    public List<HistoryResponseDto> history(@PathVariable Long id, HttpServletRequest request) {
        return expenses.getHistory(auth.currentUser(request.getSession(false)), id);
    }

    @PostMapping("/{id}/approve")
    public ExpenseResponseDto approve(@PathVariable Long id, @Valid @RequestBody(required = false) ReviewRequestDto body,
                                      HttpServletRequest request) {
        return expenses.approve(auth.currentUser(request.getSession(false)), id, body == null ? null : body.comment());
    }

    @PostMapping("/{id}/reject")
    public ExpenseResponseDto reject(@PathVariable Long id, @Valid @RequestBody ReviewRequestDto body,
                                     HttpServletRequest request) {
        return expenses.reject(auth.currentUser(request.getSession(false)), id, body.comment());
    }
}

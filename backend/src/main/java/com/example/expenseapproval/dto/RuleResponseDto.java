package com.example.expenseapproval.dto;

import java.math.BigDecimal;

public record RuleResponseDto(String companyName, BigDecimal financeThreshold) {}

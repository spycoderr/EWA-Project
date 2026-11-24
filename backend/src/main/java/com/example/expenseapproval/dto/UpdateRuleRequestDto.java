package com.example.expenseapproval.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public record UpdateRuleRequestDto(
        @NotNull(message = "Finance threshold is required.")
        @DecimalMin(value = "0.00", message = "Finance threshold cannot be negative.")
        @DecimalMax(value = "1000000.00", message = "Finance threshold cannot exceed INR 10,00,000.")
        @Digits(integer = 7, fraction = 2, message = "Finance threshold must be within the allowed limit with at most two decimal places.")
        BigDecimal financeThreshold) {}

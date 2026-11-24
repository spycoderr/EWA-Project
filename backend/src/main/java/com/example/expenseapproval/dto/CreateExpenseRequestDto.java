package com.example.expenseapproval.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public record CreateExpenseRequestDto(
        @NotBlank(message = "Title is required.")
        @Size(max = 120, message = "Title cannot exceed 120 characters.") String title,
        @Size(max = 500, message = "Description cannot exceed 500 characters.") String description,
        @NotNull(message = "Amount is required.")
        @DecimalMin(value = "0.01", message = "Amount must be at least INR 0.01.")
        @DecimalMax(value = "1000000.00", message = "Amount cannot exceed INR 10,00,000.")
        @Digits(integer = 7, fraction = 2, message = "Amount must be within the allowed limit with at most two decimal places.")
        BigDecimal amount) {}

package com.example.expenseapproval.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public record CreateExpenseRequestDto(
        @NotBlank @Size(max = 120) String title,
        @Size(max = 500) String description,
        @NotNull @DecimalMin("0.01") @DecimalMax("1000000.00")
        @Digits(integer = 7, fraction = 2) BigDecimal amount) {}

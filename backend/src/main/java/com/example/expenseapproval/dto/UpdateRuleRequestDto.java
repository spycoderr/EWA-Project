package com.example.expenseapproval.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public record UpdateRuleRequestDto(
        @NotNull @DecimalMin("0.00") @DecimalMax("1000000.00")
        @Digits(integer = 7, fraction = 2) BigDecimal financeThreshold) {}

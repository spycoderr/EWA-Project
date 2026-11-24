package com.example.expenseapproval.dto;

import jakarta.validation.constraints.Size;

public record ReviewRequestDto(
        @Size(max = 500, message = "Review comment cannot exceed 500 characters.") String comment) {}

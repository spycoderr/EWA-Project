package com.example.expenseapproval.dto;

import jakarta.validation.constraints.Size;

public record ReviewRequestDto(@Size(max = 500) String comment) {}

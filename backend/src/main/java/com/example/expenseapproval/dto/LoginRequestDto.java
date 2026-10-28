package com.example.expenseapproval.dto;

import jakarta.validation.constraints.*;

public record LoginRequestDto(
        @NotBlank @Email @Size(max = 150) String email,
        @NotBlank @Size(max = 100) String password) {}

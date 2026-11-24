package com.example.expenseapproval.dto;

import jakarta.validation.constraints.*;

public record LoginRequestDto(
        @NotBlank(message = "Email is required.")
        @Email(message = "Enter a valid email address.")
        @Size(max = 150, message = "Email cannot exceed 150 characters.") String email,
        @NotBlank(message = "Password is required.")
        @Size(max = 100, message = "Password cannot exceed 100 characters.") String password) {}

package com.example.expenseapproval.dto;

import com.example.expenseapproval.model.AppUser;
import com.example.expenseapproval.model.Role;

public record CurrentUserDto(Long id, String name, String email, Role role, String companyName) {
    public static CurrentUserDto from(AppUser user) {
        return new CurrentUserDto(user.getId(), user.getName(), user.getEmail(),
                user.getRole(), user.getCompany().getName());
    }
}

package com.example.expenseapproval.controller;

import com.example.expenseapproval.dto.*;
import com.example.expenseapproval.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService auth;

    public AuthController(AuthService auth) { this.auth = auth; }

    @PostMapping("/login")
    public CurrentUserDto login(@Valid @RequestBody LoginRequestDto body, HttpServletRequest request) {
        var user = auth.login(body.email(), body.password());
        HttpSession session = request.getSession();
        request.changeSessionId();
        session.setAttribute("USER_ID", user.getId());
        return CurrentUserDto.from(user);
    }

    @GetMapping("/me")
    public CurrentUserDto me(HttpServletRequest request) {
        return CurrentUserDto.from(auth.currentUser(request.getSession(false)));
    }

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(HttpServletRequest request) { auth.logout(request.getSession(false)); }
}

package com.example.expenseapproval;

import com.example.expenseapproval.controller.AuthController;
import com.example.expenseapproval.controller.ExpenseController;
import com.example.expenseapproval.exception.ApiExceptionHandler;
import com.example.expenseapproval.model.*;
import com.example.expenseapproval.repository.UserRepository;
import com.example.expenseapproval.service.*;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {
    @Mock UserRepository users;
    @Mock ExpenseService expenses;
    MockMvc mvc;
    AppUser user;
    static final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
    static final String hash = encoder.encode("Demo@123");

    @BeforeEach
    void setup() {
        var alpha = new Company("Company Alpha");
        user = new AppUser(alpha, "Alpha Employee", "alpha.employee@demo.com", hash, Role.EMPLOYEE);
        ReflectionTestUtils.setField(user, "id", 1L);
        AuthService auth = new AuthService(users, encoder);
        mvc = MockMvcBuilders.standaloneSetup(new AuthController(auth), new ExpenseController(auth, expenses))
                .setControllerAdvice(new ApiExceptionHandler()).build();
    }

    @Test
    void loginUsesSessionAndReturnsOnlySafeFields() throws Exception {
        when(users.findByEmailIgnoreCase("alpha.employee@demo.com")).thenReturn(Optional.of(user));
        var result = mvc.perform(post("/api/auth/login").contentType("application/json")
                .content("{\"email\":\"alpha.employee@demo.com\",\"password\":\"Demo@123\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.role").value("EMPLOYEE"))
                .andExpect(jsonPath("$.companyName").value("Company Alpha"))
                .andExpect(jsonPath("$.passwordHash").doesNotExist()).andReturn();
        var session = result.getRequest().getSession(false);
        assertEquals(1L, session.getAttribute("USER_ID"));
        assertEquals(1, java.util.Collections.list(session.getAttributeNames()).size());
    }

    @Test
    void wrongPasswordAndUnknownEmailHaveSameGeneric401() throws Exception {
        when(users.findByEmailIgnoreCase("alpha.employee@demo.com")).thenReturn(Optional.of(user));
        when(users.findByEmailIgnoreCase("unknown@demo.com")).thenReturn(Optional.empty());
        for (String email : new String[]{"alpha.employee@demo.com", "unknown@demo.com"}) {
            mvc.perform(post("/api/auth/login").contentType("application/json")
                    .content("{\"email\":\"" + email + "\",\"password\":\"wrong\"}"))
                    .andExpect(status().isUnauthorized()).andExpect(jsonPath("$.message").value("Invalid email or password."));
        }
    }

    @Test
    void unauthenticatedRequestAccessReturns401() throws Exception {
        mvc.perform(get("/api/requests")).andExpect(status().isUnauthorized());
        mvc.perform(post("/api/requests").contentType("application/json").content("{\"title\":\"Travel\",\"amount\":3000}"))
                .andExpect(status().isUnauthorized());
        verifyNoInteractions(expenses);
    }

    @Test
    void refreshUsesUserIdAndLogoutInvalidatesSession() throws Exception {
        var session = new MockHttpSession(); session.setAttribute("USER_ID", 1L);
        when(users.findById(1L)).thenReturn(Optional.of(user));
        mvc.perform(get("/api/auth/me").session(session)).andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Alpha Employee"));
        mvc.perform(post("/api/auth/logout").session(session)).andExpect(status().isNoContent());
        assertTrue(session.isInvalid());
    }
}

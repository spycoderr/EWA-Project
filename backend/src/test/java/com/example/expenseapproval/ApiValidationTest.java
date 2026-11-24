package com.example.expenseapproval;

import com.example.expenseapproval.controller.*;
import com.example.expenseapproval.exception.ApiExceptionHandler;
import com.example.expenseapproval.service.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class ApiValidationTest {
    @Mock AuthService auth;
    @Mock ExpenseService expenses;
    @Mock RuleService rules;
    MockMvc mvc;

    @BeforeEach
    void setup() {
        mvc = MockMvcBuilders.standaloneSetup(new ExpenseController(auth, expenses), new RuleController(auth, rules))
                .setControllerAdvice(new ApiExceptionHandler()).build();
    }

    private void expectBadInput(MockHttpServletRequestBuilder request, String body, String message) throws Exception {
        mvc.perform(request.contentType("application/json").content(body))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.message").value(message));
        verifyNoInteractions(auth, expenses, rules);
    }

    @Test
    void blankTitleHasReadableError() throws Exception {
        expectBadInput(post("/api/requests"), "{\"title\":\"   \",\"amount\":3000}", "Title is required.");
    }

    @Test
    void zeroAmountHasReadableError() throws Exception {
        expectBadInput(post("/api/requests"), "{\"title\":\"Travel\",\"amount\":0}", "Amount must be at least INR 0.01.");
    }

    @Test
    void extraDecimalPlacesAreRejected() throws Exception {
        expectBadInput(post("/api/requests"), "{\"title\":\"Travel\",\"amount\":12.345}",
                "Amount must be within the allowed limit with at most two decimal places.");
    }

    @Test
    void negativeThresholdHasReadableError() throws Exception {
        expectBadInput(put("/api/rules"), "{\"financeThreshold\":-1}", "Finance threshold cannot be negative.");
    }

    @Test
    void oversizedReviewCommentIsRejected() throws Exception {
        expectBadInput(post("/api/requests/1/approve"), "{\"comment\":\"" + "x".repeat(501) + "\"}",
                "Review comment cannot exceed 500 characters.");
    }

    @Test
    void malformedJsonHasReadableError() throws Exception {
        expectBadInput(post("/api/requests"), "{invalid", "Please provide valid input values.");
    }
}

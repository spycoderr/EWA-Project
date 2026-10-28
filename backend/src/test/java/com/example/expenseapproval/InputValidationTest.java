package com.example.expenseapproval;

import com.example.expenseapproval.dto.*;
import jakarta.validation.*;
import java.math.BigDecimal;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.*;
import static org.junit.jupiter.api.Assertions.*;

class InputValidationTest {
    static ValidatorFactory factory;
    static Validator validator;

    @BeforeAll static void setup() { factory = Validation.buildDefaultValidatorFactory(); validator = factory.getValidator(); }
    @AfterAll static void close() { factory.close(); }

    @ParameterizedTest
    @ValueSource(strings = {"0", "-1", "1000000.01", "0.001"})
    void invalidExpenseAmountsAreRejected(String amount) {
        assertFalse(validator.validate(new CreateExpenseRequestDto("Travel", "", new BigDecimal(amount))).isEmpty());
    }

    @Test
    void requiredFieldsAndLengthLimitsAreValidated() {
        assertFalse(validator.validate(new CreateExpenseRequestDto("   ", "", BigDecimal.ONE)).isEmpty());
        assertFalse(validator.validate(new CreateExpenseRequestDto("Title", "", null)).isEmpty());
        assertFalse(validator.validate(new CreateExpenseRequestDto("x".repeat(121), "", BigDecimal.ONE)).isEmpty());
        assertFalse(validator.validate(new CreateExpenseRequestDto("Title", "x".repeat(501), BigDecimal.ONE)).isEmpty());
        assertFalse(validator.validate(new ReviewRequestDto("x".repeat(501))).isEmpty());
    }

    @ParameterizedTest
    @ValueSource(strings = {"0.01", "1000000.00"})
    void validExpenseBoundaryAmountsAreAccepted(String amount) {
        assertTrue(validator.validate(new CreateExpenseRequestDto("Travel", "", new BigDecimal(amount))).isEmpty());
    }

    @ParameterizedTest
    @CsvSource({"0,true", "1000000.00,true", "-0.01,false", "1000000.01,false", "0.001,false"})
    void thresholdBoundsAreValidated(String amount, boolean valid) {
        assertEquals(valid, validator.validate(new UpdateRuleRequestDto(new BigDecimal(amount))).isEmpty());
    }
}

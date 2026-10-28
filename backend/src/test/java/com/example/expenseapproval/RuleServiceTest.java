package com.example.expenseapproval;

import com.example.expenseapproval.dto.UpdateRuleRequestDto;
import com.example.expenseapproval.exception.ApiException;
import com.example.expenseapproval.model.*;
import com.example.expenseapproval.repository.ApprovalRuleRepository;
import com.example.expenseapproval.service.RuleService;
import java.math.BigDecimal;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RuleServiceTest {
    @Mock ApprovalRuleRepository rules;

    private Company company(String name, long id) {
        var company = new Company(name);
        ReflectionTestUtils.setField(company, "id", id);
        return company;
    }

    @Test
    void adminUpdatesOnlyTheirCompanyRule() {
        var alpha = company("Company Alpha", 1L);
        var beta = company("Company Beta", 2L);
        var ownRule = new ApprovalRule(alpha, new BigDecimal("5000"));
        var otherRule = new ApprovalRule(beta, new BigDecimal("10000"));
        var admin = new AppUser(alpha, "Admin", "admin@demo.com", "hash", Role.ADMIN);
        when(rules.findByCompanyId(1L)).thenReturn(Optional.of(ownRule));
        var result = new RuleService(rules).updateRule(admin, new UpdateRuleRequestDto(new BigDecimal("12000")));
        assertEquals(new BigDecimal("12000"), result.financeThreshold());
        assertEquals(new BigDecimal("10000"), otherRule.getFinanceThreshold());
        verify(rules, never()).findByCompanyId(2L);
        verify(rules).save(ownRule);
    }

    @ParameterizedTest
    @EnumSource(value = Role.class, names = {"EMPLOYEE", "MANAGER", "FINANCE"})
    void nonAdminCannotReadOrUpdateRule(Role role) {
        var user = new AppUser(company("Company Alpha", 1L), "User", "user@demo.com", "hash", role);
        var service = new RuleService(rules);
        assertEquals(HttpStatus.FORBIDDEN, assertThrows(ApiException.class, () -> service.getRule(user)).getStatus());
        assertEquals(HttpStatus.FORBIDDEN, assertThrows(ApiException.class,
                () -> service.updateRule(user, new UpdateRuleRequestDto(BigDecimal.ZERO))).getStatus());
        verifyNoInteractions(rules);
    }
}

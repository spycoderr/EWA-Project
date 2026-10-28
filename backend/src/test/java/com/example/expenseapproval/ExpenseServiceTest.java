package com.example.expenseapproval;

import com.example.expenseapproval.dto.*;
import com.example.expenseapproval.exception.ApiException;
import com.example.expenseapproval.model.*;
import com.example.expenseapproval.repository.*;
import com.example.expenseapproval.service.ExpenseService;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.*;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExpenseServiceTest {
    @Mock ExpenseRequestRepository requests;
    @Mock ApprovalRuleRepository rules;
    @Mock ApprovalActionRepository actions;
    ExpenseService service;
    Company alpha;
    AppUser employee;
    AppUser manager;
    AppUser finance;

    @BeforeEach
    void setup() {
        service = new ExpenseService(requests, rules, actions);
        alpha = new Company("Company Alpha");
        ReflectionTestUtils.setField(alpha, "id", 1L);
        employee = user(Role.EMPLOYEE, 11L);
        manager = user(Role.MANAGER, 12L);
        finance = user(Role.FINANCE, 13L);
    }

    private AppUser user(Role role, long id) {
        AppUser user = new AppUser(alpha, role.name(), role + "@demo.com", "not-returned", role);
        ReflectionTestUtils.setField(user, "id", id);
        return user;
    }

    private ExpenseRequest pending(boolean financeRequired, RequestStatus status) {
        LocalDateTime now = LocalDateTime.now();
        ExpenseRequest request = new ExpenseRequest(alpha, employee, "Travel", "Visit",
                new BigDecimal(financeRequired ? "8000.00" : "3000.00"), status, financeRequired, now, now);
        ReflectionTestUtils.setField(request, "id", 100L);
        return request;
    }

    private void prepareCreate(ApprovalRule rule) {
        when(rules.findByCompanyId(1L)).thenReturn(Optional.of(rule));
        when(requests.save(any(ExpenseRequest.class))).thenAnswer(call -> {
            ExpenseRequest request = call.getArgument(0);
            ReflectionTestUtils.setField(request, "id", 100L);
            return request;
        });
    }

    private void assertError(HttpStatus status, org.junit.jupiter.api.function.Executable work) {
        assertEquals(status, assertThrows(ApiException.class, work).getStatus());
    }

    @ParameterizedTest
    @CsvSource({"3000.00,false", "5000.00,false", "8000.00,true"})
    void creationSnapshotsThresholdAndWritesSubmittedHistory(String amount, boolean expectedFinance) {
        prepareCreate(new ApprovalRule(alpha, new BigDecimal("5000")));
        var result = service.createRequest(employee, new CreateExpenseRequestDto(" Travel ", " Visit ", new BigDecimal(amount)));
        assertEquals(expectedFinance, result.requiresFinance());
        assertEquals(RequestStatus.PENDING_MANAGER, result.status());
        assertEquals("Travel", result.title());
        ArgumentCaptor<ApprovalAction> saved = ArgumentCaptor.forClass(ApprovalAction.class);
        verify(actions).save(saved.capture());
        assertEquals(ApprovalActionType.SUBMITTED, saved.getValue().getAction());
        assertSame(employee, saved.getValue().getActedBy());
        assertNotNull(saved.getValue().getActedAt());
    }

    @ParameterizedTest
    @CsvSource({"false,APPROVED", "true,PENDING_FINANCE"})
    void managerApprovalUsesSavedRoute(boolean financeRequired, RequestStatus expectedStatus) {
        var request = pending(financeRequired, RequestStatus.PENDING_MANAGER);
        when(requests.findForReview(100L, 1L)).thenReturn(Optional.of(request));
        assertEquals(expectedStatus, service.approve(manager, 100L, "Checked").status());
        ArgumentCaptor<ApprovalAction> saved = ArgumentCaptor.forClass(ApprovalAction.class);
        verify(actions).save(saved.capture());
        assertEquals(ApprovalActionType.MANAGER_APPROVED, saved.getValue().getAction());
        verifyNoInteractions(rules);
    }

    @Test
    void financeFinalizesAndRecordsActor() {
        when(requests.findForReview(100L, 1L)).thenReturn(Optional.of(pending(true, RequestStatus.PENDING_FINANCE)));
        assertEquals(RequestStatus.APPROVED, service.approve(finance, 100L, null).status());
        ArgumentCaptor<ApprovalAction> saved = ArgumentCaptor.forClass(ApprovalAction.class);
        verify(actions).save(saved.capture());
        assertEquals(ApprovalActionType.FINANCE_APPROVED, saved.getValue().getAction());
        assertSame(finance, saved.getValue().getActedBy());
        assertEquals("", saved.getValue().getComment());
    }

    @ParameterizedTest
    @CsvSource({"MANAGER,PENDING_MANAGER,MANAGER_REJECTED", "FINANCE,PENDING_FINANCE,FINANCE_REJECTED"})
    void eitherCorrectReviewerCanReject(Role role, RequestStatus stage, ApprovalActionType action) {
        when(requests.findForReview(100L, 1L)).thenReturn(Optional.of(pending(true, stage)));
        assertEquals(RequestStatus.REJECTED, service.reject(user(role, 14L), 100L, " Explain expense ").status());
        ArgumentCaptor<ApprovalAction> saved = ArgumentCaptor.forClass(ApprovalAction.class);
        verify(actions).save(saved.capture());
        assertEquals(action, saved.getValue().getAction());
        assertEquals("Explain expense", saved.getValue().getComment());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   "})
    void rejectionRequiresReason(String reason) {
        assertError(HttpStatus.BAD_REQUEST, () -> service.reject(manager, 100L, reason));
        verifyNoInteractions(requests, actions);
    }

    @ParameterizedTest
    @EnumSource(value = Role.class, names = {"EMPLOYEE", "ADMIN"})
    void nonReviewersCannotApproveOrReject(Role role) {
        var user = user(role, 15L);
        assertError(HttpStatus.FORBIDDEN, () -> service.approve(user, 100L, ""));
        assertError(HttpStatus.FORBIDDEN, () -> service.reject(user, 100L, "No"));
        verifyNoInteractions(requests, actions);
    }

    @ParameterizedTest
    @CsvSource({"MANAGER,PENDING_FINANCE", "FINANCE,PENDING_MANAGER"})
    void wrongStageCannotChangeStatus(Role role, RequestStatus stage) {
        when(requests.findForReview(100L, 1L)).thenReturn(Optional.of(pending(true, stage)));
        assertError(HttpStatus.CONFLICT, () -> service.approve(user(role, 15L), 100L, ""));
        assertError(HttpStatus.CONFLICT, () -> service.reject(user(role, 15L), 100L, "No"));
        verify(actions, never()).save(any());
        verify(requests, never()).save(any());
    }

    @ParameterizedTest
    @EnumSource(value = RequestStatus.class, names = {"APPROVED", "REJECTED"})
    void terminalRequestCannotBeChanged(RequestStatus status) {
        when(requests.findForReview(100L, 1L)).thenReturn(Optional.of(pending(true, status)));
        assertError(HttpStatus.CONFLICT, () -> service.approve(manager, 100L, ""));
        assertError(HttpStatus.CONFLICT, () -> service.reject(finance, 100L, "No"));
        verify(actions, never()).save(any());
    }

    @Test
    void anotherCompanyRequestIsNotVisibleOrReviewable() {
        when(requests.findByIdAndCompanyId(200L, 1L)).thenReturn(Optional.empty());
        when(requests.findForReview(200L, 1L)).thenReturn(Optional.empty());
        assertError(HttpStatus.NOT_FOUND, () -> service.getRequest(manager, 200L));
        assertError(HttpStatus.NOT_FOUND, () -> service.getHistory(manager, 200L));
        assertError(HttpStatus.NOT_FOUND, () -> service.approve(manager, 200L, ""));
        assertError(HttpStatus.NOT_FOUND, () -> service.reject(manager, 200L, "No"));
        verifyNoInteractions(actions);
    }

    @Test
    void employeeCannotReadAnotherEmployeesDetailOrHistory() {
        var anotherEmployee = user(Role.EMPLOYEE, 99L);
        when(requests.findByIdAndCompanyId(100L, 1L)).thenReturn(Optional.of(pending(false, RequestStatus.PENDING_MANAGER)));
        assertError(HttpStatus.NOT_FOUND, () -> service.getRequest(anotherEmployee, 100L));
        assertError(HttpStatus.NOT_FOUND, () -> service.getHistory(anotherEmployee, 100L));
        verifyNoInteractions(actions);
    }

    @Test
    void employeeListUsesBothCompanyAndSubmitter() {
        when(requests.findByCompanyIdAndCreatedByIdOrderByCreatedAtDescIdDesc(1L, 11L)).thenReturn(List.of());
        assertTrue(service.listVisibleRequests(employee).isEmpty());
        verify(requests, never()).findByCompanyIdOrderByCreatedAtDescIdDesc(anyLong());
    }

    @Test
    void thresholdChangeOnlyAffectsNewRequests() {
        ApprovalRule rule = new ApprovalRule(alpha, new BigDecimal("5000"));
        prepareCreate(rule);
        var body = new CreateExpenseRequestDto("Travel", "Visit", new BigDecimal("8000"));
        ExpenseResponseDto old = service.createRequest(employee, body);
        rule.setFinanceThreshold(new BigDecimal("10000"));
        ExpenseResponseDto fresh = service.createRequest(employee, body);
        assertTrue(old.requiresFinance());
        assertFalse(fresh.requiresFinance());
    }

    @ParameterizedTest
    @EnumSource(value = Role.class, names = {"MANAGER", "FINANCE", "ADMIN"})
    void onlyEmployeeCanCreate(Role role) {
        assertError(HttpStatus.FORBIDDEN, () -> service.createRequest(user(role, 20L),
                new CreateExpenseRequestDto("Travel", "", new BigDecimal("100"))));
        verifyNoInteractions(requests, rules, actions);
    }
}

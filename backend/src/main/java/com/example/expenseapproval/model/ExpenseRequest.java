package com.example.expenseapproval.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.hibernate.annotations.Check;

@Entity
@Table(name = "expense_requests")
@Check(constraints = "amount > 0 and amount <= 1000000")
public class ExpenseRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @ManyToOne(optional = false)
    @JoinColumn(name = "created_by_id", nullable = false)
    private AppUser createdBy;

    @Column(nullable = false, length = 120)
    private String title;

    @Column(length = 500)
    private String description;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30, columnDefinition = "varchar(30)")
    private RequestStatus status;

    @Column(name = "requires_finance", nullable = false, columnDefinition = "boolean")
    private boolean requiresFinance;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    protected ExpenseRequest() {}

    public ExpenseRequest(Company company, AppUser createdBy, String title, String description, BigDecimal amount, RequestStatus status, boolean requiresFinance, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.company = company;
        this.createdBy = createdBy;
        this.title = title;
        this.description = description;
        this.amount = amount;
        this.status = status;
        this.requiresFinance = requiresFinance;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Long getId() { return id; }
    public Company getCompany() { return company; }
    public AppUser getCreatedBy() { return createdBy; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public BigDecimal getAmount() { return amount; }
    public RequestStatus getStatus() { return status; }
    public boolean isRequiresFinance() { return requiresFinance; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }

    public void updateStatus(RequestStatus status) {
        this.status = status;
        this.updatedAt = LocalDateTime.now();
    }
}

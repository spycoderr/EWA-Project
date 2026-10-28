package com.example.expenseapproval.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import org.hibernate.annotations.Check;

@Entity
@Table(name = "approval_rules")
@Check(constraints = "finance_threshold >= 0 and finance_threshold <= 1000000")
public class ApprovalRule {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(optional = false)
    @JoinColumn(name = "company_id", nullable = false, unique = true)
    private Company company;

    @Column(name = "finance_threshold", nullable = false, precision = 12, scale = 2)
    private BigDecimal financeThreshold;

    protected ApprovalRule() {}

    public ApprovalRule(Company company, BigDecimal financeThreshold) {
        this.company = company;
        this.financeThreshold = financeThreshold;
    }

    public Long getId() { return id; }
    public Company getCompany() { return company; }
    public BigDecimal getFinanceThreshold() { return financeThreshold; }

    public void setFinanceThreshold(BigDecimal financeThreshold) {
        this.financeThreshold = financeThreshold;
    }
}

package com.example.expenseapproval.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "approval_actions")
public class ApprovalAction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "request_id", nullable = false)
    private ExpenseRequest request;

    @ManyToOne(optional = false)
    @JoinColumn(name = "acted_by_id", nullable = false)
    private AppUser actedBy;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40, columnDefinition = "varchar(40)")
    private ApprovalActionType action;

    @Column(length = 500)
    private String comment;

    @Column(name = "acted_at", nullable = false)
    private LocalDateTime actedAt;

    protected ApprovalAction() {}

    public ApprovalAction(ExpenseRequest request, AppUser actedBy, ApprovalActionType action, String comment, LocalDateTime actedAt) {
        this.request = request;
        this.actedBy = actedBy;
        this.action = action;
        this.comment = comment;
        this.actedAt = actedAt;
    }

    public Long getId() { return id; }
    public ExpenseRequest getRequest() { return request; }
    public AppUser getActedBy() { return actedBy; }
    public ApprovalActionType getAction() { return action; }
    public String getComment() { return comment; }
    public LocalDateTime getActedAt() { return actedAt; }
}

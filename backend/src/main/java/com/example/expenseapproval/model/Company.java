package com.example.expenseapproval.model;

import jakarta.persistence.*;

@Entity
@Table(name = "companies")
public class Company {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String name;

    protected Company() {}

    public Company(String name) {
        this.name = name;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
}

package com.example.expenseapproval.config;

import com.example.expenseapproval.model.*;
import com.example.expenseapproval.repository.*;
import java.math.BigDecimal;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class DemoDataSeeder implements CommandLineRunner {
    private final CompanyRepository companies;
    private final UserRepository users;
    private final ApprovalRuleRepository rules;
    private final BCryptPasswordEncoder passwordEncoder;

    public DemoDataSeeder(CompanyRepository companies, UserRepository users,
                          ApprovalRuleRepository rules, BCryptPasswordEncoder passwordEncoder) {
        this.companies = companies;
        this.users = users;
        this.rules = rules;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        seedCompany("Alpha", "5000.00");
        seedCompany("Beta", "10000.00");
    }

    private void seedCompany(String label, String threshold) {
        Company company = companies.findByName("Company " + label)
                .orElseGet(() -> companies.save(new Company("Company " + label)));
        if (rules.findByCompanyId(company.getId()).isEmpty()) {
            rules.save(new ApprovalRule(company, new BigDecimal(threshold)));
        }
        for (Role role : Role.values()) {
            String roleName = role.name().toLowerCase(java.util.Locale.ROOT);
            String email = label.toLowerCase(java.util.Locale.ROOT) + "." + roleName + "@demo.com";
            if (users.findByEmailIgnoreCase(email).isEmpty()) {
                String name = label + " " + roleName.substring(0, 1).toUpperCase() + roleName.substring(1);
                users.save(new AppUser(company, name, email, passwordEncoder.encode("Demo@123"), role));
            }
        }
    }
}

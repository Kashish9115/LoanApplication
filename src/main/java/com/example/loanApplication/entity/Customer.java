package com.example.loanApplication.entity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "Customers")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "CustomerId")
    private Long customerId;

    @Column(name = "FirstName", nullable = false, length = 50)
    private String firstName;

    @Column(name = "LastName", nullable = false, length = 50)
    private String lastName;
@Column(name = "Age")
private  Integer age;
    @Column(
            name = "Email",
            nullable = false,
            unique = true,
            length = 100
    )
    private String email;

    @Column(
            name = "Password",
            nullable = false,
            length = 255
    )
    private String password;

    @Column(name = "MobileNo", nullable = false, length = 15)
    private String mobileNo;

    @Column(name = "PanNo", length = 20)
    private String panNo;

    @Column(name = "AadhaarNo", nullable = false, length = 20)
    private String aadhaarNo;

    @Column(name = "EmploymentType", nullable = false, length = 50)
    private String employmentType;

    @Column(
            name = "MonthlyIncome",
            nullable = false,
            precision = 18,
            scale = 2
    )
    private BigDecimal monthlyIncome;

    @Column(
            name = "MonthlyInvestment",
            precision = 18,
            scale = 2
    )
    private BigDecimal monthlyInvestment;
    //

    @Column(name = "IsEmailVerified", nullable = false)
    private Boolean isEmailVerified = false;

    @Column(name = "CreatedAt", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
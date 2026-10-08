package com.example.loanApplication.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "Users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {
//
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "UserId")
    private Integer userId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "RoleId", nullable = false)
    private Role role;

    // RoleId = 1 (Customer), RoleId = 2 (Credit Officer)

    // Foreign Key link to Customers profile
    // NULL for Credit Officers
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "CustomerId",
            referencedColumnName = "CustomerId",
            nullable = true
    )
    private Customer customer;

    @Column(name = "FirstName", length = 50)
    private String firstName;

    @Column(name = "LastName", length = 50)
    private String lastName;

    @Column(
            name = "Email",
            nullable = false,
            unique = true,
            length = 100
    )
    private String email;

    @Column(name = "Mobile", length = 15)
    private String mobile;

    @Column(
            name = "Password",
            nullable = false,
            length = 255
    )
    private String password;

    // Centralized Authentication & Security Fields

    @Column(name = "RefreshToken", length = 255)
    private String refreshToken;

    @Column(name = "IsTokenRevoked", nullable = false)
    private Boolean isTokenRevoked = false;

    @Column(name = "SecretKey", length = 255)
    private String secretKey;

    // 16-character Base32 secret for Authenticator App TOTP 2FA

    @Column(name = "CreatedAt", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
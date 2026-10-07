package com.example.loanApplication.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "Roles")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "RoleId")
    private Integer roleId;

    @Column(
            name = "RoleName",
            nullable = false,
            unique = true,
            length = 50
    )
    private String roleName; // "Customer", "Credit Officer"

}
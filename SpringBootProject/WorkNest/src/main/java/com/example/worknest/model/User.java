package com.example.worknest.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "users") // avoid reserved word "user"
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class User {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(nullable = false)
    private String password; // plain for now

    @Column(nullable = false)
    private String role; // "ADMIN" / "USER"
}

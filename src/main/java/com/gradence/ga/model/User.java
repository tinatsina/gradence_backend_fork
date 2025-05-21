package com.gradence.ga.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "app_user")  // Rename table to avoid "user"
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(nullable = true)
    private String name;

    @Column(nullable = false)
    private String password; // Hashed password

    private String email;


}
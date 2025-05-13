package com.gradence.ga.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Entity
@Table(name = "question")
@Data
@NoArgsConstructor
@AllArgsConstructor

public class Question {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @NotBlank
    @Column(length = 255)
    private String prompt;
    @NotNull
    @NotBlank
    private String type;

    @Column(length = 1000)
    private String conditions;

    // ✅ ADD THIS:
    @Setter
    @Getter
    @ManyToOne
    @JoinColumn(name = "exam_id")
    private Exam exam;

    // (Optionally add other getters/setters if not using Lombok)
}
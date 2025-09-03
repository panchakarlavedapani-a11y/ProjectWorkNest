package com.example.worknest.model;

import java.time.LocalDate;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "task_assignments",
       uniqueConstraints = @UniqueConstraint(columnNames = {"task_id","assignee_id"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaskAssignment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "task_id")
    private Task task;

    @ManyToOne(optional = false)
    @JoinColumn(name = "assignee_id")
    private User assignee;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TaskStatus status;

    @Column(nullable = false)
    private LocalDate startDate;

    @Column(nullable = false)
    private LocalDate dueDate;

    // ensure default status is set when creating new assignments
    @PrePersist
    public void prePersist() {
        if (status == null) {
            status = TaskStatus.PENDING;
        }
    }
}

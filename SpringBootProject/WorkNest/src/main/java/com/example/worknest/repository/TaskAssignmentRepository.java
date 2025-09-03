package com.example.worknest.repository;

import com.example.worknest.model.Task;
import com.example.worknest.model.TaskAssignment;
import com.example.worknest.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TaskAssignmentRepository extends JpaRepository<TaskAssignment, Long> {

    List<TaskAssignment> findByAssignee(User assignee);

    List<TaskAssignment> findByTask(Task task);

    Optional<TaskAssignment> findByTask_IdAndAssignee_Id(Long taskId, Long userId);
}

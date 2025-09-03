package com.example.worknest.service;

import com.example.worknest.model.TaskAssignment;
import com.example.worknest.model.TaskStatus;
import com.example.worknest.model.User;
import com.example.worknest.repository.TaskAssignmentRepository;
import com.example.worknest.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class TaskAssignmentService {

    private final TaskAssignmentRepository assignmentRepo;
    private final UserRepository userRepo;

    /** find all assignments for a user */
    public List<TaskAssignment> findByAssignee(Long userId) {
        User u = userRepo.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        return assignmentRepo.findByAssignee(u);
    }

    /** get a specific assignment for a task + user */
    public TaskAssignment getByTaskAndAssignee(Long taskId, Long userId) {
        return assignmentRepo.findByTask_IdAndAssignee_Id(taskId, userId).orElse(null);
    }

    /** update status for this user’s assignment */
    public TaskAssignment updateStatus(Long taskId, Long userId, TaskStatus status) {
        TaskAssignment assignment = assignmentRepo.findByTask_IdAndAssignee_Id(taskId, userId)
                .orElseThrow(() -> new IllegalArgumentException("Assignment not found"));
        assignment.setStatus(status);
        return assignmentRepo.save(assignment);
    }
}

package com.example.worknest.service;

import com.example.worknest.model.Task;
import com.example.worknest.model.TaskPart;
import com.example.worknest.model.TaskStatus;
import com.example.worknest.model.User;
import com.example.worknest.repository.TaskPartRepository;
import com.example.worknest.repository.TaskRepository;
import com.example.worknest.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class TaskPartService {

    private final TaskPartRepository repo;
    private final TaskRepository taskRepo;
    private final UserRepository userRepo;

    public List<TaskPart> listByTask(Long taskId) {
        return repo.findByTask_Id(taskId);
    }

    public List<TaskPart> listByTaskAndUser(Long taskId, Long userId) {
        return repo.findByTask_IdAndUser_Id(taskId, userId);
    }

    public TaskPart create(Long taskId, Long userId, String description) {
        Task t = taskRepo.findById(taskId).orElseThrow();
        User u = userRepo.findById(userId).orElseThrow();
        TaskPart p = TaskPart.builder()
                .task(t)
                .user(u)
                .description(description)
                .status(TaskStatus.PENDING)
                .build();
        return repo.save(p);
    }

    public TaskPart updateStatus(Long partId, TaskStatus status) {
        TaskPart p = repo.findById(partId).orElseThrow();
        p.setStatus(status);
        return repo.save(p);
    }
}
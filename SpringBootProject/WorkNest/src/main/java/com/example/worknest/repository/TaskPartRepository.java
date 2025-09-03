package com.example.worknest.repository;

import com.example.worknest.model.TaskPart;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TaskPartRepository extends JpaRepository<TaskPart, Long> {
    List<TaskPart> findByTask_Id(Long taskId);
    List<TaskPart> findByTask_IdAndUser_Id(Long taskId, Long userId);
}
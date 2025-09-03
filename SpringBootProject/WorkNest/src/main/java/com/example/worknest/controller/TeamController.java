package com.example.worknest.controller;

import com.example.worknest.model.Task;
import com.example.worknest.model.TaskPart;
import com.example.worknest.model.TaskStatus;
import com.example.worknest.model.User;
import com.example.worknest.service.TaskCommentService;
import com.example.worknest.service.TaskPartService;
import com.example.worknest.service.TaskService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/user/team")
public class TeamController {

    private final TaskPartService partService;
    private final TaskService taskService;
    private final TaskCommentService commentService;

    private User current(HttpSession session) {
        return (User) session.getAttribute("loggedInUser");
    }

    @GetMapping("/{taskId}")
    public String teamPage(@PathVariable Long taskId, Model model, HttpSession session) {
        User me = current(session);
        if (me == null) return "redirect:/login";

        Task task = taskService.getById(taskId);
        // ensure user is part of the task assignees
        if (task.getAssignees().stream().noneMatch(u -> u.getId().equals(me.getId()))) {
            return "redirect:/user/dashboard";
        }

        List<TaskPart> parts = partService.listByTask(taskId);
        model.addAttribute("task", task);
        model.addAttribute("parts", parts);
        model.addAttribute("me", me);
        model.addAttribute("comments", commentService.listByTask(taskId));
        return "user-team";
    }

    @PostMapping("/{taskId}/parts")
    public String addPart(@PathVariable Long taskId,
                          @RequestParam String description,
                          HttpSession session) {
        User me = current(session);
        if (me == null) return "redirect:/login";
        partService.create(taskId, me.getId(), description);
        return "redirect:/user/team/" + taskId;
    }

    @PostMapping("/parts/{id}/status")
    public String updatePartStatus(@PathVariable Long id,
                                   @RequestParam TaskStatus status,
                                   @RequestParam Long taskId,
                                   HttpSession session) {
        User me = current(session);
        if (me == null) return "redirect:/login";
        partService.updateStatus(id, status);
        return "redirect:/user/team/" + taskId;
    }
}
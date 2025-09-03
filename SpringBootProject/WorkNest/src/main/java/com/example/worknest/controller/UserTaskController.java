package com.example.worknest.controller;

import com.example.worknest.model.Task;
import com.example.worknest.model.TaskStatus;
import com.example.worknest.model.User;
import com.example.worknest.service.TaskCommentService;
import com.example.worknest.service.TaskService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/user")
public class UserTaskController {

    private final TaskService taskService;
    private final TaskCommentService commentService;

    /** Helper: get logged-in user */
    private User current(HttpSession session) {
        return (User) session.getAttribute("loggedInUser");
    }

    /** User dashboard: list my tasks (via assignee link) */
    @GetMapping("/dashboard")
    public String dashboard(Model model, HttpSession session) {
        User me = current(session);
        if (me == null) return "redirect:/login";

        // ✅ tasks assigned to me
        List<Task> myTasks = taskService.findByAssignee(me.getId());

        long countPending    = myTasks.stream().filter(t -> t.getStatus() == TaskStatus.PENDING).count();
        long countInProgress = myTasks.stream().filter(t -> t.getStatus() == TaskStatus.IN_PROGRESS).count();
        long countCompleted  = myTasks.stream().filter(t -> t.getStatus() == TaskStatus.COMPLETED).count();
        long countDelayed    = myTasks.stream().filter(t ->
                t.getStatus() != TaskStatus.COMPLETED &&
                t.getDueDate() != null &&
                t.getDueDate().isBefore(LocalDate.now())
        ).count();

        model.addAttribute("me", me);
        model.addAttribute("tasks", myTasks);
        model.addAttribute("today", LocalDate.now());
        model.addAttribute("countPending", countPending);
        model.addAttribute("countInProgress", countInProgress);
        model.addAttribute("countCompleted", countCompleted);
        model.addAttribute("countDelayed", countDelayed);
        return "user-dashboard";
    }

    /** View a task I am assigned to */
    @GetMapping("/tasks/{id}")
    public String viewTask(@PathVariable Long id, Model model, HttpSession session) {
        User me = current(session);
        if (me == null) return "redirect:/login";

        // ✅ ensure I am one of the assignees
        Task task = taskService.getById(id);
        if (task.getAssignees().stream().noneMatch(u -> u.getId().equals(me.getId()))) {
            return "redirect:/user/dashboard";
        }

        model.addAttribute("task", task);
        model.addAttribute("comments", commentService.listByTask(id));
        model.addAttribute("today", LocalDate.now());
        return "user-task-details";
    }

    /** Update my task status */
    @PostMapping("/tasks/{id}/status")
    public String updateStatus(@PathVariable Long id,
                               @RequestParam TaskStatus status,
                               HttpSession session) {
        User me = current(session);
        if (me == null) return "redirect:/login";

        Task task = taskService.getById(id);
        if (task.getAssignees().stream().noneMatch(u -> u.getId().equals(me.getId()))) {
            return "redirect:/user/dashboard";
        }

        taskService.updateStatus(id, status);
        return "redirect:/user/tasks/" + id;
    }

    /** Add a comment on my task */
    @PostMapping("/tasks/{id}/comments")
    public String addComment(@PathVariable Long id,
                             @RequestParam String content,
                             HttpSession session) {
        User me = current(session);
        if (me == null) return "redirect:/login";

        Task task = taskService.getById(id);
        if (task.getAssignees().stream().noneMatch(u -> u.getId().equals(me.getId()))) {
            return "redirect:/user/dashboard";
        }

        commentService.add(id, me.getId(), content);
        return "redirect:/user/tasks/" + id;
    }
}

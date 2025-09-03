package com.example.worknest.controller;

import com.example.worknest.model.Task;
import com.example.worknest.model.TaskStatus;
import com.example.worknest.model.TaskComment;
import com.example.worknest.model.User;
import com.example.worknest.service.TaskCommentService;
import com.example.worknest.service.TaskService;
import com.example.worknest.service.UserService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.*;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin")
public class AdminController {

    private final UserService userService;
    private final TaskService taskService;
    private final TaskCommentService commentService;

    /** Row model for the table: one per task×assignee (assignee may be null) */
    public static class TaskRow {
        private final Task task;
        private final User assignee;
        public TaskRow(Task task, User assignee) { this.task = task; this.assignee = assignee; }
        public Task getTask() { return task; }
        public User getAssignee() { return assignee; }
    }

    /** Guard: only allow ADMIN users */
    private String requireAdmin(HttpSession session) {
        Object obj = session.getAttribute("loggedInUser");
        if (obj == null) return "redirect:/login";
        User u = (User) obj;
        if (!"ADMIN".equalsIgnoreCase(u.getRole())) return "redirect:/user/dashboard";
        return null;
    }

    /** Dashboard with optional status filter */
    @GetMapping("/dashboard")
    public String dashboard(@RequestParam(required = false, defaultValue = "ALL") String filter,
                            Model model,
                            HttpSession session) {
        String gate = requireAdmin(session);
        if (gate != null) return gate;

        populateDashboard(model, filter);
        return "admin-dashboard";
    }

    /** Helper method to populate dashboard data */
    private void populateDashboard(Model model, String filter) {
        // Counts for cards
        model.addAttribute("countPending", taskService.countByStatus(TaskStatus.PENDING));
        model.addAttribute("countInProgress", taskService.countByStatus(TaskStatus.IN_PROGRESS));
        model.addAttribute("countCompleted", taskService.countByStatus(TaskStatus.COMPLETED));
        model.addAttribute("countDelayed", taskService.countByStatus(TaskStatus.DELAYED));

        // Only non-admin users for assigning
        model.addAttribute("users", userService.findAllNonAdmins());

        // Tasks for table (filtered)
        List<Task> tasks = switch (filter.toUpperCase()) {
            case "PENDING"     -> taskService.findByStatus(TaskStatus.PENDING);
            case "IN_PROGRESS" -> taskService.findByStatus(TaskStatus.IN_PROGRESS);
            case "COMPLETED"   -> taskService.findByStatus(TaskStatus.COMPLETED);
            case "DELAYED"     -> taskService.findByStatus(TaskStatus.DELAYED);
            default            -> taskService.findAll();
        };

        // Build per-assignee rows
        List<TaskRow> rows = new ArrayList<>();
        for (Task t : tasks) {
            Collection<User> assignees = t.getAssignees();
            if (assignees == null || assignees.isEmpty()) {
                rows.add(new TaskRow(t, null));
            } else {
                for (User u : assignees) rows.add(new TaskRow(t, u));
            }
        }
        model.addAttribute("rows", rows);

        // Build comments map
        Map<Long, List<TaskComment>> taskCommentsMap = new HashMap<>();
        for (Task t : tasks) {
            taskCommentsMap.put(t.getId(), commentService.listByTask(t.getId()));
        }
        model.addAttribute("taskCommentsMap", taskCommentsMap);

        model.addAttribute("filter", filter.toUpperCase());
        model.addAttribute("today", LocalDate.now());
    }

    // ===================== User CRUD =====================

    @PostMapping("/users")
    public String addUser(@RequestParam String username,
                          @RequestParam String password,
                          @RequestParam String role,
                          HttpSession session,
                          Model model) {
        String gate = requireAdmin(session);
        if (gate != null) return gate;

        userService.create(username, password, role);
        model.addAttribute("successMessage", "User successfully created!");
        populateDashboard(model, "ALL");
        return "admin-dashboard";
    }

    @PostMapping("/users/{id}/update")
    public String updateUser(@PathVariable Long id,
                             @RequestParam String username,
                             @RequestParam String role,
                             HttpSession session,
                             Model model) {
        String gate = requireAdmin(session);
        if (gate != null) return gate;

        userService.update(id, username, role);
        model.addAttribute("successMessage", "User successfully updated!");
        populateDashboard(model, "ALL");
        return "admin-dashboard";
    }

    @PostMapping("/users/{id}/delete")
    public String deleteUser(@PathVariable Long id,
                             HttpSession session,
                             Model model) {
        String gate = requireAdmin(session);
        if (gate != null) return gate;

        userService.delete(id);
        model.addAttribute("successMessage", "User successfully deleted!");
        populateDashboard(model, "ALL");
        return "admin-dashboard";
    }

    // ===================== Task CRUD =====================

    @PostMapping("/tasks")
    public String createTask(@RequestParam String title,
                             @RequestParam(required = false) String description,
                             @RequestParam(required = false) List<Long> assigneeIds,
                             @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                             @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dueDate,
                             HttpSession session,
                             Model model) {
        String gate = requireAdmin(session);
        if (gate != null) return gate;

        taskService.create(title, description, assigneeIds, startDate, dueDate);
        model.addAttribute("successMessage", "Task successfully created!");
        populateDashboard(model, "ALL");
        return "admin-dashboard";
    }

    @PostMapping("/tasks/{id}/status")
    public String updateStatus(@PathVariable Long id,
                               @RequestParam TaskStatus status,
                               HttpSession session,
                               Model model) {
        String gate = requireAdmin(session);
        if (gate != null) return gate;

        taskService.updateStatus(id, status);
        model.addAttribute("successMessage", "Task status successfully updated!");
        populateDashboard(model, "ALL");
        return "admin-dashboard";
    }

    @PostMapping("/tasks/{id}/delete")
    public String deleteTask(@PathVariable Long id,
                             HttpSession session,
                             Model model) {
        String gate = requireAdmin(session);
        if (gate != null) return gate;

        taskService.delete(id);
        model.addAttribute("successMessage", "Task successfully deleted!");
        populateDashboard(model, "ALL");
        return "admin-dashboard";
    }

    // ===================== Task details =====================

    @GetMapping("/tasks/{id}")
    public String taskDetails(@PathVariable Long id, Model model, HttpSession session) {
        String gate = requireAdmin(session);
        if (gate != null) return gate;

        Task task = taskService.getById(id);
        model.addAttribute("task", task);
        model.addAttribute("comments", commentService.listByTask(id));
        model.addAttribute("users", userService.findAllNonAdmins());
        model.addAttribute("today", LocalDate.now());
        return "admin-task-details";
    }
}

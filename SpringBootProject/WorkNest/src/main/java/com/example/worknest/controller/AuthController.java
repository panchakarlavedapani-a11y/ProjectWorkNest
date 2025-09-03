package com.example.worknest.controller;

import com.example.worknest.model.User;
import com.example.worknest.service.UserService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    @GetMapping("/login")
    public String loginPage() { return "login"; }

    @PostMapping("/login")
    public String login(@RequestParam String username,
                        @RequestParam String password,
                        HttpSession session,
                        Model model) {

        return userService.validateLogin(username, password)
                .map(u -> {
                    session.setAttribute("loggedInUser", u);
                    if ("ADMIN".equalsIgnoreCase(u.getRole())) {
                        return "redirect:/admin/dashboard";
                    } else {
                        return "redirect:/user/dashboard";
                    }
                })
                .orElseGet(() -> {
                    model.addAttribute("error", "Invalid username or password!");
                    return "login";
                });
    }

    @GetMapping("/register")
    public String registerPage() { return "register"; }

    @PostMapping("/register")
    public String register(@RequestParam String username,
                           @RequestParam String password,
                           @RequestParam String role,
                           Model model) {
        try {
            userService.create(username, password, role);
            model.addAttribute("success", "Registration successful! Please login.");
            return "login";
        } catch (IllegalArgumentException ex) {
            model.addAttribute("error", ex.getMessage());
            return "register";
        }
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }
}

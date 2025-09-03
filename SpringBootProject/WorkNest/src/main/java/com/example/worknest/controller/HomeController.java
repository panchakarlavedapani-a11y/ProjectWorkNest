package com.example.worknest.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    // Show Home Page (public landing page)
    @GetMapping("/")
    public String home() {
        return "home"; // returns home.html
    }
}

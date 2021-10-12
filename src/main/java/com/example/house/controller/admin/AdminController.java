package com.example.house.controller.admin;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AdminController {
    @GetMapping("/admin/center")
    public String adminCenterPage() {
        return "admin/center";
    }
    @GetMapping("/admin/welcome")
    public String welcomePage() {
        return "admin/welcome";
    }

    @GetMapping("/admin/login")
    public String adminLoginPage() {
        return "admin/login";
    }

    @GetMapping("admin/house/list")
    public String houseListPage() {
        return "admin/house-list";
    }

    @GetMapping("admin/add/house")
    public String addHousePage() {
        return "admin/house-add";
    }
}

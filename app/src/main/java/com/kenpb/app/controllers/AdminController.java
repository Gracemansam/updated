package com.kenpb.app.controllers;

import com.kenpb.app.security.annotation.RequirePermission;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/core")
public class AdminController {

    @RequirePermission("ADMIN")
    @GetMapping("/admin-data")
    public ResponseEntity<String> getAdminData() {
        return ResponseEntity.ok("This is protected admin data from the core application");
    }
}


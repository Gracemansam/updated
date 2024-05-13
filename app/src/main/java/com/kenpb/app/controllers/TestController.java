//package com.kenpb.app.controllers;
//
//
//import com.kenpb.app.security.annotation.RequirePermission;
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RestController;
//
//@RestController
//@RequestMapping("/api")
//public class TestController {
//
//
//    @GetMapping("/public")
//    public String publicEndpoint() {
//        return "This is a public endpoint";
//    }
//
//    @RequirePermission("USER")
//    @GetMapping("/user")
//    public String userEndpoint() {
//        return "This is a user endpoint";
//    }
//
//    @RequirePermission("ADMIN")
//    @GetMapping("/admin")
//    public String adminEndpoint() {
//        return "This is an admin endpoint";
//    }
//}

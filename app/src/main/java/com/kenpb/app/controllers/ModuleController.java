package com.kenpb.app.controllers;


import com.kenpb.app.dtos.ApiResponse;
import com.kenpb.app.dtos.ModuleStateResponse;
import com.kenpb.app.exceptions.ModuleExceptionHandler;
import com.kenpb.app.security.annotation.RequirePermission;
import com.kenpb.app.service.ModuleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;


@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/module")
public class ModuleController {

    private final ModuleService moduleService;

    @PostMapping("/upload")
    @RequirePermission("ADMIN")
    public ResponseEntity<ApiResponse> uploadModule(@RequestParam("file") MultipartFile file) {
        ApiResponse response = moduleService.uploadModule(file);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping("/{pluginId}/update")
    public ResponseEntity<ApiResponse> updateModule(@PathVariable String pluginId, @RequestParam("file") MultipartFile file) {
        ApiResponse response = moduleService.updateModule(pluginId, file);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/{moduleId}/delete")
    public ResponseEntity<ApiResponse> deleteModule(@PathVariable String moduleId) {
        ApiResponse response = moduleService.deleteModule(moduleId);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/{moduleId}/stop")
    public ResponseEntity<ApiResponse> stopModule(@PathVariable String moduleId) {
        ApiResponse response = moduleService.stopModule(moduleId);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/{pluginId}/start")
    public ResponseEntity<ApiResponse> startModule(@PathVariable String pluginId) {
        ApiResponse response = moduleService.startModule(pluginId);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/{pluginId}/uninstall")
    public ResponseEntity<ApiResponse> uninstall(@PathVariable String pluginId) {
        ApiResponse response = moduleService.uninstall(pluginId);
        return ResponseEntity.ok().body(response);
    }

    @GetMapping("/list")
    public ResponseEntity<ApiResponse> getListOfModules() {
        try {
            ApiResponse response = moduleService.getListOfModules();
            return ResponseEntity.ok().body(response);
        }catch (ModuleExceptionHandler.NoPluginsFoundException e){

            return new ResponseEntity<>(new ApiResponse(String.valueOf(HttpStatus.NOT_FOUND.value()),
                    e.getMessage(), e.getMessage()), HttpStatus.NOT_FOUND);
        }

    }

    @GetMapping("/{pluginId}/state")
    public ResponseEntity<ModuleStateResponse> getModuleState(@PathVariable String pluginId) {
        ModuleStateResponse response = moduleService.getModuleState(pluginId);
        return ResponseEntity.ok().body(response);
    }


    @GetMapping("/{pluginId}/plugin")
    public ResponseEntity<ApiResponse> getModule(@PathVariable String pluginId) {
        ApiResponse response = moduleService.getModule(pluginId);
        return ResponseEntity.ok().body(response);
    }
    @GetMapping("/hello")
    public String hello() {
        return "Hello from plugin";
    }

}

package com.kenpb.app.serviceImplementation;

import com.kenpb.app.constants.GeneralResponseEnum;
import com.kenpb.app.dtos.ModulePropertiesDto;
import com.kenpb.app.dtos.ApiResponse;
import com.kenpb.app.dtos.ModuleStateResponse;
import com.kenpb.app.dtos.ModuleUploadResult;
import com.kenpb.app.exceptions.*;
import com.kenpb.app.service.ModuleService;
import lombok.extern.slf4j.Slf4j;
import org.pf4j.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarInputStream;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;

@Service
@Slf4j
public class ModuleServiceImpl implements ModuleService {

    @Autowired(required = false)
    private PluginManager pluginManager;

    @Override
    public ApiResponse uninstall(String pluginId) {
        PluginWrapper pluginWrapper = pluginManager.getPlugin(pluginId);
        if (pluginWrapper == null) {
            throw new ModuleExceptionHandler.ModuleNotFoundException("Module with ID " + pluginId + " does not exist");
        }

        try {

            if (pluginWrapper.getPluginState() != PluginState.STARTED) {
                throw new ModuleExceptionHandler.ModuleNotLoadedException("Module is not loaded");
            }


            pluginManager.unloadPlugin(pluginId);

            return ApiResponse.builder()
                    .statusCode(String.valueOf(HttpStatus.OK.value()))
                    .message(GeneralResponseEnum.SUCCESS.getMessage())
                    .data(pluginWrapper.getPluginState())
                    .build();

        } catch (Exception e) {
            return ApiResponse.builder()
                    .statusCode(String.valueOf(HttpStatus.INTERNAL_SERVER_ERROR.value()))
                    .message(GeneralResponseEnum.FAILED.getMessage())
                    .data("Error unloading plugin: " + e.getMessage())
                    .build();
        }
    }
    public ApiResponse uploadModule(MultipartFile file) {
        try {
            if (!Objects.requireNonNull(file.getOriginalFilename()).toLowerCase().endsWith(".jar")) {
                throw new ModuleExceptionHandler.InvalidFileTypeException("Invalid file type, File must be of type JAR");
            }
            Path pluginsDir = Paths.get("plugins");
            Files.createDirectories(pluginsDir);
            Path destinationFile = pluginsDir.resolve(file.getOriginalFilename());
            log.info("Destination file: {}", destinationFile);

            if (Files.exists(destinationFile)) {
                throw new ModuleExceptionHandler.JarAlreadyExitException("JAR file with the same name already exists");
            }

            long maxFileSize = 10000L * 1024 * 1024;
            if (file.getSize() > maxFileSize) {
                return ApiResponse.builder()
                        .statusCode(String.valueOf(HttpStatus.BAD_REQUEST.value()))
                        .message("File size too small. File must be above 10000MB")
                        .data(null)
                        .build();
            }

            try (InputStream is = file.getInputStream()) {
                Files.copy(is, destinationFile, StandardCopyOption.REPLACE_EXISTING);
            }

            if (!containsNecessaryFiles(file)) {
                throw new ModuleExceptionHandler.InvalidModuleException("The uploaded JAR file is missing necessary files");
            }

            try {
                pluginManager.loadPlugins();
            } catch (DependencyResolver.DependenciesNotFoundException e) {

                log.error("Dependency resolution failed: {}", e.getMessage());


                return ApiResponse.builder()
                        .statusCode(String.valueOf(HttpStatus.BAD_REQUEST.value()))
                        .message("Plugin dependency not found: " + e.getMessage())
                        .data(null)
                        .build();
            }

            List<PluginWrapper> resolvedPlugins = pluginManager.getResolvedPlugins();
            if (resolvedPlugins.isEmpty()) {
                log.warn("No plugins were resolved during loading");
                return ApiResponse.builder()
                        .statusCode(String.valueOf(HttpStatus.BAD_REQUEST.value()))
                        .message("Plugin dependency missing : No valid plugins could be loaded")
                        .data(null)
                        .build();
            }

            try {
                pluginManager.startPlugins();
            } catch (Exception e) {
                log.error("Error starting plugins: {}", e.getMessage());
                return ApiResponse.builder()
                        .statusCode(String.valueOf(HttpStatus.INTERNAL_SERVER_ERROR.value()))
                        .message("Failed to start plugins: " + e.getMessage())
                        .data(null)
                        .build();
            }

            List<ModulePropertiesDto> pluginProperties = getPluginProperties(resolvedPlugins);
            log.info("Plugin properties: {}", pluginProperties);

            return ApiResponse.builder()
                    .statusCode(String.valueOf(HttpStatus.OK.value()))
                    .message(GeneralResponseEnum.SUCCESS.getMessage())
                    .data(pluginProperties)
                    .build();

        } catch (IOException e) {
            log.error("IOException: {}", e.getMessage());
            return ApiResponse.builder()
                    .statusCode(String.valueOf(HttpStatus.INTERNAL_SERVER_ERROR.value()))
                    .message(GeneralResponseEnum.FAILED.getMessage())
                    .data(null)
                    .build();
        } catch (Exception e) {
            log.error("Unexpected error: {}", e.getMessage());
            return ApiResponse.builder()
                    .statusCode(String.valueOf(HttpStatus.INTERNAL_SERVER_ERROR.value()))
                    .message("An unexpected error occurred: " + e.getMessage())
                    .data(null)
                    .build();
        }
    }
    @Override
    public ApiResponse updateModule(String pluginId, MultipartFile file) {
        // Validate plugin existence
        PluginWrapper pluginWrapper = pluginManager.getPlugin(pluginId);
        if (pluginWrapper == null) {
            throw new ModuleExceptionHandler.ModuleNotFoundException("Module with ID " + pluginId + " does not exist");
        }

        try {

            if (!Objects.requireNonNull(file.getOriginalFilename()).toLowerCase().endsWith(".jar")) {
                throw new ModuleExceptionHandler.InvalidFileTypeException("Invalid file type. File must be a JAR file");
            }


            long maxFileSize = 10000L * 1024 * 1024;
            if (file.getSize() > maxFileSize) {
                throw new ModuleExceptionHandler.FileSizeExceededModuleException("File size exceeds the maximum limit of 10000MB");
            }
            

            pluginManager.deletePlugin(pluginId);


            Path tempDir = Files.createTempDirectory("plugins");
            Path tempFile = tempDir.resolve(file.getOriginalFilename());
            try (InputStream is = file.getInputStream()) {
                Files.copy(is, tempFile, StandardCopyOption.REPLACE_EXISTING);
            }


            pluginManager.loadPlugins();

            pluginManager.startPlugins();

            return ApiResponse.builder()
                    .statusCode(String.valueOf(HttpStatus.OK.value()))
                    .message(GeneralResponseEnum.SUCCESS.getMessage())
                    .build();

        } catch (IOException e) {

            return ApiResponse.builder()
                    .statusCode(String.valueOf(HttpStatus.INTERNAL_SERVER_ERROR.value()))
                    .message(GeneralResponseEnum.FAILED.getMessage())
                    .build();
        }
    }


    @Override
    public ApiResponse deleteModule(String moduleId) {

        PluginWrapper pluginWrapper = pluginManager.getPlugin(moduleId);
        if (pluginWrapper == null) {
            throw  new ModuleExceptionHandler.ModuleNotFoundException("Module with ID " + moduleId + " does not exist");
        }

        try {

            if (isModuleInUse(moduleId)) {
                throw new ModuleExceptionHandler.ModuleInUseException("Module is in use and cannot be deleted");
            }

            pluginManager.deletePlugin(moduleId);

            return ApiResponse.builder()
                    .statusCode(String.valueOf(HttpStatus.OK.value()))
                    .message(GeneralResponseEnum.SUCCESS.getMessage())
                    .build();
        } catch (Exception e) {

            return ApiResponse.builder()
                    .statusCode(String.valueOf(HttpStatus.INTERNAL_SERVER_ERROR.value()))
                    .message(GeneralResponseEnum.FAILED.getMessage())
                    .build();
        }
    }

    @Override
    public ApiResponse stopModule(String moduleId) {

        PluginWrapper pluginWrapper = pluginManager.getPlugin(moduleId);
        if (pluginWrapper == null) {
            throw new ModuleExceptionHandler.ModuleNotFoundException("Module with ID " + moduleId + " does not exist");
        }

        try {

            if (pluginWrapper.getPluginState() != PluginState.STARTED) {
                throw new ModuleExceptionHandler.ModuleNotRunningException("Module is not running and cannot be stopped");
            }

            PluginState pluginState = pluginManager.stopPlugin(moduleId);
            log.info("Plugin stopped: {}", moduleId);

            return ApiResponse.builder()
                    .statusCode(String.valueOf(HttpStatus.OK.value()))
                    .message(GeneralResponseEnum.SUCCESS.getMessage())
                    .data(pluginState)
                    .build();

        } catch (Exception e) {

            return ApiResponse.builder()
                    .statusCode(String.valueOf(HttpStatus.INTERNAL_SERVER_ERROR.value()))
                    .message(GeneralResponseEnum.FAILED.getMessage())
                    .data("Error stopping plugin: " + e.getMessage())
                    .build();
        }

    }

    @Override
    public ApiResponse startModule(String pluginId) {
        PluginWrapper pluginWrapper = pluginManager.getPlugin(pluginId);
        if (pluginWrapper == null) {
            throw new ModuleExceptionHandler.ModuleNotFoundException("module with ID " + pluginId + " does not exist");
        }

        try {

            if (pluginWrapper.getPluginState() != PluginState.STOPPED) {
                throw new ModuleExceptionHandler.ModuleAlreadyRunningModuleException("module is already running and cannot be started");
            }

            PluginState pluginState = pluginManager.startPlugin(pluginId);
            log.info("Plugin started: {}", pluginId);

            return ApiResponse.builder()
                    .statusCode(String.valueOf(HttpStatus.OK.value()))
                    .message(GeneralResponseEnum.SUCCESS.getMessage())
                    .data(pluginState)
                    .build();

        } catch (Exception e) {

            return ApiResponse.builder()
                    .statusCode(String.valueOf(HttpStatus.INTERNAL_SERVER_ERROR.value()))
                    .message(GeneralResponseEnum.FAILED.getMessage())
                    .data("Error starting module: " + e.getMessage())
                    .build();
        }
    }


    private boolean containsNecessaryFiles(MultipartFile jarFile) {
        try (JarInputStream jarInputStream = new JarInputStream(jarFile.getInputStream())) {
            JarEntry entry;
            boolean hasPluginProperties = false;
            boolean hasJavaClasses = false;

            while ((entry = jarInputStream.getNextJarEntry()) != null) {
                if (!entry.isDirectory()) {

                    if (entry.getName().equals("plugin.properties")) {
                        hasPluginProperties = true;
                    }

                    if (entry.getName().endsWith(".class")) {
                        hasJavaClasses = true;
                    }
                }
            }


            return hasPluginProperties && hasJavaClasses;
        } catch (IOException e) {

            e.printStackTrace();
        }
        return false;
    }


    private boolean isModuleInUse(String pluginId) {

        List<PluginWrapper> loadedPlugins = pluginManager.getPlugins();
        for (PluginWrapper loadedPlugin : loadedPlugins) {

            if (loadedPlugin.getPluginId().equals(pluginId)) {
                continue;
            }
            PluginDescriptor pluginDescriptor = loadedPlugin.getDescriptor();
            if (pluginDescriptor.getDependencies().contains(pluginId)) {
                return true;
            }
        }
        return false;
    }


    @Override
    public ApiResponse getListOfModules() {
        List<PluginWrapper> resolvedPlugins = pluginManager.getResolvedPlugins();
        log.info("Resolved plugins: {}", resolvedPlugins);

        if (resolvedPlugins.isEmpty()) {
            throw new ModuleExceptionHandler.NoPluginsFoundException("No modules found");
        }
        List<ModulePropertiesDto> pluginProperties = getPluginProperties(resolvedPlugins);
        log.info("Plugin properties: {}", pluginProperties);

        return ApiResponse.builder()
                .statusCode(String.valueOf(HttpStatus.OK.value()))
                .message(GeneralResponseEnum.SUCCESS.getMessage())
                .data(pluginProperties)
                .build();
    }

    @Override
    public ModuleStateResponse getModuleState(String pluginId) {
        PluginWrapper pluginWrapper = pluginManager.getPlugin(pluginId);

        if (pluginWrapper == null) {
            throw new ModuleExceptionHandler.ModuleNotFoundException("Module with ID " + pluginId + " does not exist");
        }

        PluginState pluginState = pluginWrapper.getPluginState();
        log.info("Plugin state: {}", pluginState);

        String stateDescription = pluginState.toString();

        return ModuleStateResponse.builder()
                .statusCode(String.valueOf(HttpStatus.OK.value()))
                .message(GeneralResponseEnum.SUCCESS.getMessage())
                .pluginId(pluginId)
                .state(stateDescription)
                .build();
    }


    @Override
    public ApiResponse getModule(String pluginId) {

        PluginWrapper pluginWrapper = pluginManager.getPlugin(pluginId);

        if (pluginWrapper == null) {

            throw new ModuleExceptionHandler.ModuleNotFoundException("Module with ID " + pluginId + " does not exist");
        }

        String pluginName = pluginWrapper.getDescriptor().getPluginId();
        PluginState pluginState = pluginWrapper.getPluginState();
        String stateDescription = pluginState.toString();
        String version = pluginWrapper.getDescriptor().getVersion();

        ModulePropertiesDto modulePropertiesDto = ModulePropertiesDto.builder()
                .moduleId(pluginId)
                .moduleName(pluginName)
                .state(stateDescription)
                .version(version)
                .build();

        return ApiResponse.builder()
                .statusCode(String.valueOf(HttpStatus.OK.value()))
                .message(GeneralResponseEnum.SUCCESS.getMessage())
                .data(modulePropertiesDto)
                .build();
    }


    private List<ModulePropertiesDto> getPluginProperties(List<PluginWrapper> resolvedPlugins) {
        return resolvedPlugins.stream()
                .map(pluginWrapper -> {
                    String pluginId = pluginWrapper.getPluginId();
                    String pluginName = pluginWrapper.getDescriptor().getPluginId();
                    PluginState pluginState = pluginWrapper.getPluginState();
                    String stateDescription = pluginState.toString();
                    String version = pluginWrapper.getDescriptor().getVersion();

                    return ModulePropertiesDto.builder()
                            .moduleId(pluginId)
                            .moduleName(pluginName)
                            .state(stateDescription)
                            .version(version)
                            .build();
                })
                .collect(Collectors.toList());
    }




}

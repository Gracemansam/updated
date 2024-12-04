package com.kenpb.app.serviceImplementation;

import com.kenpb.app.constants.GeneralResponseEnum;
import com.kenpb.app.dtos.ApiResponse;
import com.kenpb.app.exceptions.ModuleExceptionHandler;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.pf4j.PluginManager;
import org.pf4j.PluginState;
import org.pf4j.PluginWrapper;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ModuleServiceImplTest {

    @Mock
    private PluginManager pluginManager;

    @Mock
    private PluginWrapper pluginWrapper;

    @InjectMocks
    private ModuleServiceImpl moduleService;

    @Test
    void uninstallPluginSuccess() {
        when(pluginManager.getPlugin("validPluginId")).thenReturn(pluginWrapper);
        when(pluginWrapper.getPluginState()).thenReturn(PluginState.STARTED);

        ApiResponse response = moduleService.uninstall("validPluginId");

        assertEquals(HttpStatus.OK.value(), Integer.parseInt(response.getStatusCode()));
        assertEquals(GeneralResponseEnum.SUCCESS.getMessage(), response.getMessage());
        verify(pluginManager, times(1)).unloadPlugin("validPluginId");
    }

    @Test
    void uninstallPluginNotFound() {
        when(pluginManager.getPlugin("invalidPluginId")).thenReturn(null);

        assertThrows(ModuleExceptionHandler.ModuleNotFoundException.class, () -> moduleService.uninstall("invalidPluginId"));
    }


//    @Test
//    void uploadModuleSuccess() throws Exception {
//        MultipartFile file = new MockMultipartFile("test", "test.jar", "application/java-archive", "test".getBytes());
//        when(pluginManager.getPlugin("validPluginId")).thenReturn(pluginWrapper);
//        when(pluginWrapper.getPluginState()).thenReturn(PluginState.STARTED);
//
//        Path pluginsDir = Paths.get("plugins");
//        Files.createDirectories(pluginsDir);
//        Path destinationFile = pluginsDir.resolve(file.getOriginalFilename());
//
//        ApiResponse response = moduleService.uploadModule(file);
//
//        assertEquals(HttpStatus.OK.value(), Integer.parseInt(response.getStatusCode()));
//        assertEquals(GeneralResponseEnum.SUCCESS.getMessage(), response.getMessage());
//        verify(pluginManager, times(1)).loadPlugins();
//        verify(pluginManager, times(1)).startPlugins();
//    }
//
//    @Test
//    void uploadModuleInvalidFileType() {
//        MultipartFile file = new MockMultipartFile("test", "test.txt", "text/plain", "test".getBytes());
//
//        assertThrows(ModuleExceptionHandler.InvalidFileTypeException.class, () -> moduleService.uploadModule(file));
//    }
//
//    @Test
//    void uploadModuleFileExists() throws Exception {
//        MultipartFile file = new MockMultipartFile("test", "test.jar", "application/java-archive", "test".getBytes());
//
//        Path pluginsDir = Paths.get("plugins");
//        Files.createDirectories(pluginsDir);
//        Path destinationFile = pluginsDir.resolve(file.getOriginalFilename());
//        Files.createFile(destinationFile);
//
//        assertThrows(ModuleExceptionHandler.JarAlreadyExitException.class, () -> moduleService.uploadModule(file));
//    }


}
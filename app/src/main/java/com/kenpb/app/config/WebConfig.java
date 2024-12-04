package com.kenpb.app.config;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.CacheControl;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;


@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Core static resources
        registry.addResourceHandler("/**")
                .addResourceLocations("classpath:/static/")
                .setCachePeriod(0);

        // Plugin static resources from plugins directory
        registry.addResourceHandler("/patient/**")
                .addResourceLocations("jar:file:plugins/demohiv-0.0.1-SNAPSHOT.jar!/static/")
                .setCachePeriod(0);
    }
}

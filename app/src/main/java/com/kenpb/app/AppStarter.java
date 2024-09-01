package com.kenpb.app;

import org.laxture.spring.util.ApplicationContextProvider;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.transaction.annotation.EnableTransactionManagement;
//@SpringBootApplication(scanBasePackages = "com.kenpb")
@SpringBootApplication(scanBasePackages = "com.kenpb", exclude = {
         SecurityAutoConfiguration.class
})
@EnableTransactionManagement
public class AppStarter {

    public static void main(String[] args) {
//        SpringApplication.run(AppStarter.class, args);

        SpringApplicationBuilder builder = new SpringApplicationBuilder();
        builder.sources(AppStarter.class);
        builder.build().run();
    }


    @Bean
    public ApplicationContextAware multiApplicationContextProviderRegister() {
        return ApplicationContextProvider::registerApplicationContext;
    }

}


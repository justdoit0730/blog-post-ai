package org.justdoit.blog.config;

import org.justdoit.blog.variable.GlobalVariables;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfig {
    @Bean
    public GlobalVariables globalVariables() {
        return new GlobalVariables();
    }
}
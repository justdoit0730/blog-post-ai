package org.justdoit.blog.configuration.app;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "app")
public class AppMetadata {

    private String env;
    private Map<String, String> servers;

    private String server;

    @PostConstruct
    public void initServer() {
        this.server = servers.getOrDefault(env, servers.get("dev"));
    }
}


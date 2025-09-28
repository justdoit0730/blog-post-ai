package org.justdoit.blog.configuration.manager;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import java.util.Map;

@Configuration
@ConfigurationProperties(prefix = "manager.write")
@Getter
@Setter
public class WriteMetadata {
    private int maxRows;
    private Map<String, Integer> availableTokenPerDay;
}


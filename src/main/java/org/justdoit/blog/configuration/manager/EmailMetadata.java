package org.justdoit.blog.configuration.manager;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "manager.email")
@Getter
@Setter
public class EmailMetadata {
    private boolean debug;
    private String mainEmail;
    private String sendEmail;
}


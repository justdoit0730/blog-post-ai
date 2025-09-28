package org.justdoit.blog.configuration.manager;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.regions.Region;

@Configuration
@ConfigurationProperties(prefix = "aws.s3")
@Data
public class AwsS3Metadata {
    private Region region;
    private String bucketName;
}


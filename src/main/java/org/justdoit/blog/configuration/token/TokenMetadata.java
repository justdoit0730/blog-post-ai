package org.justdoit.blog.configuration.token;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "token")
@Getter
@Setter
@Deprecated
public class TokenMetadata {
    /** 토큰 유효성 검사 최대 시도 횟수 */
    private int maxValidationAttempts;

    /** 토큰 유효성 검사 실패 시 알림 이메일 */
    private String alertEmail;
}


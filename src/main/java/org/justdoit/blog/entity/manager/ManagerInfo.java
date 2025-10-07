package org.justdoit.blog.entity.manager;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "ac_manager_info")
// email key, AES-256 key, ai key, s3 설정, naver api 설정 최초 기동 시 변경 없음
public class ManagerInfo {
    @Id
    private String id = "default";

    @Column(name = "email_key", nullable = false)
    private String emailKey;

    @Column(name = "main_email", nullable = false)
    private String mainEmail;

    @Column(name = "send_email", nullable = false)
    private String sendEmail;

    @Column(name = "aes_key", nullable = false)
    private String aesKey;

    @Column(name = "ai_key", nullable = false)
    private String aiKey;

    // 네이버 Cafe 인증 실패 횟수
    @Column(nullable = false)
    private int cafeValidationFailCount;

    @Column
    private String cafeClientId;

    @Column
    private String cafeClientSecret;

    @Column
    private String cafeRefreshToken;

    @Column
    private long cafeRefreshTokenExpiresAt;

    @Column(name = "s3_key", nullable = false)
    private String s3Key;

    @Column(name = "s3_password", nullable = false)
    private String s3Password;

    @CreatedDate
    @Column(updatable = false)
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;

    public ManagerInfo() {}

}


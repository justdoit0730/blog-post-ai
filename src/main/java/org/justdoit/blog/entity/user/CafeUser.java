package org.justdoit.blog.entity.user;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.justdoit.blog.template.Role;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@Entity
// 회원 및 네이버 카페 정보
@EntityListeners(AuditingEntityListener.class)
@Table(name = "ac_cafe_user")
public class CafeUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column
    private String subEmail;

    @Column
    private boolean isSubEmailUsed;

    @Column(nullable = false)
    private String password;

    // 네이버 API 인증 실패 횟수
    @Column(nullable = false)
    private int cafeValidationFailCount;

    // 네이버 API 사용 여부
    @Column
    private boolean clientApiEnabled;

    @Column
    private String cafeClientId;

    @Column
    private String cafeClientSecret;

    @Column
    private String cafeRefreshToken;

    @Column
    private long cafeRefreshTokenExpiresAt;

    @Column
    private boolean isEmailPrivacyAgreed;

    @Column
    private boolean isClientPrivacyAgreed;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @CreatedDate
    @Column(updatable = false)
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;

    @Builder
    public CafeUser(String email, String password, int cafeValidationFailCount, boolean clientApiEnabled, String cafeClientId, String cafeClientSecret, String cafeRefreshToken, long cafeRefreshTokenExpiresAt, boolean isEmailPrivacyAgreed, boolean isClientPrivacyAgreed, Role role) {
        this.email = email;
        this.password = password;
        this.cafeValidationFailCount = cafeValidationFailCount;
        this.clientApiEnabled = clientApiEnabled;
        this.cafeClientId = cafeClientId;
        this.cafeClientSecret = cafeClientSecret;
        this.cafeRefreshToken = cafeRefreshToken;
        this.cafeRefreshTokenExpiresAt = cafeRefreshTokenExpiresAt;
        this.isEmailPrivacyAgreed = isEmailPrivacyAgreed;
        this.isClientPrivacyAgreed = isClientPrivacyAgreed;
        this.role = role;
    }

    public String getRoleKey() {
        return this.role.getKey();
    }
}

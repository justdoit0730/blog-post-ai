package org.justdoit.blog.entity.ai;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.justdoit.blog.entity.user.CafeUser;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@Entity
@EntityListeners(AuditingEntityListener.class)
// AI 기본 설정
@Table(name = "ac_ai_write_setting")
public class AiWriteSetting {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ac_cafe_user.email 과 연결 -> 만약  ac_cafe_user.email 에서 삭제하면 이 테이블에서 해당 email row도 삭제
    @OneToOne
    @JoinColumn(name = "email", referencedColumnName = "email", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private CafeUser cafeUser;

    @Column(nullable = false)
    private int maxToken = 1000;

    @Column(nullable = false)
    private double temperature = 0.5;

    @Column(nullable = false)
    private String textVolume = "1";

    @Column
    private int availableToken = 10000;

    @Column
    private int totalWriteCount;

    @Column
    private int blackListCount = 0;

    /**
     * blackListReason 은 JSON 필드 저장 → TEXT 컬럼에 JSON 문자열 저장하는 방식 추천
     * JSONArray 자체를 @Column에 쓰는 건 잘 안 맞습니다.
     */
    @Lob
    @Column
    private String blackListReason;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private Instant updatedAt;

    @Builder
    public AiWriteSetting(CafeUser cafeUser, int maxToken, double temperature, String textVolume, int availableToken) {
        this.cafeUser = cafeUser;
        this.temperature = temperature;
        this.textVolume = textVolume;
        this.availableToken = availableToken;
    }

}

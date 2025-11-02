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
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@Entity
@EntityListeners(AuditingEntityListener.class)
// AI 글쓰기 목록
@Table(name = "ac_ai_write")
public class AiWrite {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ac_cafe_user.email 과 연결 -> 만약  ac_cafe_user.email 에서 삭제하면 이 테이블에서 해당 email row도 삭제
    @OneToOne
    @JoinColumn(name = "email", referencedColumnName = "email", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private CafeUser cafeUser;

    // 주제
    @Column(nullable = false)
    private String subject;

    // 요구사항
    @Column(nullable = false)
    private String prompt;

    // 제목
    @Column(nullable = false)
    private String title;

    // 내용<p> 태그
    @Column(nullable = false)
    private String content;

    // s3 내 저장된 이미지 파일명 List
    @Column
    private String imgList;//{"", "", ""}

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @Builder
    public AiWrite(CafeUser cafeUser, String subject, String prompt, String title, String content, String imgList) {
        this.cafeUser = cafeUser;

        this.subject = subject;
        this.prompt = prompt;
        this.title = title;
        this.content = content;
        this.imgList = imgList;
    }

}

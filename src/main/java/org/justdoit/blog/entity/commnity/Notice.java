package org.justdoit.blog.entity.commnity;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;


@Getter
@Setter
@NoArgsConstructor
@Entity
@EntityListeners(AuditingEntityListener.class)
// 카페 공지 사항 게시글
@Table(name = "ac_cafe_notice")
public class Notice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 주제
    @Column
    private String subject;

    // 제목
    @Column
    private String title;

    // 내용
    @Lob
    @Column
    private String content;

    @CreatedDate
    @Column(updatable = false)
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;

    @Builder
    public Notice(String subject, String title, String content) {
        this.subject = subject;
        this.title = title;
        this.content = content;
    }

}

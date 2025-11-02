package org.justdoit.blog.entity.cafe.ai;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.justdoit.blog.entity.user.CafeUser;
import org.springframework.data.annotation.CreatedDate;

import java.time.Instant;


@Getter
@Setter
@NoArgsConstructor
@Entity
// Naver cafe 게시한 글 목록
@Table(name = "ac_cafe_ai_posting")
public class CafeAiPosting {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "email", referencedColumnName = "email", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private CafeUser cafeUser;

    // 카페 이름
    @Column
    private String cafeName;

    // 카페 ID
    @Column
    private String cafeId;

    // 카페 게시판 태그
    @Column
    private String cafeBoardTag;

    // 카페 게시판 ID
    @Column
    private String cafeBoardId;

    // 주제
    @Column(nullable = false)
    private String subject;

    // 요구사항
    @Column(nullable = false)
    private String prompt;

    // 제목
    @Column(nullable = false)
    private String title;

    // html 태그
    @Lob
    @Column(nullable = false)
    private String contentHtml;

    // s3 내 저장된 이미지 파일명 List
    @Lob
    @Column
    private String imgList;

    // 게시글 링크
    @Column
    private String cafeBoardLink;

    @CreatedDate
    @Column(updatable = false)
    private Instant createdAt;

    @Builder
    public CafeAiPosting(CafeUser cafeUser, String cafeName, String cafeId, String cafeBoardTag, String cafeBoardId, String subject, String prompt, String title, String contentHtml, String imgList, String cafeBoardLink) {
        this.cafeUser = cafeUser;
        this.cafeName = cafeName;
        this.cafeId = cafeId;
        this.cafeBoardTag = cafeBoardTag;
        this.cafeBoardId = cafeBoardId;

        this.subject = subject;
        this.prompt = prompt;
        this.title = title;
        this.contentHtml = contentHtml;
        this.imgList = imgList;
        this.cafeBoardLink = cafeBoardLink;
    }

}

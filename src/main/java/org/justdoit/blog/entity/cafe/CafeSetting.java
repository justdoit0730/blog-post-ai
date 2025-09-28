package org.justdoit.blog.entity.cafe;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import java.time.Instant;


@Getter
@Setter
@NoArgsConstructor
@Entity
// 카페 게시글 설정
@Table(name = "ac_cafe_setting")
public class CafeSetting {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private boolean isUsed;

    @Column(nullable = false)
    private String no;

    @Column(nullable = false)
    private String cafeId;

    @Column(nullable = false)
    private String cafeName;

    @Column(nullable = false)
    private String cafeMenuId;

    @Column(nullable = false)
    private String subject;

    @Column(nullable = false)
    private String template;

    @CreatedDate
    @Column(updatable = false)
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;

    @Builder
    public CafeSetting(String email, String cafeId, String cafeName, String cafeMenuId, String subject, String template) {
        this.email = email;
        this.cafeId = cafeId;
        this.cafeName = cafeName;
        this.cafeMenuId = cafeMenuId;
        this.subject = subject;
        this.template = template;
    }

}

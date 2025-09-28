package org.justdoit.blog.entity.cafe;

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

import java.time.Instant;


@Getter
@Setter
@NoArgsConstructor
@Entity
// 카페 ID 설정
@Table(name = "ac_cafe_id_template")
public class CafeIdTemplate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "email", referencedColumnName = "email", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private CafeUser cafeUser;

    @Lob
    @Column
    private String cafeIdTemplate;

    @CreatedDate
    @Column(updatable = false)
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;

    @Builder
    public CafeIdTemplate(CafeUser cafeUser, String cafeIdTemplate) {
        this.cafeUser = cafeUser;
        this.cafeIdTemplate = cafeIdTemplate;
    }

}

package org.justdoit.blog.entity.manager;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.Instant;

@Deprecated
@Getter
@Setter
@Entity
@Table(name = "ac_manager_config")
// 사용자에게 영향을 줄 수 있는 설정값. 주기적으로 갱신한다.
public class ManagerConfig {
    @Id
    private String id = "default";

    @Column(nullable = false)
    private int availableTokensPerDay = 10000;

    @CreatedDate
    @Column(updatable = false)
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;

    public ManagerConfig() {}

}


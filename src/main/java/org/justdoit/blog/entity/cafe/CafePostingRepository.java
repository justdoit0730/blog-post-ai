package org.justdoit.blog.entity.cafe;

import org.justdoit.blog.entity.user.CafeUser;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CafePostingRepository extends JpaRepository<CafePosting, Long> {
    Optional<CafePosting> findByCafeUser(CafeUser cafeUser);

    Page<CafePosting> findByCafeUserEmailOrderByCreatedAtDesc(String email, Pageable pageable);

}

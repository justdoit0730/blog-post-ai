package org.justdoit.blog.entity.cafe.ai;

import org.justdoit.blog.entity.user.CafeUser;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CafeAiPostingRepository extends JpaRepository<CafeAiPosting, Long> {
    Optional<CafeAiPosting> findByCafeUser(CafeUser cafeUser);

    Page<CafeAiPosting> findByCafeUserEmailOrderByCreatedAtDesc(String email, Pageable pageable);

}

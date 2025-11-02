package org.justdoit.blog.entity.ai;

import org.justdoit.blog.entity.user.CafeUser;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AiWriteRepository extends JpaRepository<AiWrite, Long> {

    Optional<AiWrite> findByCafeUser(CafeUser cafeUser);

    List<AiWrite> findByCafeUserEmail(String email);

    Page<AiWrite> findByCafeUserEmailOrderByCreatedAtDesc(String email, Pageable pageable);
}

package org.justdoit.blog.entity.ai.write;

import org.justdoit.blog.entity.user.CafeUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AiWriteTemplateRepository extends JpaRepository<AiWriteTemplate, Long> {

    Optional<AiWriteTemplate> findByCafeUser(CafeUser cafeUser);

}

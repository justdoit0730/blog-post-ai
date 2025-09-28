package org.justdoit.blog.entity.cafe;

import org.justdoit.blog.entity.user.CafeUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CafePostingTemplateRepository extends JpaRepository<CafePostingTemplate, Long> {

    Optional<CafePostingTemplate> findByCafeUser(CafeUser cafeUser);

}

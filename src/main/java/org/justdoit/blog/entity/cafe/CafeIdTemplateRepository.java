package org.justdoit.blog.entity.cafe;

import org.justdoit.blog.entity.user.CafeUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CafeIdTemplateRepository extends JpaRepository<CafeIdTemplate, Long> {

    Optional<CafeIdTemplate> findByCafeUser(CafeUser cafeUser);

}

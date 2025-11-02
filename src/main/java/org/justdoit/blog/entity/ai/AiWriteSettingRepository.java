package org.justdoit.blog.entity.ai;

import org.justdoit.blog.entity.user.CafeUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AiWriteSettingRepository extends JpaRepository<AiWriteSetting, Long> {

    Optional<AiWriteSetting> findByCafeUser(CafeUser cafeUser);

}

package org.justdoit.blog.entity.manager;

import org.justdoit.blog.entity.user.CafeUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ManagerInfoRepository extends JpaRepository<ManagerInfo, String> {
    Optional<ManagerInfo> findByMainEmail(String mainEmail);
}

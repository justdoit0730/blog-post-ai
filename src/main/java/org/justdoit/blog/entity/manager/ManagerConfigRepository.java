package org.justdoit.blog.entity.manager;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Deprecated
@Repository
public interface ManagerConfigRepository extends JpaRepository<ManagerConfig, String> {
}

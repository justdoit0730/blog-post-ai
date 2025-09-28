package org.justdoit.blog.entity.manager;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ManagerInfoRepository extends JpaRepository<ManagerInfo, String> {
}

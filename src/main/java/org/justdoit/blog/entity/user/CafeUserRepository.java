package org.justdoit.blog.entity.user;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CafeUserRepository extends JpaRepository<CafeUser, Long> {

    Optional<CafeUser> findByEmail(String email);

//    Optional<User> findByUsername(String email);

//    Optional<User> findByUsernameOrEmail(String email);

    void deleteByEmail(String email);
}

package com.sanket.AI.Code.Review.Platform.user.repository;

import com.sanket.AI.Code.Review.Platform.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {
    Optional<User> findByEmail(String email);
    Optional<User> findByUsername(String username);
    Optional<User> findByGithubId(String githubId);
    Optional<User> findByGithubUsername(String githubUsername);
    boolean existsByEmail(String email);
    boolean existsByUsername(String username);
    boolean existsByGithubId(String githubId);
}

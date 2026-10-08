package com.samrat.ai.developer.assessment.user.repository;

import com.samrat.ai.developer.assessment.user.model.Role;
import com.samrat.ai.developer.assessment.user.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    Page<User> findByNameContainingIgnoreCaseOrEmailContainingIgnoreCase(String search, String search1, Pageable pageable);

    Page<User> findAllByRole(Role role, Pageable pageable);
}
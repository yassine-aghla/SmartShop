package com.smartshop.repositories;

import com.smartshop.entity.User;
import com.smartshop.entity.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    boolean existsByUsername(String username);

    List<User> findByRole(UserRole role);

    List<User> findByRoleOrderByCreatedAtDesc(UserRole role);
}
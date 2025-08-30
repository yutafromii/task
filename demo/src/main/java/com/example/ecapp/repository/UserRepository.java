package com.example.ecapp.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

import com.example.ecapp.domain.AppUser;

public interface UserRepository extends JpaRepository<AppUser, Long> {
  Optional<AppUser> findByEmail(String email);
}
package com.example.ecapp.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.ecapp.domain.AppUser;
import com.example.ecapp.domain.Cart;

public interface CartRepository extends JpaRepository<Cart, Long>{
  List<Cart> findByUser(AppUser user);
  Optional<Cart> findByUserId(Long userId);

}

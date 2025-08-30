package com.example.ecapp.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.ecapp.domain.AppUser;
import com.example.ecapp.domain.Order;

public interface OrderRepository extends JpaRepository<Order, Long>{
  List<Order> findByUser(AppUser user);
  Optional<Order> findByUserId(Long userId);
}

package com.example.ecapp.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.example.ecapp.domain.AppUser;
import com.example.ecapp.domain.Order;

public interface OrderRepository extends JpaRepository<Order, Long>, JpaSpecificationExecutor<Order> {
  List<Order> findByUser(AppUser user);
  Optional<Order> findByUserId(Long userId);

  // 履歴取得用
  @EntityGraph(attributePaths = {"items", "items.product", "user"})
  List<Order> findByUserIdOrderByOrderedAtDesc(Long userId);
  @EntityGraph(attributePaths = {"items", "items.product", "user"})
  Page<Order> findByUserId(Long userId, Pageable pageable);
}

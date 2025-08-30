package com.example.ecapp.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.ecapp.domain.OrderItem;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long>{
  
}

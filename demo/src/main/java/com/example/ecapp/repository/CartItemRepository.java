package com.example.ecapp.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.ecapp.domain.CartItem;

public interface CartItemRepository extends JpaRepository<CartItem, Long>{
}

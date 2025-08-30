package com.example.ecapp.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.ecapp.domain.Product;

public interface ProductRepository extends JpaRepository<Product, Long>{
  
} 
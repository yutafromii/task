package com.example.ecapp.repository.spec;

import org.springframework.data.jpa.domain.Specification;

import com.example.ecapp.domain.Category;
import com.example.ecapp.domain.Product;

public class ProductSpecifications {
  public static Specification<Product> isActive() {
    return (root, query, cb) -> cb.isTrue(root.get("isActive"));
  }

  public static Specification<Product> categoryEquals(Category category) {
    if (category == null) return null;
    return (root, query, cb) -> cb.equal(root.get("category"), category);
  }

  public static Specification<Product> stockIn() {
    return (root, query, cb) -> cb.greaterThan(root.get("stock"), 0);
  }

  public static Specification<Product> stockComing() {
    return (root, query, cb) -> cb.equal(root.get("stock"), 0);
  }

  public static Specification<Product> nameContainsIgnoreCase(String q) {
    if (q == null || q.trim().isEmpty()) return null;
    String like = "%" + q.trim().toLowerCase() + "%";
    return (root, query, cb) -> cb.like(cb.lower(root.get("name")), like);
  }
}


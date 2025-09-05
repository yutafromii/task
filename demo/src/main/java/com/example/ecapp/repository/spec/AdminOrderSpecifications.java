package com.example.ecapp.repository.spec;

import org.springframework.data.jpa.domain.Specification;

import com.example.ecapp.domain.Order;
import com.example.ecapp.domain.OrderStatus;

public class AdminOrderSpecifications {
  public static Specification<Order> hasStatus(OrderStatus status){
    if (status == null) return null;
    return (root, query, cb) -> cb.equal(root.get("status"), status);
  }
  public static Specification<Order> orderedFrom(java.time.LocalDateTime from){
    if (from == null) return null;
    return (root, query, cb) -> cb.greaterThanOrEqualTo(root.get("orderedAt"), from);
  }
  public static Specification<Order> orderedTo(java.time.LocalDateTime to){
    if (to == null) return null;
    return (root, query, cb) -> cb.lessThan(root.get("orderedAt"), to);
  }
  public static Specification<Order> q(String q){
    if (q == null || q.isBlank()) return null;
    String like = "%" + q.trim().toLowerCase() + "%";
    return (root, query, cb) -> cb.or(
      cb.like(cb.lower(root.get("orderNumber")), like),
      cb.like(cb.lower(root.join("user").get("name")), like)
    );
  }
}


package com.example.ecapp.domain;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.*;
import org.hibernate.annotations.BatchSize;
import lombok.*;

@Entity
@Table(name = "orders")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Order {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(optional = false)
  @JoinColumn(name = "user_id")
  private AppUser user;

  @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
  @BatchSize(size = 50)
  private List<OrderItem> items = new ArrayList<>();

  private LocalDateTime orderedAt;

  @jakarta.persistence.Column(length = 64)
  private String orderNumber;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20, columnDefinition = "varchar(20) default 'PENDING'")
  private OrderStatus status = OrderStatus.PENDING;

  @PrePersist
  void onCreate() {
    if (this.orderedAt == null) {
      this.orderedAt = LocalDateTime.now();
    }
    if (this.orderNumber == null || this.orderNumber.isBlank()) {
      this.orderNumber = "ORD-" + java.time.format.DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS").format(java.time.LocalDateTime.now());
    }
  }
}

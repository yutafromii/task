package com.example.ecapp.domain;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "app_user")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AppUser {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, unique = true)
  private String email; // メールアドレスログインを前提

  @Column(nullable = false)
  private String password;

  @Column(nullable = false)
  private String name;

  @Column(nullable = true)
  private String phoneNumber;

  @Column(nullable = true)
  private String address; // 旧: 後方互換のため残置

  // --- 住所の詳細分割フィールド（新規）---
  @Column(length = 16)
  private String postalCode; // 例: 123-4567

  @Column(length = 50)
  private String prefecture; // 都道府県

  @Column(length = 100)
  private String city; // 市区町村

  @Column(length = 200)
  private String addressLine1; // 町名・番地

  @Column(length = 200)
  private String addressLine2; // 建物名・部屋番号 等（任意）

  @Column(nullable = false)
  private String role; // 後で Enum化してもOK

  @Column(nullable = false)
  private boolean isActive = true;

  @Column(nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @Column(name = "updated_at")
  private LocalDateTime updatedAt;

  @PrePersist
  void onCreate() {
    LocalDateTime now = LocalDateTime.now();
    if (createdAt == null) createdAt = now;
    updatedAt = now;
  }

  @PreUpdate
  void onUpdate() {
    updatedAt = LocalDateTime.now();
  }

  @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<DeliveryAddress> deliveryAddresses = new ArrayList<>();

}

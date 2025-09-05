package com.example.ecapp.domain;


import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "delivery_address")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DeliveryAddress {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private String name;

  @Column(nullable = false)
  private String furigana;

  @Column(nullable = false)
  private String postalCode;

  // 旧: 単一の住所文字列（後方互換のため必須のまま保持）
  @Column(nullable = false)
  private String address;

  // 新: 分割住所フィールド（任意）
  @Column(length = 50)
  private String prefecture; // 都道府県

  @Column(length = 100)
  private String city; // 市区町村

  @Column(length = 200)
  private String addressLine1; // 町名・番地

  @Column(length = 200)
  private String addressLine2; // 建物名等（任意）

  @Column(nullable = false)
  private String phone;

  @Column(nullable = false)
  private String email;

  // 🔸 AppUserとのリレーション（多対1）
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private AppUser user;
}

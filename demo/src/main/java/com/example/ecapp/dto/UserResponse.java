package com.example.ecapp.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {
  private Long id;
  private String name;
  private String email;
  private String phoneNumber;
  // 旧: 単一文字列の住所（後方互換）
  private String address;
  // 新: 分割住所
  private String postalCode;
  private String prefecture;
  private String city;
  private String addressLine1;
  private String addressLine2;
  private String password;
  private String role;
}

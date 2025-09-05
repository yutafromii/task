package com.example.ecapp.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserUpdateRequest {
  private String name;
  private String email;
  private String phoneNumber;
  // 旧フォーマット
  private String address;
  // 新フォーマット（個別項目）
  private String postalCode;
  private String prefecture;
  private String city;
  private String addressLine1;
  private String addressLine2;
  private String password;
  private String role;
}

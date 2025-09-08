package com.example.ecapp.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegisterRequest {
  private String name;
  private String email;
  private String password;
  // 任意: "ADMIN" を指定した場合のみ管理者登録
  private String role;
}

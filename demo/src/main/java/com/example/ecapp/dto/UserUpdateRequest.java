package com.example.ecapp.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserUpdateRequest {
  private String name;
  private String email;
  private String phoneNumber;
  private String address;
  private String password;
  private String role;
}

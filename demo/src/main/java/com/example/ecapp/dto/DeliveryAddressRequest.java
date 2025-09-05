package com.example.ecapp.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Data
public class DeliveryAddressRequest {
    private String name;
    private String furigana;
    private String postalCode;
    // 旧: 単一住所（任意）
    private String address;
    // 新: 分割住所
    private String prefecture;
    private String city;
    private String addressLine1;
    private String addressLine2;
    private String phone;
    private String email;
}

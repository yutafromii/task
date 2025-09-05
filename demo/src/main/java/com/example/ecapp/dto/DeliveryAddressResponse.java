package com.example.ecapp.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class DeliveryAddressResponse {
    private Long id;
    private String name;
    private String furigana;
    private String postalCode;
    private String address; // 旧
    private String prefecture;
    private String city;
    private String addressLine1;
    private String addressLine2;
    private String phone;
    private String email;
}

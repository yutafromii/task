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
    private String address;
    private String phone;
    private String email;
}
package com.example.ecapp.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItemResponse {
  private Long id;
  private Long productId;
  private String productName;
  private String productDescription;
  private int price;
  private int quantity;
  private int subtotal; // price * quantity
  
}

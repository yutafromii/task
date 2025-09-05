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
public class CartItemResponse {
  private Long id;
  private Long productId;
  private String productName;
  private String productDescription;
  private long price;
  private int quantity;
  private long subtotal; // price * quantity
}

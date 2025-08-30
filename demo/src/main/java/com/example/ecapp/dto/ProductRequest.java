package com.example.ecapp.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductRequest {
  private String name;
  private String description;
  private String fabric;
  private int price;
  private int stock;

  // 画像URLやvariantの登録などは、別APIやServiceで扱う想定
  private List<String> imageUrls; // 追加
}
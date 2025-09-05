package com.example.ecapp.dto;

import java.util.List;

import com.example.ecapp.domain.Category;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductResponse {
  private Long id;
  private String name;
  private String description;
  private String fabric;
  private int price;
  private int stock;
  private Category category;
  private List<String> imageUrls;
  private Boolean isActive;
  private String createdAt;
}

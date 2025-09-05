package com.example.ecapp.dto;

import java.util.List;

import com.example.ecapp.domain.Category;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductRequest {
  @NotBlank
  private String name;

  @NotBlank
  private String description;

  private String fabric;

  @Min(0)
  private int price;

  @Min(0)
  private int stock;

  @NotNull
  private Category category;

  // 順序維持
  private List<String> imageUrls;
}

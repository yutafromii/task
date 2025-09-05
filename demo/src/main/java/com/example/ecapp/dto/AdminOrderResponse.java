package com.example.ecapp.dto;

import java.time.LocalDateTime;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminOrderResponse {
  private Long id;
  private String orderNumber;
  private Long userId;
  private String userName;
  private long total;
  private String status; // 日本語ラベル
  private LocalDateTime orderedAt;
  private List<OrderItemResponse> items;
}


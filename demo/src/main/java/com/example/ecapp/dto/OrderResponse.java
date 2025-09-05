package com.example.ecapp.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class OrderResponse {

    private Long orderId;
    private List<OrderItemResponse> items;
    private long total;
    private LocalDateTime orderedAt;
    private String status; // 日本語ラベル
    private String orderNumber;
}

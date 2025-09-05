package com.example.ecapp.controller;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.ecapp.controller.Base.BaseController;
import com.example.ecapp.common.ApiResponse;
import com.example.ecapp.domain.Order;
import com.example.ecapp.dto.OrderRequest;
import com.example.ecapp.dto.OrderResponse;
import com.example.ecapp.service.OrderService;

@RestController
@RequestMapping("/orders")
public class OrderController extends BaseController<Order, OrderRequest, OrderResponse, OrderService> {
  private final OrderService orderService;

  public OrderController(OrderService orderService) {
    super(orderService);
    this.orderService = orderService;
  }

  // 明示的に一覧取得を無効化（安全のため）
  @Override
  @GetMapping
  public ResponseEntity<List<OrderResponse>> getAll() {
    return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
  }

  // 🔹 ログインユーザーの注文取得（最新1件）
  @GetMapping("/me")
  public ApiResponse<OrderResponse> getMyOrder() {
    return ApiResponse.success(orderService.getOrderByLoginUser());
  }

  // 🔹 履歴一覧（ページングあり）
  @GetMapping(value = "/history", params = {"page"})
  public ResponseEntity<Page<OrderResponse>> getMyHistoryPaged(Pageable pageable) {
    return ResponseEntity.ok(orderService.getHistoryByLoginUser(pageable));
  }

  // 🔹 履歴一覧（全件・新しい順）
  @GetMapping("/history")
  public ApiResponse<List<OrderResponse>> getMyHistory() {
    return ApiResponse.success(orderService.getHistoryByLoginUser());
  }

  // 🔹 ログインユーザーの注文に商品を追加 or 更新
  @PostMapping("/me")
  public ApiResponse<OrderResponse> addOrUpdateOrder(@RequestBody List<OrderRequest> requestList) {
    return ApiResponse.success(orderService.addOrUpdateItemsByLoginUser(requestList));
  }

  // 🔹 注文確定（在庫減算 + カートクリア）
  @PostMapping("/checkout")
  public ApiResponse<OrderResponse> checkout() {
    return ApiResponse.success(orderService.checkoutByLoginUser());
  }
}

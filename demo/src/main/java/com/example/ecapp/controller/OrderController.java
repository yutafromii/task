package com.example.ecapp.controller;

import java.util.List;

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

  // 🔹 ログインユーザーのカート取得
  @GetMapping("/me")
  public ApiResponse<OrderResponse> getMyOrder() {
    return ApiResponse.success(orderService.getOrderByLoginUser());
  }

  // 🔹 ログインユーザーのカートに商品を追加 or 更新
  @PostMapping("/me")
  public ApiResponse<OrderResponse> addOrUpdateOrder(@RequestBody List<OrderRequest> requestList) {
    return ApiResponse.success(orderService.addOrUpdateItemsByLoginUser(requestList));
  }
}

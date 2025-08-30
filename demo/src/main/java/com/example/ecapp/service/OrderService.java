package com.example.ecapp.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.example.ecapp.domain.AppUser;
import com.example.ecapp.domain.Order;
import com.example.ecapp.domain.OrderItem;
import com.example.ecapp.domain.Product;
import com.example.ecapp.dto.OrderItemResponse;
import com.example.ecapp.dto.OrderRequest;
import com.example.ecapp.dto.OrderResponse;
import com.example.ecapp.repository.OrderItemRepository;
import com.example.ecapp.repository.OrderRepository;
import com.example.ecapp.repository.ProductRepository;
import com.example.ecapp.service.Base.AbstractBaseService;
import jakarta.transaction.Transactional;

@Service
@Transactional
public class OrderService extends AbstractBaseService<Order, OrderRequest, OrderResponse> {
  private final OrderRepository orderRepository;
  private final OrderItemRepository orderItemRepository;
  private final ProductRepository productRepository;
  private final UserService userService;

  public OrderService(OrderRepository orderRepository, OrderItemRepository orderItemRepository,
      ProductRepository productRepository, UserService userService) {
    this.orderRepository = orderRepository;
    this.orderItemRepository = orderItemRepository;
    this.productRepository = productRepository;
    this.userService = userService;
  }

  @Override
  protected OrderRepository getRepository() {
    return orderRepository;
  }

  @Override
  protected OrderResponse toDto(Order order) {
    List<OrderItemResponse> itemResponses = order.getItems().stream()
        .map(item -> {
          Product product = item.getProduct();
          int subtotal = product.getPrice() * item.getQuantity();
          return OrderItemResponse.builder()
              .id(item.getId())
              .productId(product.getId())
              .productName(product.getName())
              .productDescription(product.getDescription())
              .price(product.getPrice())
              .quantity(item.getQuantity())
              .subtotal(subtotal)
              .build();
        }).collect(Collectors.toList());

    int total = itemResponses.stream().mapToInt(OrderItemResponse::getSubtotal).sum();

    return OrderResponse.builder()
        .orderId(order.getId())
        .items(itemResponses)
        .total(total)
        .build();
  }

  @Override
  protected Order toEntity(OrderRequest request) {
    throw new UnsupportedOperationException("Order の直接作成はサポートされていません");
  }

  @Override
  protected void updateEntity(Order entity, OrderRequest request) {
    throw new UnsupportedOperationException("Order の直接更新はサポートされていません");
  }

  public OrderResponse getOrderByLoginUser() {
    AppUser loginUser = userService.getLoginUser(); // ✅ 修正
    return getOrderByUserId(loginUser.getId());
  }

  // addOrUpdateItemByLoginUser
  public OrderResponse addOrUpdateItemsByLoginUser(List<OrderRequest> requestList) {
    AppUser loginUser = userService.getLoginUser();
    return addToOrder(loginUser.getId(), requestList);
  }

  /**
   * ログインユーザーの注文取得
   */
  public OrderResponse getOrderByUserId(Long userId) {
    Order order = orderRepository.findByUserId(userId)
        .orElseThrow(() -> new IllegalArgumentException("カートが見つかりません"));
    return toDto(order);
  }

  /**
   * 商品を注文履歴に追加（すでにある場合は数量加算）
   */

  public OrderResponse addToOrder(Long userId, List<OrderRequest> requestList) {
    Order order = orderRepository.findByUserId(userId)
        .orElseGet(() -> {
          Order newOrder = new Order();
          AppUser user = new AppUser();
          user.setId(userId);
          newOrder.setUser(user);
          return orderRepository.save(newOrder);
        });

    for (OrderRequest request : requestList) {
      Product product = productRepository.findById(request.getProductId())
          .orElseThrow(() -> new IllegalArgumentException("商品が見つかりません"));

      OrderItem existingItem = order.getItems().stream()
          .filter(i -> i.getProduct().getId().equals(product.getId()))
          .findFirst()
          .orElse(null);

      if (existingItem != null) {
        existingItem.setQuantity(existingItem.getQuantity() + request.getQuantity());
      } else {
        OrderItem newItem = new OrderItem();
        newItem.setProduct(product);
        newItem.setQuantity(request.getQuantity());
        newItem.setOrder(order);
        order.getItems().add(newItem);
      }
    }

    Order updatedOrder = orderRepository.save(order);
    return toDto(updatedOrder);
  }
}

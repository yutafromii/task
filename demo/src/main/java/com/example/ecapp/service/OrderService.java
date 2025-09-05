// src/main/java/com/example/ecapp/service/OrderService.java
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
import com.example.ecapp.repository.CartRepository;
import com.example.ecapp.repository.DeliveryAddressRepository;
import com.example.ecapp.service.Base.AbstractBaseService;

import jakarta.transaction.Transactional;

@Service
@Transactional
public class OrderService extends AbstractBaseService<Order, OrderRequest, OrderResponse> {
  private final OrderRepository orderRepository;
  private final OrderItemRepository orderItemRepository;
  private final ProductRepository productRepository;
  private final UserService userService;
  private final CartRepository cartRepository;
  private final DeliveryAddressRepository deliveryAddressRepository;

  public OrderService(
      OrderRepository orderRepository,
      OrderItemRepository orderItemRepository,
      ProductRepository productRepository,
      UserService userService,
      CartRepository cartRepository,
      DeliveryAddressRepository deliveryAddressRepository
  ) {
    this.orderRepository = orderRepository;
    this.orderItemRepository = orderItemRepository;
    this.productRepository = productRepository;
    this.userService = userService;
    this.cartRepository = cartRepository;
    this.deliveryAddressRepository = deliveryAddressRepository;
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
          long subtotal = ((long) product.getPrice()) * (long) item.getQuantity();
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

    long total = itemResponses.stream().mapToLong(OrderItemResponse::getSubtotal).sum();

    return OrderResponse.builder()
        .orderId(order.getId())
        .items(itemResponses)
        .total(total)
        .orderedAt(order.getOrderedAt())
        .status(order.getStatus() == null ? null : order.getStatus().label())
        .orderNumber(order.getOrderNumber())
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
    AppUser loginUser = userService.getLoginUser();
    return getOrderByUserId(loginUser.getId());
  }

  public List<OrderResponse> getHistoryByLoginUser() {
    AppUser loginUser = userService.getLoginUser();
    return getHistoryByUserId(loginUser.getId());
  }

  public org.springframework.data.domain.Page<OrderResponse> getHistoryByLoginUser(org.springframework.data.domain.Pageable pageable) {
    AppUser loginUser = userService.getLoginUser();
    return getHistoryByUserId(loginUser.getId(), pageable);
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

  public List<OrderResponse> getHistoryByUserId(Long userId) {
    return orderRepository.findByUserIdOrderByOrderedAtDesc(userId)
        .stream().map(this::toDto).collect(Collectors.toList());
  }

  public org.springframework.data.domain.Page<OrderResponse> getHistoryByUserId(Long userId, org.springframework.data.domain.Pageable pageable) {
    // デフォルトは orderedAt desc
    org.springframework.data.domain.Pageable sorted = pageable;
    if (pageable.getSort().isUnsorted()) {
      sorted = org.springframework.data.domain.PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(),
          org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.DESC, "orderedAt"));
    }
    return orderRepository.findByUserId(userId, sorted).map(this::toDto);
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
          newOrder.setOrderedAt(java.time.LocalDateTime.now());
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

  // ========= 住所チェック用のヘルパ =========

  /** プロフィールに“配送に十分な住所”が入っているか（name, postalCode, prefecture, city, addressLine1|address） */
  private boolean hasValidProfileAddress(AppUser u) {
    if (u == null) return false;
    return notBlank(u.getName())
        && notBlank(u.getPostalCode())
        && notBlank(u.getPrefecture())
        && notBlank(u.getCity())
        && (notBlank(u.getAddressLine1()) || notBlank(u.getAddress()));
  }

  private boolean notBlank(String s) {
    return s != null && !s.isBlank();
  }

  // =======================================

  /**
   * カートの中身で在庫を確定し、注文を確定する（在庫を減算）。
   * ※ 住所未登録（住所帳にもプロフィールにも無い）なら拒否
   */
  public OrderResponse checkoutByLoginUser() {
    AppUser loginUser = userService.getLoginUser();
    Long userId = loginUser.getId();

    // ★★★ 住所ガード：住所帳 or プロフィールのどちらにも有効な住所が無ければ拒否
    boolean hasAddressBook = deliveryAddressRepository.existsByUserId(userId);
    if (!hasAddressBook && !hasValidProfileAddress(loginUser)) {
      throw new IllegalArgumentException("配送先住所が未登録です");
    }

    // カートを取得
    var cartOpt = cartRepository.findByUserId(userId);
    if (cartOpt.isEmpty() || cartOpt.get().getItems().isEmpty()) {
      throw new IllegalArgumentException("カートが空です");
    }

    var cart = cartOpt.get();

    // 注文エンティティ（新規）
    Order order = new Order();
    AppUser user = new AppUser();
    user.setId(userId);
    order.setUser(user);
    order.setOrderedAt(java.time.LocalDateTime.now());

    // 各商品について在庫をロックして確定
    for (var item : cart.getItems()) {
      var product = productRepository.findByIdForUpdate(item.getProduct().getId());
      if (product == null) {
        throw new IllegalArgumentException("商品が見つかりません");
      }
      int qty = item.getQuantity();
      if (qty <= 0) continue;
      if (product.getStock() < qty) {
        throw new IllegalArgumentException("在庫不足: " + product.getName());
      }
      product.setStock(product.getStock() - qty);

      OrderItem newItem = new OrderItem();
      newItem.setProduct(product);
      newItem.setQuantity(qty);
      newItem.setOrder(order);
      order.getItems().add(newItem);
    }

    // 保存（在庫減算はエンティティ更新で反映）
    Order saved = orderRepository.save(order);

    // カートをクリア
    cart.getItems().clear();
    cartRepository.save(cart);

    return toDto(saved);
  }
}

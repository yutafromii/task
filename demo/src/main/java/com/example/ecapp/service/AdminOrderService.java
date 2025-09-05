package com.example.ecapp.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.example.ecapp.domain.Order;
import com.example.ecapp.domain.OrderStatus;
import com.example.ecapp.dto.AdminOrderResponse;
import com.example.ecapp.dto.OrderItemResponse;
import com.example.ecapp.repository.OrderRepository;
import com.example.ecapp.repository.spec.AdminOrderSpecifications;

import jakarta.transaction.Transactional;

@Service
@Transactional
public class AdminOrderService {
  private final OrderRepository orderRepository;

  public AdminOrderService(OrderRepository orderRepository){
    this.orderRepository = orderRepository;
  }

  public List<AdminOrderResponse> list(String statusLabel, String q, LocalDateTime from, LocalDateTime to){
    OrderStatus status = null;
    if (statusLabel != null && !statusLabel.isBlank()) {
      status = OrderStatus.fromLabel(statusLabel);
    }
    Specification<Order> spec = Specification.where(AdminOrderSpecifications.hasStatus(status))
        .and(AdminOrderSpecifications.q(q))
        .and(AdminOrderSpecifications.orderedFrom(from))
        .and(AdminOrderSpecifications.orderedTo(to));

    return orderRepository.findAll(spec, Sort.by(Sort.Direction.DESC, "orderedAt")).stream()
      .map(this::toAdminDto)
      .collect(Collectors.toList());
  }

  public Page<AdminOrderResponse> page(String statusLabel, String q, LocalDateTime from, LocalDateTime to, Pageable pageable){
    OrderStatus status = null;
    if (statusLabel != null && !statusLabel.isBlank()) {
      status = OrderStatus.fromLabel(statusLabel);
    }
    var spec = Specification.where(AdminOrderSpecifications.hasStatus(status))
        .and(AdminOrderSpecifications.q(q))
        .and(AdminOrderSpecifications.orderedFrom(from))
        .and(AdminOrderSpecifications.orderedTo(to));
    Pageable effective = pageable == null ? org.springframework.data.domain.PageRequest.of(0, 20, Sort.by(Sort.Direction.DESC, "orderedAt")) : pageable;
    if (effective.getSort().isUnsorted()) {
      effective = org.springframework.data.domain.PageRequest.of(effective.getPageNumber(), effective.getPageSize(), Sort.by(Sort.Direction.DESC, "orderedAt"));
    }
    return orderRepository.findAll(spec, effective).map(this::toAdminDto);
  }

  public AdminOrderResponse get(Long id){
    Order o = orderRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("order not found"));
    return toAdminDto(o);
  }

  public AdminOrderResponse updateStatus(Long id, String statusLabel){
    if (statusLabel == null || statusLabel.isBlank()) throw new IllegalArgumentException("invalid status");
    OrderStatus next = OrderStatus.fromLabel(statusLabel);
    Order o = orderRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("order not found"));
    // simple transition rule
    if (o.getStatus() == OrderStatus.CANCELED) {
      throw new IllegalStateException("cannot transition from canceled");
    }
    if (o.getStatus() == OrderStatus.SHIPPED && next != OrderStatus.CANCELED) {
      throw new IllegalStateException("cannot transition from shipped");
    }
    o.setStatus(next);
    Order saved = orderRepository.save(o);
    return toAdminDto(saved);
  }

  private AdminOrderResponse toAdminDto(Order order){
    List<OrderItemResponse> items = order.getItems().stream().map(it -> {
      int price = it.getProduct().getPrice();
      int qty = it.getQuantity();
      long subtotal = (long)price * (long)qty;
      return OrderItemResponse.builder()
        .id(it.getId())
        .productId(it.getProduct().getId())
        .productName(it.getProduct().getName())
        .price(price)
        .quantity(qty)
        .subtotal(subtotal)
        .build();
    }).collect(Collectors.toList());
    long total = items.stream().mapToLong(OrderItemResponse::getSubtotal).sum();
    return AdminOrderResponse.builder()
      .id(order.getId())
      .orderNumber(order.getOrderNumber())
      .userId(order.getUser().getId())
      .userName(order.getUser().getName())
      .total(total)
      .status(order.getStatus()==null?null:order.getStatus().label())
      .orderedAt(order.getOrderedAt())
      .items(items)
      .build();
  }
}

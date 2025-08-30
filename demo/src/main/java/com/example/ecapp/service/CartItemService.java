package com.example.ecapp.service;

import org.springframework.stereotype.Service;
import com.example.ecapp.domain.CartItem;
import com.example.ecapp.dto.CartItemRequest;
import com.example.ecapp.dto.CartItemResponse;
import com.example.ecapp.repository.CartItemRepository;
import com.example.ecapp.service.Base.AbstractBaseService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CartItemService extends AbstractBaseService<CartItem, CartItemRequest, CartItemResponse> {

  private final CartItemRepository cartItemRepository;
  private final UserService userService;

  @Override
  protected CartItemRepository getRepository() {
    return cartItemRepository;
  }

  @Override
  protected CartItemResponse toDto(CartItem cartItem) {
    return CartItemResponse.builder()
        .id(cartItem.getId())
        .productId(cartItem.getProduct().getId())
        .productName(cartItem.getProduct().getName())
        .productDescription(cartItem.getProduct().getDescription())
        .price(cartItem.getProduct().getPrice())
        .quantity(cartItem.getQuantity())
        .subtotal(cartItem.getProduct().getPrice() * cartItem.getQuantity())
        .build();
  }

  @Override
  protected CartItem toEntity(CartItemRequest request) {
    throw new UnsupportedOperationException("CartItem の直接作成はサポートされていません");
  }

  @Override
  protected void updateEntity(CartItem entity, CartItemRequest request) {
    throw new UnsupportedOperationException("CartItem の直接更新はサポートされていません");
  }
}
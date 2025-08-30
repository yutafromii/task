package com.example.ecapp.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.ecapp.common.ApiResponse;
import com.example.ecapp.service.CartItemService;

import lombok.RequiredArgsConstructor;


@RestController
@RequestMapping("/cart-items")
@RequiredArgsConstructor
public class CartItemController {

  private final CartItemService cartItemService;

  @DeleteMapping("/{id}")
  public ResponseEntity<?> deleteCartItem(@PathVariable Long id) {
    boolean deleted = cartItemService.delete(id);
    if (deleted) {
      return ResponseEntity.ok(new ApiResponse<>(true, "カートアイテム（ID: " + id + "）を削除しました。", null));
    } else {
      return ResponseEntity.notFound().build();
    }
  }
}

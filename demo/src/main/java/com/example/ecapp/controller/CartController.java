package com.example.ecapp.controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.ecapp.controller.Base.BaseController;
import com.example.ecapp.common.ApiResponse;
import com.example.ecapp.domain.Cart;
import com.example.ecapp.dto.CartRequest;
import com.example.ecapp.dto.CartResponse;
import com.example.ecapp.dto.UpdateCartItemRequest;
import com.example.ecapp.service.CartService;

@RestController
@RequestMapping("/carts")
public class CartController extends BaseController<Cart, CartRequest, CartResponse, CartService>{
  
  private final CartService cartService;

  public CartController(CartService cartService){
    super(cartService);
    this.cartService = cartService;
  }

  // 🔹 ログインユーザーのカート取得
  @GetMapping("/me")
  public ApiResponse<CartResponse> getMyCart() {
    return ApiResponse.success(cartService.getCartByLoginUser());
  }

  // 🔹 ログインユーザーのカートに商品を追加 or 更新
  @PostMapping("/me")
  public ApiResponse<CartResponse> addOrUpdateCart(@RequestBody CartRequest request) {
    return ApiResponse.success(cartService.addOrUpdateItemByLoginUser(request));
  }
  
  // ★ 追加：全削除（フロントの CartAPI.clear() 用）
  @DeleteMapping("/me")
  public ApiResponse<CartResponse> clearMyCart() {
    return ApiResponse.success(cartService.clearCartByLoginUser());
  }

  // ★ 追加：1行削除（CartAPI.deleteItem(cartItemId) 用）
  @DeleteMapping("/me/items/{cartItemId}")
  public ApiResponse<CartResponse> deleteItem(@PathVariable Long cartItemId) {
    return ApiResponse.success(cartService.removeItemByIdForLoginUser(cartItemId));
  }

  // ★ 追加：数量更新（CartAPI.update({ cartItemId, quantity }) 用）
  @PutMapping("/me/items/{cartItemId}")
  public ApiResponse<CartResponse> updateItemQuantity(
      @PathVariable Long cartItemId,
      @RequestBody UpdateCartItemRequest body) {
    return ApiResponse.success(cartService.updateItemQuantityForLoginUser(cartItemId, body.getQuantity()));
  }
}

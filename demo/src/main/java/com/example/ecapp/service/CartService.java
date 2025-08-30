package com.example.ecapp.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.example.ecapp.domain.AppUser;
import com.example.ecapp.domain.Cart;
import com.example.ecapp.domain.CartItem;
import com.example.ecapp.domain.Order;
import com.example.ecapp.domain.OrderItem;
import com.example.ecapp.domain.Product;
import com.example.ecapp.dto.CartRequest;
import com.example.ecapp.dto.CartResponse;
import com.example.ecapp.dto.OrderResponse;
import com.example.ecapp.dto.CartItemResponse;
import com.example.ecapp.repository.CartItemRepository;
import com.example.ecapp.repository.CartRepository;
import com.example.ecapp.repository.OrderRepository;
import com.example.ecapp.repository.ProductRepository;
import com.example.ecapp.service.Base.AbstractBaseService;
import jakarta.transaction.Transactional;

@Service
@Transactional
public class CartService extends AbstractBaseService<Cart, CartRequest, CartResponse> {

  private final CartRepository cartRepository;
  private final ProductRepository productRepository;
  private final UserService userService;
  private final OrderRepository orderRepository;
  private final CartItemRepository cartItemRepository;

  public CartService(CartRepository cartRepository, ProductRepository productRepository, UserService userService,
      OrderRepository orderRepository, CartItemRepository cartItemRepository) {
    this.cartRepository = cartRepository;
    this.productRepository = productRepository;
    this.userService = userService;
    this.orderRepository = orderRepository;
    this.cartItemRepository = cartItemRepository;
  }

  @Override
  protected CartRepository getRepository() {
    return cartRepository;
  }

  @Override
  protected CartResponse toDto(Cart cart) {
    List<CartItemResponse> itemResponses = cart.getItems().stream()
        .map(item -> {
          Product product = item.getProduct();
          int subtotal = product.getPrice() * item.getQuantity();
          return CartItemResponse.builder()
              .id(item.getId())
              .productId(product.getId())
              .productName(product.getName())
              .productDescription(product.getDescription())
              .price(product.getPrice())
              .quantity(item.getQuantity())
              .subtotal(subtotal)
              .build();
        }).collect(Collectors.toList());

    int total = itemResponses.stream().mapToInt(CartItemResponse::getSubtotal).sum();

    return CartResponse.builder()
        .cartId(cart.getId())
        .items(itemResponses)
        .total(total)
        .build();
  }

  @Override
  protected Cart toEntity(CartRequest request) {
    throw new UnsupportedOperationException("Cart の直接作成はサポートされていません");
  }

  @Override
  protected void updateEntity(Cart entity, CartRequest request) {
    throw new UnsupportedOperationException("Cart の直接更新はサポートされていません");
  }

  /**
   * 商品をカートに追加（すでにある場合は数量加算）
   */
  public CartResponse addToCart(Long userId, CartRequest request) {
    Cart cart = cartRepository.findByUserId(userId)
        .orElseGet(() -> {
          Cart newCart = new Cart();

          AppUser user = new AppUser();
          user.setId(userId); // IDだけでOK（永続化済みのユーザーであるため）
          newCart.setUser(user);

          return cartRepository.save(newCart);
        });

    Product product = productRepository.findById(request.getProductId())
        .orElseThrow(() -> new IllegalArgumentException("商品が見つかりません"));

    CartItem existingItem = cart.getItems().stream()
        .filter(i -> i.getProduct().getId().equals(product.getId()))
        .findFirst()
        .orElse(null);

    if (existingItem != null) {
      existingItem.setQuantity(existingItem.getQuantity() + request.getQuantity());
    } else {
      CartItem newItem = new CartItem();
      newItem.setProduct(product);
      newItem.setQuantity(request.getQuantity());
      newItem.setCart(cart);
      cart.getItems().add(newItem);
    }

    Cart updatedCart = cartRepository.save(cart);
    return toDto(updatedCart);
  }

  /**
   * ログインユーザーのカート取得
   */
  public CartResponse getCartByUserId(Long userId) {
    Cart cart = cartRepository.findByUserId(userId)
        .orElseThrow(() -> new IllegalArgumentException("カートが見つかりません"));
    return toDto(cart);
  }

  /**
   * 商品削除（個別）
   */
  public CartResponse removeItem(Long userId, Long productId) {
    Cart cart = cartRepository.findByUserId(userId)
        .orElseThrow(() -> new IllegalArgumentException("カートが見つかりません"));

    cart.getItems().removeIf(item -> item.getProduct().getId().equals(productId));

    Cart updatedCart = cartRepository.save(cart);
    return toDto(updatedCart);
  }

  /**
   * カートを空にする
   */
  public CartResponse clearCart(Long userId) {
    Cart cart = cartRepository.findByUserId(userId)
        .orElseThrow(() -> new IllegalArgumentException("カートが見つかりません"));

    cart.getItems().clear();

    Cart updatedCart = cartRepository.save(cart);
    return toDto(updatedCart);
  }

  public CartResponse getCartByLoginUser() {
    AppUser loginUser = userService.getLoginUser(); // ✅ 修正
    return getCartByUserId(loginUser.getId());
  }

  // 🔁 修正2：addOrUpdateItemByLoginUser
  public CartResponse addOrUpdateItemByLoginUser(CartRequest request) {
    AppUser loginUser = userService.getLoginUser(); // ✅ 修正
    return addToCart(loginUser.getId(), request);
  }
  /** ログインユーザーのカートを空にする */
  public CartResponse clearCartByLoginUser() {
    AppUser loginUser = userService.getLoginUser();
    return clearCart(loginUser.getId());
  }

  /** cartItemId 指定で1行削除（ログインユーザーの所有確認付き） */
  public CartResponse removeItemByIdForLoginUser(Long cartItemId) {
    AppUser loginUser = userService.getLoginUser();
    Long userId = loginUser.getId();

    CartItem item = cartItemRepository.findById(cartItemId)
        .orElseThrow(() -> new IllegalArgumentException("カートアイテムが見つかりません"));
    // 所有チェック
    if (item.getCart() == null || item.getCart().getUser() == null
        || !userId.equals(item.getCart().getUser().getId())) {
      throw new IllegalArgumentException("このカートアイテムを削除する権限がありません");
    }

    Cart cart = item.getCart();
    cart.getItems().remove(item);
    cartItemRepository.delete(item); // 明示削除
    Cart updated = cartRepository.save(cart);
    return toDto(updated);
  }

  /** cartItemId 指定で数量更新（0以下なら削除扱い） */
  public CartResponse updateItemQuantityForLoginUser(Long cartItemId, int quantity) {
    AppUser loginUser = userService.getLoginUser();
    Long userId = loginUser.getId();

    CartItem item = cartItemRepository.findById(cartItemId)
        .orElseThrow(() -> new IllegalArgumentException("カートアイテムが見つかりません"));
    if (item.getCart() == null || item.getCart().getUser() == null
        || !userId.equals(item.getCart().getUser().getId())) {
      throw new IllegalArgumentException("このカートアイテムを更新する権限がありません");
    }

    if (quantity <= 0) {
      // 0以下は削除
      Cart cart = item.getCart();
      cart.getItems().remove(item);
      cartItemRepository.delete(item);
      Cart updated = cartRepository.save(cart);
      return toDto(updated);
    } else {
      item.setQuantity(quantity);
      cartItemRepository.save(item);
      Cart cart = item.getCart();
      return toDto(cart);
    }
  }
}
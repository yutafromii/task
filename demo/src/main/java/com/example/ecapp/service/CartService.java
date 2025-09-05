package com.example.ecapp.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.example.ecapp.domain.AppUser;
import com.example.ecapp.domain.Cart;
import com.example.ecapp.domain.CartItem;
import com.example.ecapp.domain.Product;
import com.example.ecapp.dto.CartRequest;
import com.example.ecapp.dto.CartResponse;
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

  /** お一人様上限 */
  private static final int LIMIT_PER_PERSON = 2;

  public CartService(
      CartRepository cartRepository,
      ProductRepository productRepository,
      UserService userService,
      OrderRepository orderRepository,
      CartItemRepository cartItemRepository
  ) {
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
          long subtotal = ((long) product.getPrice()) * (long) item.getQuantity();
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

    long total = itemResponses.stream().mapToLong(CartItemResponse::getSubtotal).sum();

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
   * 在庫と「お一人様2点まで」を上限としてクランプします。
   */
  public CartResponse addToCart(Long userId, CartRequest request) {
    Cart cart = cartRepository.findByUserId(userId)
        .orElseGet(() -> {
          Cart newCart = new Cart();
          AppUser user = new AppUser();
          user.setId(userId);
          newCart.setUser(user);
          return cartRepository.save(newCart);
        });

    Product product = productRepository.findById(request.getProductId())
        .orElseThrow(() -> new IllegalArgumentException("商品が見つかりません"));

    CartItem existingItem = cart.getItems().stream()
        .filter(i -> i.getProduct().getId().equals(product.getId()))
        .findFirst()
        .orElse(null);

    int current = (existingItem == null) ? 0 : existingItem.getQuantity();
    int add = Math.max(0, request.getQuantity()); // 負数防止
    int desired = current + add;

    // 在庫と1人2点の両方でクランプ
    int finalQty = clampQty(product, desired);

    // 超過時に 400 を返したい場合は以下に切り替え
    // if (desired > finalQty) throw new IllegalArgumentException("同一商品はお一人様2点までです。");

    if (existingItem != null) {
      if (finalQty <= 0) {
        cart.getItems().remove(existingItem);
        cartItemRepository.delete(existingItem);
      } else {
        existingItem.setQuantity(finalQty);
      }
    } else {
      if (finalQty > 0) {
        CartItem newItem = new CartItem();
        newItem.setProduct(product);
        newItem.setQuantity(finalQty);
        newItem.setCart(cart);
        cart.getItems().add(newItem);
      }
    }

    Cart updatedCart = cartRepository.save(cart);
    return toDto(updatedCart);
  }

  /** ログインユーザーのカート取得（なければ作成） */
  public CartResponse getCartByUserId(Long userId) {
    Cart cart = cartRepository.findByUserId(userId).orElseGet(() -> {
      Cart c = new Cart();
      AppUser u = new AppUser();
      u.setId(userId);
      c.setUser(u);
      return cartRepository.save(c);
    });
    return toDto(cart);
  }

  /** 商品削除（個別／productId 指定） */
  public CartResponse removeItem(Long userId, Long productId) {
    Cart cart = cartRepository.findByUserId(userId)
        .orElseThrow(() -> new IllegalArgumentException("カートが見つかりません"));

    cart.getItems().removeIf(item -> item.getProduct().getId().equals(productId));
    Cart updatedCart = cartRepository.save(cart);
    return toDto(updatedCart);
  }

  /** カートを空にする */
  public CartResponse clearCart(Long userId) {
    Cart cart = cartRepository.findByUserId(userId)
        .orElseThrow(() -> new IllegalArgumentException("カートが見つかりません"));

    cart.getItems().clear();
    Cart updatedCart = cartRepository.save(cart);
    return toDto(updatedCart);
  }

  public CartResponse getCartByLoginUser() {
    AppUser loginUser = userService.getLoginUser();
    return getCartByUserId(loginUser.getId());
  }

  public CartResponse addOrUpdateItemByLoginUser(CartRequest request) {
    AppUser loginUser = userService.getLoginUser();
    return addToCart(loginUser.getId(), request);
  }

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

    if (item.getCart() == null || item.getCart().getUser() == null
        || !userId.equals(item.getCart().getUser().getId())) {
      throw new IllegalArgumentException("このカートアイテムを削除する権限がありません");
    }

    Cart cart = item.getCart();
    cart.getItems().remove(item);
    cartItemRepository.delete(item);
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

    Product product = item.getProduct();
    int finalQty = clampQty(product, quantity);

    if (finalQty <= 0) {
      Cart cart = item.getCart();
      cart.getItems().remove(item);
      cartItemRepository.delete(item);
      Cart updated = cartRepository.save(cart);
      return toDto(updated);
    } else {
      item.setQuantity(finalQty);
      cartItemRepository.save(item);
      Cart cart = item.getCart();
      return toDto(cart);
    }
  }
  /** 在庫とお一人様上限で数量をクランプ（stock は primitive int） */
  private int clampQty(Product product, int desired) {
    int stock = product.getStock(); // ← null にならない
    int upper = Math.min(LIMIT_PER_PERSON, stock);
    return Math.max(0, Math.min(desired, upper));
  }
}

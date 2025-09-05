package com.example.ecapp.service;

import java.util.ArrayList;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;     // ★ 追加
import org.springframework.data.domain.Sort;          // ★ 追加
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.example.ecapp.domain.Category;
import com.example.ecapp.domain.Product;
import com.example.ecapp.dto.ProductRequest;
import com.example.ecapp.dto.ProductResponse;
import com.example.ecapp.repository.ProductRepository;
import com.example.ecapp.repository.spec.ProductSpecifications;
import com.example.ecapp.service.Base.AbstractBaseService;

import jakarta.transaction.Transactional;

@Service
@Transactional
public class ProductService extends AbstractBaseService<Product, ProductRequest, ProductResponse> {
  private final ProductRepository productRepository;
  private final com.example.ecapp.repository.OrderItemRepository orderItemRepository;
  private final com.example.ecapp.repository.CartItemRepository cartItemRepository;

  public ProductService(ProductRepository productRepository,
      com.example.ecapp.repository.OrderItemRepository orderItemRepository,
      com.example.ecapp.repository.CartItemRepository cartItemRepository) {
    this.productRepository = productRepository;
    this.orderItemRepository = orderItemRepository;
    this.cartItemRepository = cartItemRepository;
  }

  @Override
  protected ProductRepository getRepository() {
    return productRepository;
  }

  @Override
  @jakarta.transaction.Transactional(value = jakarta.transaction.Transactional.TxType.SUPPORTS)
  public java.util.Optional<ProductResponse> getById(Long id) {
    return productRepository.findOneById(id).map(this::toDto);
  }

  /** 一覧取得（公開のみ）。既定で createdAt DESC（新しい順） */
  @Override
  public java.util.List<ProductResponse> getAll() {
    var spec = ProductSpecifications.isActive();
    var sort = Sort.by(Sort.Direction.DESC, "createdAt"); // ★ 新しい順
    return productRepository.findAll(Specification.where(spec), sort)
        .stream().map(this::toDto).toList();
  }

  /** 一覧取得（公開/非公開すべて）。既定で createdAt DESC（新しい順） */
  public java.util.List<ProductResponse> getAllIncludingInactive() {
    var sort = Sort.by(Sort.Direction.DESC, "createdAt"); // ★ 新しい順
    return productRepository.findAll(sort).stream().map(this::toDto).toList();
  }

  @Override
  protected ProductResponse toDto(Product entity) {
    return ProductResponse.builder()
        .id(entity.getId())
        .name(entity.getName())
        .description(entity.getDescription())
        .fabric(entity.getFabric())
        .price(entity.getPrice())
        .stock(entity.getStock())
        .category(entity.getCategory())
        // Defensive copy to initialize lazy ElementCollection inside TX
        .imageUrls(entity.getImageUrls() == null
            ? java.util.Collections.emptyList()
            : new ArrayList<>(entity.getImageUrls()))
        // ★ 追加済みフィールド
        .isActive(entity.isActive())
        .createdAt(entity.getCreatedAt() == null
            ? null
            : entity.getCreatedAt()
                  .atZone(java.time.ZoneId.systemDefault())
                  .toOffsetDateTime()
                  .toString())
        .build();
  }

  @Override
  protected Product toEntity(ProductRequest request) {
    Product product = new Product();
    product.setName(request.getName());
    product.setDescription(request.getDescription());
    product.setFabric(request.getFabric());
    product.setPrice(request.getPrice());
    product.setStock(request.getStock());
    product.setCategory(request.getCategory());
    if (request.getImageUrls() != null) {
      java.util.List<String> sanitized = request.getImageUrls().stream()
          .filter(java.util.Objects::nonNull)
          .map(String::trim)
          .filter(s -> !s.isEmpty())
          .collect(java.util.stream.Collectors.toCollection(java.util.ArrayList::new));
      product.setImageUrls(sanitized);
    }
    return product;
  }

  @Override
  protected void updateEntity(Product entity, ProductRequest request) {
    entity.setName(request.getName());
    entity.setDescription(request.getDescription());
    entity.setFabric(request.getFabric());
    entity.setPrice(request.getPrice());
    entity.setStock(request.getStock());
    entity.setCategory(request.getCategory());
    if (request.getImageUrls() != null) {
      java.util.List<String> sanitized = request.getImageUrls().stream()
          .filter(java.util.Objects::nonNull)
          .map(String::trim)
          .filter(s -> !s.isEmpty())
          .collect(java.util.stream.Collectors.toCollection(java.util.ArrayList::new));
      // mutate managed collection instead of replacing it (avoids Immutable list issues)
      if (entity.getImageUrls() == null) {
        entity.setImageUrls(new java.util.ArrayList<>());
      } else {
        entity.getImageUrls().clear();
      }
      entity.getImageUrls().addAll(sanitized);
    }
  }

  /**
   * 検索（公開のみ）。ページャにソート指定が無い場合は createdAt DESC を既定化
   */
  @jakarta.transaction.Transactional(value = jakarta.transaction.Transactional.TxType.SUPPORTS)
  public Page<ProductResponse> search(String categoryStr, String stockFilter, String q, Pageable pageable) {
    Category category = null;
    if (categoryStr != null && !"all".equalsIgnoreCase(categoryStr)) {
      try {
        category = Category.from(categoryStr);
      } catch (Exception ex) {
        throw new IllegalArgumentException("Invalid category: " + categoryStr);
      }
    }

    Specification<Product> spec = Specification.where(ProductSpecifications.isActive());

    if (category != null) {
      spec = spec.and(ProductSpecifications.categoryEquals(category));
    }

    if (stockFilter != null && !"all".equalsIgnoreCase(stockFilter)) {
      if ("in".equalsIgnoreCase(stockFilter)) {
        spec = spec.and(ProductSpecifications.stockIn());
      } else if ("coming".equalsIgnoreCase(stockFilter)) {
        spec = spec.and(ProductSpecifications.stockComing());
      }
    }

    if (q != null && !q.isEmpty()) {
      spec = spec.and(ProductSpecifications.nameContainsIgnoreCase(q));
    }

    // ★ pageable にソートが無ければ createdAt DESC をデフォルトに適用
    Pageable effective = pageable;
    if (pageable == null) {
      effective = PageRequest.of(0, 20, Sort.by(Sort.Direction.DESC, "createdAt"));
    } else if (pageable.getSort() == null || pageable.getSort().isUnsorted()) {
      effective = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(),
          Sort.by(Sort.Direction.DESC, "createdAt"));
    }

    return productRepository.findAll(spec, effective).map(this::toDto);
  }

  // 管理画面からの削除要求に対して、注文で参照されている場合はソフトデリートへフォールバック
  @Override
  public boolean delete(Long id) {
    if (!productRepository.existsById(id)) return false;
    // 先に参照有無を確認し、参照ありなら物理削除を試みない（Txのロールバックを避ける）
    if (orderItemRepository.existsByProductId(id) || cartItemRepository.existsByProductId(id)) {
      return productRepository.findById(id).map(p -> {
        p.setActive(false);
        productRepository.saveAndFlush(p);
        return true;
      }).orElse(false);
    }
    // 参照なし → 物理削除
    productRepository.deleteById(id);
    productRepository.flush();
    return true;
  }

  // ゴミ箱から復元（isActive=true）
  public boolean restore(Long id) {
    return productRepository.findById(id).map(p -> {
      p.setActive(true);
      productRepository.saveAndFlush(p);
      return true;
    }).orElse(false);
  }

  // 完全削除（参照があれば 409 相当の扱いを上位に返すため false を返す）
  public enum PurgeResult { DELETED, CONFLICT, NOT_FOUND }

  public PurgeResult purge(Long id) {
    if (!productRepository.existsById(id)) return PurgeResult.NOT_FOUND;
    if (orderItemRepository.existsByProductId(id) || cartItemRepository.existsByProductId(id)) {
      return PurgeResult.CONFLICT;
    }
    productRepository.deleteById(id);
    productRepository.flush();
    return PurgeResult.DELETED;
  }

  @Override
  protected boolean handleDeleteConstraintViolation(Long id, Exception ex) {
    // 注文明細等から参照されている場合は論理削除
    return productRepository.findById(id).map(p -> {
      p.setActive(false);
      productRepository.save(p);
      return true;
    }).orElse(false);
  }
  
  /** Admin: ページング取得（画像含む）。includeInactive=false なら isActive=true で絞込 */
  @jakarta.transaction.Transactional(value = jakarta.transaction.Transactional.TxType.SUPPORTS)
  public Page<ProductResponse> adminPage(Boolean includeInactive, String q, Pageable pageable) {
    Specification<Product> spec = Specification.where(null);
    if (includeInactive == null || !includeInactive) {
      spec = ProductSpecifications.isActive();
    }
    if (q != null && !q.isBlank()) {
      spec = (spec == null) ? ProductSpecifications.nameContainsIgnoreCase(q) : spec.and(ProductSpecifications.nameContainsIgnoreCase(q));
    }
    Pageable effective = pageable;
    if (pageable == null) {
      effective = PageRequest.of(0, 20, Sort.by(Sort.Direction.DESC, "createdAt"));
    } else if (pageable.getSort() == null || pageable.getSort().isUnsorted()) {
      effective = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), Sort.by(Sort.Direction.DESC, "createdAt"));
    }
    return productRepository.findAll(spec, effective).map(this::toDto);
  }
}

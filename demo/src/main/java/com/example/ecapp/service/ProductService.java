package com.example.ecapp.service;

import org.springframework.stereotype.Service;
import com.example.ecapp.domain.Product;
import com.example.ecapp.dto.ProductRequest;
import com.example.ecapp.dto.ProductResponse;
import com.example.ecapp.repository.ProductRepository;
import com.example.ecapp.service.Base.AbstractBaseService;

import jakarta.transaction.Transactional;

@Service
@Transactional
public class ProductService extends AbstractBaseService<Product, ProductRequest, ProductResponse> {
  private final ProductRepository productRepository;

  public ProductService(ProductRepository productRepository) {
    
    this.productRepository = productRepository;
  }

  @Override
  protected ProductRepository getRepository() {
    return productRepository;
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
        .imageUrls(entity.getImageUrls()) // ✅ 修正：直接取得
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

    // ✅ 修正：画像URLを直接設定
    if (request.getImageUrls() != null) {
      product.setImageUrls(request.getImageUrls());
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
  }

}

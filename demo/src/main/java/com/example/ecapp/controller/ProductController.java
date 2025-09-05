package com.example.ecapp.controller;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.ecapp.controller.Base.BaseController;
import com.example.ecapp.domain.Product;
import com.example.ecapp.dto.ProductRequest;
import com.example.ecapp.dto.ProductResponse;
import com.example.ecapp.service.ProductService;

@RestController
@RequestMapping("/products")
public class ProductController extends BaseController<Product, ProductRequest, ProductResponse, ProductService> {
  private final ProductService productService;

  public ProductController(ProductService productService){
    super(productService);
    this.productService = productService;
  }

  // Use when pagination is present to avoid mapping conflict with BaseController#getAll
  @GetMapping(params = {"page"})
  public ResponseEntity<Page<ProductResponse>> search(
      @RequestParam(name = "category", defaultValue = "all") String category,
      @RequestParam(name = "stock", defaultValue = "all") String stock,
      @RequestParam(name = "q", required = false) String q,
      Pageable pageable) {
    try {
      return ResponseEntity.ok(productService.search(category, stock, q, pageable));
    } catch (IllegalArgumentException e) {
      return ResponseEntity.badRequest().build();
    }
  }

  // 管理用途: 非活性も含めて取得
  @GetMapping(params = {"includeInactive"})
  public ResponseEntity<List<ProductResponse>> getAllWithInactive(@RequestParam("includeInactive") boolean includeInactive) {
    if (includeInactive) {
      return ResponseEntity.ok(productService.getAllIncludingInactive());
    } else {
      return ResponseEntity.ok(productService.getAll());
    }
  }

  // 復元（ゴミ箱→戻す）
  @PostMapping("/{id}/restore")
  public ResponseEntity<?> restore(@PathVariable Long id) {
    boolean ok = productService.restore(id);
    return ok ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
  }

  // 完全削除（参照があれば 409）
  @DeleteMapping("/{id}/purge")
  public ResponseEntity<?> purge(@PathVariable Long id) {
    var result = productService.purge(id);
    return switch (result) {
      case DELETED -> ResponseEntity.noContent().build();
      case NOT_FOUND -> ResponseEntity.notFound().build();
      case CONFLICT -> ResponseEntity.status(409).body("cannot purge: referenced by orders or carts");
    };
  }
}

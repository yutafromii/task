package com.example.ecapp.controller.admin;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.ecapp.dto.ProductResponse;
import com.example.ecapp.service.ProductService;

@RestController
@RequestMapping("/admin/products")
public class AdminProductController {
  private final ProductService productService;
  public AdminProductController(ProductService productService){ this.productService = productService; }

  // ページング一覧（画像含む）。includeInactive=false の場合のみ公開品に限定
  @GetMapping
  public ResponseEntity<Page<ProductResponse>> page(
      @RequestParam(name = "includeInactive", required = false) Boolean includeInactive,
      @RequestParam(name = "q", required = false) String q,
      Pageable pageable){
    return ResponseEntity.ok(productService.adminPage(includeInactive, q, pageable));
  }

  // 詳細（画像含む）
  @GetMapping("/{id}")
  public ResponseEntity<ProductResponse> detail(@PathVariable Long id){
    return productService.getById(id)
      .map(ResponseEntity::ok)
      .orElse(ResponseEntity.notFound().build());
  }
}


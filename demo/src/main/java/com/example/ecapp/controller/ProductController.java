package com.example.ecapp.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.ecapp.controller.Base.BaseController;
import com.example.ecapp.domain.Product;
import com.example.ecapp.dto.ProductRequest;
import com.example.ecapp.dto.ProductResponse;
import com.example.ecapp.service.ProductService;

@RestController
@RequestMapping("/products")
public class ProductController extends BaseController<Product, ProductRequest, ProductResponse, ProductService> {
  public ProductController(ProductService productService){
    super(productService);
  }
}

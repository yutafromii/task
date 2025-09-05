package com.example.ecapp.controller.Base;

import java.util.List;
import java.util.Optional;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.example.ecapp.service.Base.BaseService;
import jakarta.validation.Valid;

public abstract class BaseController<T, RQ, RS, S extends BaseService<T, RQ, RS>> {
  protected final S service;

  protected BaseController(S service) {
    this.service = service;
  }

  @GetMapping
  public ResponseEntity<List<RS>> getAll() {
    return ResponseEntity.ok(service.getAll());
  }

  @GetMapping("/{id}")
  public ResponseEntity<RS> getById(@PathVariable Long id) {
    Optional<RS> result = service.getById(id);
    return result.map(ResponseEntity::ok)
        .orElse(ResponseEntity.notFound().build());
  }

  @PostMapping
  public ResponseEntity<RS> create(@Valid @RequestBody RQ request) {
    RS created = service.create(request);
    return ResponseEntity.ok(created);
  }

  @PutMapping("/{id}")
  public ResponseEntity<RS> update(@PathVariable Long id, @Valid @RequestBody RQ request) {
    try {
      RS updated = service.update(id, request);
      return ResponseEntity.ok(updated);
    } catch (IllegalArgumentException e) {
      return ResponseEntity.notFound().build();
    }
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<?> delete(@PathVariable Long id) {
    boolean deleted = service.delete(id);
    if (deleted) {
      return ResponseEntity.noContent().build(); // 削除成功 → 204
    } else {
      return ResponseEntity.notFound().build(); // 削除失敗 → 404
    }
  }

}

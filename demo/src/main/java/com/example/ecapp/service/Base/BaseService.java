package com.example.ecapp.service.Base;

import java.util.List;
import java.util.Optional;

public interface BaseService<T, RQ, RS> {
  List<RS> getAll();
  Optional<RS> getById(Long id);
  RS create(RQ request);
  RS update(Long id, RQ request);
  boolean delete(Long id);
}

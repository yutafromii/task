package com.example.ecapp.service.Base;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.data.jpa.repository.JpaRepository;


public abstract class AbstractBaseService<T, RQ, RS> implements BaseService<T, RQ, RS> {
  protected abstract JpaRepository<T, Long> getRepository();
  protected abstract RS toDto(T entity);
  protected abstract T toEntity(RQ request);
  protected abstract void updateEntity(T entity, RQ request);

  @Override
  public List<RS> getAll(){
    return getRepository().findAll().stream()
      .map(this::toDto)
      .collect(Collectors.toList());
  }
  @Override
  public Optional<RS> getById(Long id){
    return getRepository().findById(id).map(this::toDto);
  }
  @Override
  public RS create(RQ request){
    T entity = toEntity(request);
    return toDto(getRepository().save(entity));
  }
  @Override
  public RS update(Long id, RQ request){
    T entity = getRepository().findById(id)
      .orElseThrow(() -> new IllegalArgumentException("データが見つかりません"));
    updateEntity(entity, request);
    return toDto(getRepository().save(entity));
  }
  @Override
  public boolean delete(Long id){
    if(!getRepository().existsById(id)){
      return false;
    }
    try {
      getRepository().deleteById(id);
      return true;
    } catch (org.springframework.dao.DataIntegrityViolationException ex) {
      // Allow subclasses to handle FK violations (e.g., soft delete)
      if (handleDeleteConstraintViolation(id, ex)) {
        return true;
      }
      throw ex;
    }
  }

  /**
   * Optional hook for subclasses to handle delete FK constraint violations.
   * Return true if the subclass handled the situation (e.g., performed a soft delete).
   */
  protected boolean handleDeleteConstraintViolation(Long id, Exception ex) { return false; }
}

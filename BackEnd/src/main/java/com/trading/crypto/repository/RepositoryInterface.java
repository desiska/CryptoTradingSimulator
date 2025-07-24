package com.trading.crypto.repository;

import java.util.List;
import java.util.Optional;

public interface RepositoryInterface<T, ID> {
    Long save(T entity);
    int update(T entity);
    Optional<T> findById(ID id);
    List<T> findAll();
    int deleteById(ID id);
    int deleteAll();
}

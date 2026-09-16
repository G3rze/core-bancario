package com.banco.core.dao;

import java.util.List;
import java.util.Optional;

public interface Dao<T> {

    T guardar(T entidad);

    Optional<T> buscarPorId(Long id);

    List<T> listarTodos();
}

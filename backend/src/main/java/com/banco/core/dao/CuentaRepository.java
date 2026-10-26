package com.banco.core.dao;

import com.banco.core.model.entity.Cuenta;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CuentaRepository extends JpaRepository<Cuenta, Long> {

    Optional<Cuenta> findByNumeroCuenta(String numeroCuenta);

    /**
     * Igual que findByNumeroCuenta pero con SELECT ... FOR UPDATE: la fila
     * queda bloqueada en la base hasta que termine la transaccion, asi dos
     * operaciones concurrentes sobre la misma cuenta se serializan aunque
     * cada una tenga su propia instancia Java (un synchronized sobre la
     * entidad no lo logra). Debe llamarse dentro de una transaccion.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from Cuenta c where c.numeroCuenta = :numeroCuenta")
    Optional<Cuenta> buscarConBloqueo(@Param("numeroCuenta") String numeroCuenta);

    List<Cuenta> findByTitular_DuiOrderByNumeroCuenta(String dui);
}

package com.banco.core.dao;

import com.banco.core.model.entity.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ClienteRepository extends JpaRepository<Cliente, Long> {

    Optional<Cliente> findByDui(String dui);

    Optional<Cliente> findByNumeroCliente(String numeroCliente);
}

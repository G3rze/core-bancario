package com.banco.core.dao;

import com.banco.core.model.entity.Empleado;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface EmpleadoRepository extends JpaRepository<Empleado, Long> {

    Optional<Empleado> findByDui(String dui);

    Optional<Empleado> findByCodigoEmpleado(String codigoEmpleado);

    // Consultar directamente por el subtipo Gerente ya filtra por el
    // discriminador de la tabla SINGLE_TABLE (empleados.tipo_empleado).
    // Se devuelve el conteo (long) en vez de comparar ">0" en el propio
    // JPQL: EmpleadoServiceImpl.existeAlgunGerente() hace la comparacion en
    // Java, mas simple de verificar que confiar en que el proveedor JPA
    // soporte una expresion booleana en el SELECT.
    @Query("SELECT COUNT(g) FROM Gerente g")
    long contarGerentes();
}

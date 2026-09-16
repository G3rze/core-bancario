package com.banco.core.dao;

import com.banco.core.model.entity.Cuenta;
import org.springframework.stereotype.Repository;

import java.nio.file.Path;

@Repository
public class CuentaDAO extends ArchivoDao<Cuenta> {

    private static final Path RUTA_POR_DEFECTO = Path.of("datos", "cuentas.dat");

    public CuentaDAO() {
        this(RUTA_POR_DEFECTO);
    }

    public CuentaDAO(Path archivo) {
        super(archivo);
    }

    @Override
    protected Long idDe(Cuenta entidad) {
        return entidad.getId();
    }

    @Override
    protected void asignarId(Cuenta entidad, Long id) {
        entidad.setId(id);
    }
}

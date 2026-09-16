package com.banco.core.dao;

import com.banco.core.model.entity.Transaccion;
import org.springframework.stereotype.Repository;

import java.nio.file.Path;

@Repository
public class TransaccionDAO extends ArchivoDao<Transaccion> {

    private static final Path RUTA_POR_DEFECTO = Path.of("datos", "transacciones.dat");

    public TransaccionDAO() {
        this(RUTA_POR_DEFECTO);
    }

    public TransaccionDAO(Path archivo) {
        super(archivo);
    }

    @Override
    protected Long idDe(Transaccion entidad) {
        return entidad.getId();
    }

    @Override
    protected void asignarId(Transaccion entidad, Long id) {
        entidad.setId(id);
    }
}

package com.banco.core.dao;

import com.banco.core.model.entity.Cliente;
import org.springframework.stereotype.Repository;

import java.nio.file.Path;

@Repository
public class ClienteDAO extends ArchivoDao<Cliente> {

    private static final Path RUTA_POR_DEFECTO = Path.of("datos", "clientes.dat");

    public ClienteDAO() {
        this(RUTA_POR_DEFECTO);
    }

    public ClienteDAO(Path archivo) {
        super(archivo);
    }

    @Override
    protected Long idDe(Cliente entidad) {
        return entidad.getId();
    }

    @Override
    protected void asignarId(Cliente entidad, Long id) {
        entidad.setId(id);
    }
}

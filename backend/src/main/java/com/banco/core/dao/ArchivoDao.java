package com.banco.core.dao;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.EOFException;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Persistencia via serializacion Java a un unico archivo .dat por
 * coleccion: cada operacion relee y reescribe la lista completa. Suficiente
 * para el volumen de datos de este avance academico; el swap a JPA/Postgres
 * reemplazaria esta clase sin tocar Service/Controller, que solo conocen
 * la interfaz Dao<T>.
 */
public abstract class ArchivoDao<T extends Serializable> implements Dao<T> {

    private final Path archivo;
    private final AtomicLong contadorId;

    protected ArchivoDao(Path archivo) {
        this.archivo = archivo;
        long maxId = leerTodos().stream()
                .map(this::idDe)
                .filter(Objects::nonNull)
                .mapToLong(Long::longValue)
                .max()
                .orElse(0L);
        this.contadorId = new AtomicLong(maxId + 1);
    }

    protected abstract Long idDe(T entidad);

    protected abstract void asignarId(T entidad, Long id);

    @Override
    public synchronized T guardar(T entidad) {
        List<T> registros = leerTodos();
        if (idDe(entidad) == null) {
            asignarId(entidad, contadorId.getAndIncrement());
            registros.add(entidad);
        } else {
            registros.removeIf(r -> idDe(entidad).equals(idDe(r)));
            registros.add(entidad);
        }
        escribirTodos(registros);
        return entidad;
    }

    @Override
    public synchronized Optional<T> buscarPorId(Long id) {
        return leerTodos().stream().filter(r -> id.equals(idDe(r))).findFirst();
    }

    @Override
    public synchronized List<T> listarTodos() {
        return leerTodos();
    }

    @SuppressWarnings("unchecked")
    private List<T> leerTodos() {
        if (!Files.exists(archivo)) {
            return new ArrayList<>();
        }
        try (ObjectInputStream in = new ObjectInputStream(
                new BufferedInputStream(new FileInputStream(archivo.toFile())))) {
            return (List<T>) in.readObject();
        } catch (EOFException e) {
            return new ArrayList<>();
        } catch (IOException | ClassNotFoundException e) {
            throw new IllegalStateException("No se pudo leer " + archivo, e);
        }
    }

    private void escribirTodos(List<T> registros) {
        try {
            if (archivo.getParent() != null) {
                Files.createDirectories(archivo.getParent());
            }
            try (ObjectOutputStream out = new ObjectOutputStream(
                    new BufferedOutputStream(new FileOutputStream(archivo.toFile())))) {
                out.writeObject(registros);
            }
        } catch (IOException e) {
            throw new IllegalStateException("No se pudo escribir " + archivo, e);
        }
    }
}

package com.banco.core.service.impl;

import com.banco.core.dao.ClienteRepository;
import com.banco.core.model.entity.Cliente;
import com.banco.core.model.entity.TipoCliente;
import com.banco.core.service.ClienteService;
import jakarta.annotation.PostConstruct;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * Cliente vive en Postgres (ClienteRepository/JPA); ya no hay Map en
 * memoria indexado por DUI como antes de la migracion -Postgres indexa
 * dui/numeroCliente de forma nativa (constraints UNIQUE), asi que un Map
 * duplicado seria una cache redundante, no una optimizacion real a esta
 * escala-.
 */
@Service
public class ClienteServiceImpl implements ClienteService {

    private final ClienteRepository clienteRepository;

    public ClienteServiceImpl(ClienteRepository clienteRepository) {
        this.clienteRepository = clienteRepository;
    }

    @PostConstruct
    void inicializarAlArrancar() {
        // El id lo genera Postgres, pero numeroCliente sigue siendo un
        // AtomicLong en memoria; sin esto, un cliente nuevo tras reiniciar
        // la JVM podria repetir el numero de uno ya persistido.
        clienteRepository.findAll().forEach(cliente -> Cliente.avanzarContador(cliente.getNumeroCliente()));
    }

    @Override
    @Transactional
    public Cliente registrar(String dui, String nombre, String direccion, String telefono, TipoCliente tipo) {
        Cliente cliente = new Cliente(dui, nombre, direccion, telefono, tipo);
        try {
            return clienteRepository.save(cliente);
        } catch (DataIntegrityViolationException e) {
            // La constraint UNIQUE de la columna dui es la que realmente
            // decide esto (mas correcto que un Map en memoria: aguanta
            // aunque corran varias instancias del backend a la vez).
            throw new IllegalArgumentException("Ya existe un cliente registrado con DUI " + dui, e);
        }
    }

    @Override
    public Optional<Cliente> buscarPorDui(String dui) {
        return clienteRepository.findByDui(dui);
    }

    @Override
    public List<Cliente> listarTodos() {
        return clienteRepository.findAll().stream()
                .sorted(Comparator.comparing(Cliente::getNumeroCliente))
                .toList();
    }

    public Optional<Cliente> buscarPorNumeroCliente(String numeroCliente) {
        return clienteRepository.findByNumeroCliente(numeroCliente);
    }

    /**
     * Busqueda combinada para ventanilla (HU-007: "buscar clientes por
     * numero de cliente o DUI").
     */
    public Optional<Cliente> buscarPorDuiONumeroCliente(String identificador) {
        return buscarPorDui(identificador).or(() -> buscarPorNumeroCliente(identificador));
    }
}

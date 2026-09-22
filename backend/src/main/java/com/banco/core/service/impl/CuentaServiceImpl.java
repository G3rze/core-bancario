package com.banco.core.service.impl;

import com.banco.core.dao.CuentaRepository;
import com.banco.core.dao.ClienteRepository;
import com.banco.core.dao.TransaccionDAO;
import com.banco.core.exception.ClienteNoEncontradoException;
import com.banco.core.exception.CuentaNoEncontradaException;
import com.banco.core.model.entity.Cajero;
import com.banco.core.model.entity.Cliente;
import com.banco.core.model.entity.Cuenta;
import com.banco.core.model.entity.CuentaAhorros;
import com.banco.core.model.entity.CuentaCorriente;
import com.banco.core.model.entity.CuentaPlazoFijo;
import com.banco.core.model.entity.Deposito;
import com.banco.core.model.entity.Notificacion;
import com.banco.core.model.entity.RegistroTransaccion;
import com.banco.core.model.entity.Retiro;
import com.banco.core.model.entity.Transaccion;
import com.banco.core.model.entity.Transferencia;
import com.banco.core.service.CuentaService;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

@Service
public class CuentaServiceImpl implements CuentaService {

    private static final Comparator<RegistroTransaccion> ORDEN_CRONOLOGICO =
            Comparator.comparing(RegistroTransaccion::getFecha).thenComparing(RegistroTransaccion::getNumeroTransaccion);

    private final CuentaRepository cuentaRepository;
    private final ClienteRepository clienteRepository;
    private final TransaccionDAO transaccionDAO;

    // Map<numeroCuenta, TreeSet<RegistroTransaccion>>: el historial de una
    // cuenta debe mostrarse en orden cronologico (HU-006); TreeSet mantiene
    // ese orden en cada insercion en vez de tener que ordenar toda la lista
    // en cada consulta. Sigue en memoria (no hay tabla de transacciones en
    // Postgres, transacciones.dat es la unica fuente persistida) porque
    // reconstruirlo leyendo el .dat completo en cada request seria mas caro
    // que mantenerlo actualizado incrementalmente.
    private final Map<String, TreeSet<RegistroTransaccion>> historialPorCuenta = new ConcurrentHashMap<>();

    // Queue<Notificacion>: buffer FIFO de eventos generados por las
    // transacciones. El envio real (push/SMS) esta fuera de alcance de este
    // avance; la cola solo demuestra el patron productor/consumidor, y el
    // orden estricto de entrada/salida es justo lo que Queue garantiza y
    // List/Set no.
    private final Queue<Notificacion> notificaciones = new ConcurrentLinkedQueue<>();

    public CuentaServiceImpl(CuentaRepository cuentaRepository, ClienteRepository clienteRepository,
                              TransaccionDAO transaccionDAO) {
        this.cuentaRepository = cuentaRepository;
        this.clienteRepository = clienteRepository;
        this.transaccionDAO = transaccionDAO;
    }

    @PostConstruct
    void inicializarAlArrancar() {
        // Cliente/Cuenta ya viven en Postgres (el id lo genera la base),
        // pero numeroCuenta sigue siendo un AtomicLong en memoria por tipo;
        // sin esto, una cuenta nueva tras reiniciar la JVM podria repetir
        // el numero de una ya persistida.
        cuentaRepository.findAll().forEach(this::avanzarContadorDeNumero);
        // El historial y el numeroTransaccion si dependen del .dat: se
        // reconstruyen leyendolo completo al arrancar.
        transaccionDAO.listarTodos().forEach(registro -> {
            Transaccion.avanzarContador(registro.getNumeroTransaccion());
            indexarEnHistorial(registro);
        });
    }

    private void avanzarContadorDeNumero(Cuenta cuenta) {
        switch (cuenta) {
            case CuentaAhorros c -> CuentaAhorros.avanzarContador(c.getNumeroCuenta());
            case CuentaCorriente c -> CuentaCorriente.avanzarContador(c.getNumeroCuenta());
            case CuentaPlazoFijo c -> CuentaPlazoFijo.avanzarContador(c.getNumeroCuenta());
            default -> throw new IllegalStateException("Tipo de cuenta desconocido: " + cuenta.getClass());
        }
    }

    @Override
    @Transactional
    public Cuenta abrirCuenta(String duiTitular, String tipoCuenta, BigDecimal saldoInicial, Integer plazoMeses) {
        Cliente titular = clienteRepository.findByDui(duiTitular)
                .orElseThrow(() -> ClienteNoEncontradoException.porDui(duiTitular));
        Cuenta cuenta = crearCuenta(tipoCuenta, titular, saldoInicial, plazoMeses);
        titular.agregarCuenta(cuenta);
        return cuentaRepository.save(cuenta);
    }

    private Cuenta crearCuenta(String tipoCuenta, Cliente titular, BigDecimal saldoInicial, Integer plazoMeses) {
        if (tipoCuenta == null) {
            throw new IllegalArgumentException("El tipo de cuenta es obligatorio");
        }
        return switch (tipoCuenta.toUpperCase()) {
            case "AHORROS" -> new CuentaAhorros(titular, saldoInicial);
            case "CORRIENTE" -> new CuentaCorriente(titular, saldoInicial);
            case "PLAZO_FIJO" -> {
                if (plazoMeses == null) {
                    throw new IllegalArgumentException("Una cuenta a plazo fijo requiere un plazo en meses");
                }
                yield new CuentaPlazoFijo(titular, saldoInicial, plazoMeses);
            }
            default -> throw new IllegalArgumentException("Tipo de cuenta desconocido: " + tipoCuenta);
        };
    }

    @Override
    public Optional<Cuenta> buscarPorNumero(String numeroCuenta) {
        return cuentaRepository.findByNumeroCuenta(numeroCuenta);
    }

    @Override
    public List<Cuenta> listarPorCliente(String duiTitular) {
        return cuentaRepository.findByTitular_DuiOrderByNumeroCuenta(duiTitular);
    }

    @Override
    @Transactional
    public RegistroTransaccion depositar(String numeroCuenta, BigDecimal monto) {
        return depositar(numeroCuenta, monto, null);
    }

    /**
     * Version de ventanilla (HU-007): igual que depositar(), pero deja
     * registrado que cajero la proceso. Cajero es obligatorio aqui -a
     * diferencia del flujo de cliente, donde cajero siempre es null- porque
     * si alguien llega a este metodo es porque vino de la ventanilla.
     */
    @Transactional
    public RegistroTransaccion depositarEnVentanilla(String numeroCuenta, BigDecimal monto, Cajero cajero) {
        return depositar(numeroCuenta, monto, requireCajero(cajero));
    }

    private RegistroTransaccion depositar(String numeroCuenta, BigDecimal monto, Cajero cajero) {
        Cuenta cuenta = obtenerCuenta(numeroCuenta);
        Transaccion transaccion = new Deposito(cuenta, monto);
        transaccion.setCajero(cajero);
        synchronized (cuenta) {
            transaccion.ejecutar();
            cuentaRepository.save(cuenta);
        }
        return persistirTransaccion(transaccion);
    }

    @Override
    @Transactional
    public RegistroTransaccion retirar(String numeroCuenta, BigDecimal monto) {
        return retirar(numeroCuenta, monto, null);
    }

    @Transactional
    public RegistroTransaccion retirarEnVentanilla(String numeroCuenta, BigDecimal monto, Cajero cajero) {
        return retirar(numeroCuenta, monto, requireCajero(cajero));
    }

    private RegistroTransaccion retirar(String numeroCuenta, BigDecimal monto, Cajero cajero) {
        Cuenta cuenta = obtenerCuenta(numeroCuenta);
        Transaccion transaccion = new Retiro(cuenta, monto);
        transaccion.setCajero(cajero);
        synchronized (cuenta) {
            transaccion.ejecutar();
            cuentaRepository.save(cuenta);
        }
        return persistirTransaccion(transaccion);
    }

    @Override
    @Transactional
    public RegistroTransaccion transferir(String numeroCuentaOrigen, String numeroCuentaDestino, BigDecimal monto) {
        return transferir(numeroCuentaOrigen, numeroCuentaDestino, monto, null);
    }

    @Transactional
    public RegistroTransaccion transferirEnVentanilla(String numeroCuentaOrigen, String numeroCuentaDestino,
                                                        BigDecimal monto, Cajero cajero) {
        return transferir(numeroCuentaOrigen, numeroCuentaDestino, monto, requireCajero(cajero));
    }

    private RegistroTransaccion transferir(String numeroCuentaOrigen, String numeroCuentaDestino, BigDecimal monto,
                                            Cajero cajero) {
        Cuenta origen = obtenerCuenta(numeroCuentaOrigen);
        Cuenta destino = obtenerCuenta(numeroCuentaDestino);
        Transaccion transaccion = new Transferencia(origen, destino, monto);
        transaccion.setCajero(cajero);
        // Se bloquean ambas cuentas en un orden fijo (por numero de cuenta)
        // para que dos transferencias cruzadas (A->B y B->A) ejecutandose en
        // hilos distintos al mismo tiempo no puedan deadlockearse esperando
        // cada una el lock que tiene la otra.
        Cuenta primero = origen.getNumeroCuenta().compareTo(destino.getNumeroCuenta()) <= 0 ? origen : destino;
        Cuenta segundo = primero == origen ? destino : origen;
        synchronized (primero) {
            synchronized (segundo) {
                transaccion.ejecutar();
                cuentaRepository.save(origen);
                cuentaRepository.save(destino);
            }
        }
        return persistirTransaccion(transaccion);
    }

    private RegistroTransaccion persistirTransaccion(Transaccion transaccion) {
        RegistroTransaccion registro = RegistroTransaccion.desde(transaccion);
        transaccionDAO.guardar(registro);
        indexarEnHistorial(registro);
        notificaciones.add(Notificacion.deTransaccion(transaccion));
        return registro;
    }

    private void indexarEnHistorial(RegistroTransaccion registro) {
        registro.numerosCuentaInvolucradas().forEach(numeroCuenta -> {
            TreeSet<RegistroTransaccion> historial =
                    historialPorCuenta.computeIfAbsent(numeroCuenta, k -> new TreeSet<>(ORDEN_CRONOLOGICO));
            synchronized (historial) {
                historial.add(registro);
            }
        });
    }

    @Override
    public List<RegistroTransaccion> historial(String numeroCuenta) {
        obtenerCuenta(numeroCuenta); // valida que la cuenta exista (404 si no)
        TreeSet<RegistroTransaccion> historial = historialPorCuenta.get(numeroCuenta);
        if (historial == null) {
            return List.of();
        }
        synchronized (historial) {
            return List.copyOf(historial);
        }
    }

    private Cuenta obtenerCuenta(String numeroCuenta) {
        return buscarPorNumero(numeroCuenta)
                .orElseThrow(() -> CuentaNoEncontradaException.porNumero(numeroCuenta));
    }

    private Cajero requireCajero(Cajero cajero) {
        if (cajero == null) {
            throw new IllegalArgumentException("La transaccion de ventanilla requiere un cajero");
        }
        return cajero;
    }

    /**
     * Capitaliza el interes periodico de las cuentas de ahorro. Es la unica
     * cuenta con capitalizacion periodica en este modelo: CuentaCorriente no
     * genera interes (calcularInteres() siempre da 0, se salta solo por el
     * chequeo de signum) y CuentaPlazoFijo lo devuelve como un monto unico
     * al vencimiento -no capitalizable en el camino, su propio depositar()
     * lo prohibe explicitamente-, asi que se excluye por tipo.
     * <p>
     * Pensada para correr desde un hilo periodico (ver
     * {@code CalculoInteresScheduler}), concurrente con depositos/retiros
     * disparados por requests HTTP sobre las mismas cuentas: calcularInteres()
     * y ejecutar() corren dentro del mismo synchronized(cuenta) que ya usan
     * depositar()/retirar(), para no leer un saldo que un deposito/retiro
     * concurrente esta modificando a mitad de camino. Una cuenta con
     * problemas (p. ej. bloqueada) no debe frenar el calculo de las demas,
     * por eso el catch por cuenta: se reintenta en la siguiente corrida.
     */
    @Transactional
    public void aplicarInteresPeriodico() {
        for (Cuenta cuenta : cuentaRepository.findAll()) {
            if (!(cuenta instanceof CuentaAhorros)) {
                continue;
            }
            try {
                aplicarInteres(cuenta);
            } catch (RuntimeException e) {
                // Se reintenta en la siguiente corrida del scheduler.
            }
        }
    }

    private void aplicarInteres(Cuenta cuenta) {
        Transaccion transaccion;
        synchronized (cuenta) {
            BigDecimal interes = cuenta.calcularInteres();
            if (interes.signum() <= 0) {
                return;
            }
            transaccion = new Deposito(cuenta, interes);
            transaccion.ejecutar();
            cuentaRepository.save(cuenta);
        }
        persistirTransaccion(transaccion);
    }

    /**
     * Drena la cola de notificaciones pendientes (orden FIFO). No hay
     * endpoint que la exponga todavia -push/SMS esta fuera de alcance de
     * este avance-, pero queda disponible para cuando se conecte un
     * despachador real o un test que verifique el orden de generacion.
     */
    public List<Notificacion> obtenerNotificacionesPendientes() {
        List<Notificacion> pendientes = new ArrayList<>();
        Notificacion siguiente;
        while ((siguiente = notificaciones.poll()) != null) {
            pendientes.add(siguiente);
        }
        return pendientes;
    }

    // ==== TODO — Tarea B4 de la planeacion: reglas de negocio ====
    // Cuenta ya valida el deposito minimo de apertura y un limite POR
    // TRANSACCION de retiro (ver Cuenta.java: montoMinimoApertura /
    // limiteRetiroDiario), pero nadie acumula cuanto se ha retirado o
    // transferido en el dia -hoy se puede retirar el limite completo varias
    // veces seguidas el mismo dia-. Para completar HU-004/HU-005 de verdad:
    //   1. Agregar un registro por cuenta de "monto movido hoy" (por
    //      ejemplo Map<LocalDate, BigDecimal> o un par fecha/acumulado que
    //      se reinicia al cambiar el dia).
    //   2. Validar ese acumulado antes de transaccion.ejecutar() en
    //      retirar()/transferir(), sumando el monto de la operacion actual.
    //   3. Decidir si ese estado vive en Cuenta (columna nueva en Postgres,
    //      mas cohesivo con las validaciones que ya tiene) o aqui en el
    //      service (en memoria, mas simple pero se pierde al reiniciar).

    // ==== TODO — Tarea B5 de la planeacion: historial (HU-006) ====
    // historial(numeroCuenta) de arriba devuelve todo el historial sin
    // filtrar. Falta:
    //   1. Filtrar por tipo de transaccion (RegistroTransaccion.getTipo()
    //      ya trae "Deposito"/"Retiro"/"Transferencia" como String) -
    //      probablemente agregando un parametro a este metodo o uno nuevo
    //      en la interfaz CuentaService.
    //   2. Un resumen de ingresos vs. egresos para el DTO que consuma el
    //      frontend.
    // El TreeSet ya esta ordenado cronologicamente, asi que ambos son un
    // filter()/reduce() sobre la lista que ya devuelve historial().
}

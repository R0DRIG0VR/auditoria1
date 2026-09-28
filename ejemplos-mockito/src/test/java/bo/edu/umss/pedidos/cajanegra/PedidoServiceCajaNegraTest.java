package bo.edu.umss.pedidos.cajanegra;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import bo.edu.umss.pedidos.modelo.EstadoPedido;
import bo.edu.umss.pedidos.modelo.ItemPedido;
import bo.edu.umss.pedidos.modelo.Pedido;
import bo.edu.umss.pedidos.modelo.Producto;
import bo.edu.umss.pedidos.modelo.ResultadoPago;
import bo.edu.umss.pedidos.modelo.StockInsuficienteException;
import bo.edu.umss.pedidos.modelo.TipoCliente;
import bo.edu.umss.pedidos.puertos.InventarioRepository;
import bo.edu.umss.pedidos.puertos.Notificador;
import bo.edu.umss.pedidos.puertos.PasarelaPago;
import bo.edu.umss.pedidos.puertos.PedidoRepository;
import bo.edu.umss.pedidos.servicio.CalculadoraDescuentos;
import bo.edu.umss.pedidos.servicio.PedidoService;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * PRUEBAS DE CAJA NEGRA del caso de uso "Procesar pedido".
 * Se derivan de los requisitos (tabla de decisión), NO del código:
 *
 *  Regla | Items | Stock suficiente | Pago aprobado | Resultado esperado
 *  R1    | no    | -                | -             | IllegalArgumentException
 *  R2    | sí    | no               | -             | StockInsuficienteException
 *  R3    | sí    | sí               | no            | estado RECHAZADO
 *  R4    | sí    | sí               | sí            | estado PAGADO, total con descuento
 *
 * Los mocks sólo actúan como STUBS (proveen entradas: stock y respuesta del banco).
 * Las aserciones se hacen únicamente sobre las SALIDAS observables; no se usa verify().
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Caja negra - PedidoService (tabla de decisión)")
class PedidoServiceCajaNegraTest {

    @Mock InventarioRepository inventario;
    @Mock PasarelaPago pasarela;
    @Mock Notificador notificador;
    @Mock PedidoRepository pedidoRepository;

    PedidoService servicio;
    final Producto monitor = new Producto("MON-01", "Monitor", 250.0);

    @BeforeEach
    void setUp() {
        servicio = new PedidoService(inventario, pasarela, notificador, pedidoRepository,
                new CalculadoraDescuentos());
    }

    private Pedido pedidoDe(int cantidad) {
        return new Pedido("PED-100", "cliente@umss.edu.bo", List.of(new ItemPedido(monitor, cantidad)));
    }

    @Test
    @DisplayName("R1: pedido sin items es rechazado")
    void r1PedidoSinItems() {
        Pedido vacio = new Pedido("PED-0", "c@umss.edu.bo", List.of());
        assertThrows(IllegalArgumentException.class,
                () -> servicio.procesarPedido(vacio, TipoCliente.REGULAR));
    }

    @Test
    @DisplayName("R2: sin stock suficiente se lanza StockInsuficienteException")
    void r2SinStock() {
        when(inventario.stockDisponible("MON-01")).thenReturn(1);
        assertThrows(StockInsuficienteException.class,
                () -> servicio.procesarPedido(pedidoDe(2), TipoCliente.REGULAR));
    }

    @Test
    @DisplayName("R3: pago rechazado deja el pedido en estado RECHAZADO")
    void r3PagoRechazado() {
        when(inventario.stockDisponible("MON-01")).thenReturn(10);
        when(pasarela.cobrar(anyString(), anyDouble())).thenReturn(ResultadoPago.rechazado("Tarjeta vencida"));

        Pedido resultado = servicio.procesarPedido(pedidoDe(2), TipoCliente.REGULAR);

        assertEquals(EstadoPedido.RECHAZADO, resultado.getEstado());
    }

    @Test
    @DisplayName("R4: pago aprobado -> PAGADO y total = 500 - 10 % = 450")
    void r4PagoAprobado() {
        when(inventario.stockDisponible("MON-01")).thenReturn(10);
        when(pasarela.cobrar(anyString(), anyDouble())).thenReturn(ResultadoPago.aprobado("TX-1"));
        when(pedidoRepository.guardar(any())).thenAnswer(inv -> inv.getArgument(0));

        Pedido resultado = servicio.procesarPedido(pedidoDe(2), TipoCliente.REGULAR);

        assertEquals(EstadoPedido.PAGADO, resultado.getEstado());
        assertEquals(450.0, resultado.getTotal(), 0.001);
    }

    @Test
    @DisplayName("Valor límite de stock: stock == cantidad es suficiente")
    void limiteStockIgualACantidad() {
        when(inventario.stockDisponible("MON-01")).thenReturn(2);
        when(pasarela.cobrar(anyString(), anyDouble())).thenReturn(ResultadoPago.aprobado("TX-2"));
        when(pedidoRepository.guardar(any())).thenAnswer(inv -> inv.getArgument(0));

        assertEquals(EstadoPedido.PAGADO,
                servicio.procesarPedido(pedidoDe(2), TipoCliente.REGULAR).getEstado());
    }
}

package bo.edu.umss.pedidos.cajablanca;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.startsWith;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * PRUEBAS DE CAJA BLANCA de PedidoService.procesarPedido (técnica: camino básico, McCabe).
 *
 * Decisiones en el código: D1 pedido == null, D2 items.isEmpty(), D3 for validación,
 * D4 stock < cantidad, D5 !pago.aprobado(), D6 for descuento de stock.
 * V(G) = decisiones + 1 = 6 + 1 = 7  →  7 caminos independientes = 7 casos de prueba.
 *
 *  C1: D1=V                                  → IllegalArgumentException
 *  C2: D1=F, D2=V                            → IllegalArgumentException
 *  C3: D3 1 iteración, D4=V                  → StockInsuficienteException (sin cobrar)
 *  C4: D3 2 iteraciones, D4=F luego D4=V     → excepción en el 2º item
 *  C5: D4=F, D5=V                            → RECHAZADO, sin descontar ni guardar
 *  C6: D5=F, D6 1 iteración                  → PAGADO
 *  C7: D5=F, D6 3 iteraciones                → PAGADO, descuenta los 3 productos
 *
 * Aquí Mockito es imprescindible: los stubs FUERZAN cada rama y verify() DEMUESTRA
 * qué instrucciones internas se ejecutaron (o no) en cada camino.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Caja blanca - PedidoService.procesarPedido (camino básico)")
class PedidoServiceCajaBlancaTest {

    @Mock InventarioRepository inventario;
    @Mock PasarelaPago pasarela;
    @Mock Notificador notificador;
    @Mock PedidoRepository pedidoRepository;
    @Mock CalculadoraDescuentos calculadora;
    @InjectMocks PedidoService servicio;

    static final Producto P1 = new Producto("P1", "Cuaderno", 10.0);
    static final Producto P2 = new Producto("P2", "Lapicero", 2.0);
    static final Producto P3 = new Producto("P3", "Mochila", 80.0);
    static final String EMAIL = "est@umss.edu.bo";

    @Test
    @DisplayName("C1: pedido null")
    void c1PedidoNull() {
        assertThrows(IllegalArgumentException.class, () -> servicio.procesarPedido(null, TipoCliente.REGULAR));
        verifyNoInteractions(inventario, pasarela, notificador, pedidoRepository, calculadora);
    }

    @Test
    @DisplayName("C2: pedido sin items")
    void c2PedidoVacio() {
        Pedido pedido = new Pedido("X", EMAIL, List.of());
        assertThrows(IllegalArgumentException.class, () -> servicio.procesarPedido(pedido, TipoCliente.REGULAR));
        verifyNoInteractions(inventario, pasarela, notificador, pedidoRepository, calculadora);
    }

    @Test
    @DisplayName("C3: stock insuficiente en el primer item")
    void c3SinStockPrimerItem() {
        Pedido pedido = new Pedido("X", EMAIL, List.of(new ItemPedido(P1, 5)));
        when(inventario.stockDisponible("P1")).thenReturn(4);

        assertThrows(StockInsuficienteException.class, () -> servicio.procesarPedido(pedido, TipoCliente.REGULAR));

        verifyNoInteractions(pasarela, calculadora, pedidoRepository, notificador);
        verify(inventario, never()).descontarStock(anyString(), anyInt());
    }

    @Test
    @DisplayName("C4: el bucle itera una vez y falla en el segundo item")
    void c4SinStockSegundoItem() {
        Pedido pedido = new Pedido("X", EMAIL, List.of(new ItemPedido(P1, 1), new ItemPedido(P2, 3)));
        when(inventario.stockDisponible("P1")).thenReturn(10);
        when(inventario.stockDisponible("P2")).thenReturn(0);

        StockInsuficienteException ex = assertThrows(StockInsuficienteException.class,
                () -> servicio.procesarPedido(pedido, TipoCliente.REGULAR));

        assertEquals("Stock insuficiente para el producto P2", ex.getMessage());
        verify(inventario).stockDisponible("P1");
        verify(inventario).stockDisponible("P2");
        verifyNoInteractions(pasarela);
    }

    @Test
    @DisplayName("C5: pago rechazado")
    void c5PagoRechazado() {
        Pedido pedido = new Pedido("X", EMAIL, List.of(new ItemPedido(P1, 2)));
        when(inventario.stockDisponible("P1")).thenReturn(2);
        when(calculadora.calcularDescuento(20.0, TipoCliente.REGULAR)).thenReturn(0.0);
        when(pasarela.cobrar(EMAIL, 20.0)).thenReturn(ResultadoPago.rechazado("Sin fondos"));

        Pedido resultado = servicio.procesarPedido(pedido, TipoCliente.REGULAR);

        assertEquals(EstadoPedido.RECHAZADO, resultado.getEstado());
        verify(notificador).enviar(EMAIL, "Pago rechazado: Sin fondos");
        verify(inventario, never()).descontarStock(anyString(), anyInt());
        verify(pedidoRepository, never()).guardar(any());
    }

    @Test
    @DisplayName("C6: pago aprobado con un item")
    void c6PagoAprobadoUnItem() {
        Pedido pedido = new Pedido("PED-6", EMAIL, List.of(new ItemPedido(P3, 1)));
        when(inventario.stockDisponible("P3")).thenReturn(1);
        when(calculadora.calcularDescuento(80.0, TipoCliente.VIP)).thenReturn(4.0);
        when(pasarela.cobrar(EMAIL, 76.0)).thenReturn(ResultadoPago.aprobado("TX-6"));
        when(pedidoRepository.guardar(pedido)).thenReturn(pedido);

        Pedido resultado = servicio.procesarPedido(pedido, TipoCliente.VIP);

        assertSame(pedido, resultado);
        assertEquals(EstadoPedido.PAGADO, resultado.getEstado());
        assertEquals(76.0, resultado.getTotal());
        verify(inventario).descontarStock("P3", 1);
        verify(notificador).enviar(EMAIL, "Pedido PED-6 confirmado. Total: 76.0 Bs. Transacción: TX-6");
    }

    @Test
    @DisplayName("C7: pago aprobado con tres items (bucle de descuento itera 3 veces)")
    void c7PagoAprobadoVariosItems() {
        Pedido pedido = new Pedido("PED-7", EMAIL,
                List.of(new ItemPedido(P1, 1), new ItemPedido(P2, 5), new ItemPedido(P3, 2)));
        when(inventario.stockDisponible(anyString())).thenReturn(100);
        when(calculadora.calcularDescuento(180.0, TipoCliente.REGULAR)).thenReturn(9.0);
        when(pasarela.cobrar(EMAIL, 171.0)).thenReturn(ResultadoPago.aprobado("TX-7"));
        when(pedidoRepository.guardar(pedido)).thenReturn(pedido);

        servicio.procesarPedido(pedido, TipoCliente.REGULAR);

        verify(inventario, times(3)).stockDisponible(anyString());
        verify(inventario, times(3)).descontarStock(anyString(), anyInt());
        verify(inventario).descontarStock("P1", 1);
        verify(inventario).descontarStock("P2", 5);
        verify(inventario).descontarStock("P3", 2);
        verify(notificador, times(1)).enviar(eq(EMAIL), startsWith("Pedido PED-7 confirmado"));
    }
}

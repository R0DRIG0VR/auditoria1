package bo.edu.umss.pedidos.pasoapaso;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import bo.edu.umss.pedidos.modelo.EstadoPedido;
import bo.edu.umss.pedidos.modelo.ItemPedido;
import bo.edu.umss.pedidos.modelo.Pedido;
import bo.edu.umss.pedidos.modelo.Producto;
import bo.edu.umss.pedidos.modelo.ResultadoPago;
import bo.edu.umss.pedidos.modelo.TipoCliente;
import bo.edu.umss.pedidos.puertos.InventarioRepository;
import bo.edu.umss.pedidos.puertos.Notificador;
import bo.edu.umss.pedidos.puertos.PasarelaPago;
import bo.edu.umss.pedidos.puertos.PedidoRepository;
import bo.edu.umss.pedidos.servicio.CalculadoraDescuentos;
import bo.edu.umss.pedidos.servicio.PedidoService;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * PASO 4 - VERDE: se programan todas las dependencias del camino
 * (stock, cobro y guardado) y se verifica el ESTADO resultante.
 * Total esperado calculado a mano: 2 x 250 = 500; 500 >= 500 -> 10 %; 500 - 50 = 450.
 */
@ExtendWith(MockitoExtension.class)
class Paso4_VerdeTest {

    @Mock InventarioRepository inventario;
    @Mock PasarelaPago pasarela;
    @Mock Notificador notificador;
    @Mock PedidoRepository pedidoRepository;
    @Spy CalculadoraDescuentos calculadora;
    @InjectMocks PedidoService servicio;

    @Test
    void pedidoValidoQuedaPagadoConDescuento() {
        // Arrange - datos
        Producto monitor = new Producto("MON-01", "Monitor", 250.0);
        Pedido pedido = new Pedido("PED-1", "cliente@umss.edu.bo", List.of(new ItemPedido(monitor, 2)));
        // Arrange - stubs (uno por cada dependencia que el camino consulta)
        when(inventario.stockDisponible("MON-01")).thenReturn(10);
        when(pasarela.cobrar("cliente@umss.edu.bo", 450.0)).thenReturn(ResultadoPago.aprobado("TX-1")); // NUEVO
        when(pedidoRepository.guardar(any(Pedido.class))).thenAnswer(inv -> inv.getArgument(0));      // NUEVO

        // Act
        Pedido resultado = servicio.procesarPedido(pedido, TipoCliente.REGULAR);

        // Assert - estado
        assertEquals(EstadoPedido.PAGADO, resultado.getEstado());
        assertEquals(450.0, resultado.getTotal(), 0.001);
    }
}

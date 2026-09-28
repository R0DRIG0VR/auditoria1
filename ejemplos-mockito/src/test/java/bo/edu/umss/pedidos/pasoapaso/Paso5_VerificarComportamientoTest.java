package bo.edu.umss.pedidos.pasoapaso;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.startsWith;
import static org.mockito.Mockito.verify;
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
 * PASO 5 - Además del estado, se verifica el COMPORTAMIENTO: que el servicio
 * descontó el stock, guardó el pedido y envió la confirmación.
 */
@ExtendWith(MockitoExtension.class)
class Paso5_VerificarComportamientoTest {

    @Mock InventarioRepository inventario;
    @Mock PasarelaPago pasarela;
    @Mock Notificador notificador;
    @Mock PedidoRepository pedidoRepository;
    @Spy CalculadoraDescuentos calculadora;
    @InjectMocks PedidoService servicio;

    @Test
    void pedidoValidoDescuentaStockGuardaYNotifica() {
        Producto monitor = new Producto("MON-01", "Monitor", 250.0);
        Pedido pedido = new Pedido("PED-1", "cliente@umss.edu.bo", List.of(new ItemPedido(monitor, 2)));
        when(inventario.stockDisponible("MON-01")).thenReturn(10);
        when(pasarela.cobrar("cliente@umss.edu.bo", 450.0)).thenReturn(ResultadoPago.aprobado("TX-1"));
        when(pedidoRepository.guardar(any(Pedido.class))).thenAnswer(inv -> inv.getArgument(0));

        Pedido resultado = servicio.procesarPedido(pedido, TipoCliente.REGULAR);

        assertEquals(EstadoPedido.PAGADO, resultado.getEstado());
        verify(inventario).descontarStock("MON-01", 2);                                   // NUEVO
        verify(pedidoRepository).guardar(pedido);                                         // NUEVO
        verify(notificador).enviar(eq("cliente@umss.edu.bo"), startsWith("Pedido PED-1 confirmado")); // NUEVO: todos con matchers
    }
}

package bo.edu.umss.pedidos.basico;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import bo.edu.umss.pedidos.modelo.GeneradorId;
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
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Ejemplo 7: verificación del ORDEN de las llamadas (InOrder),
 * verifyNoMoreInteractions y simulación de métodos estáticos (mockStatic).
 */
@ExtendWith(MockitoExtension.class)
class E07_OrdenYStaticTest {

    @Mock InventarioRepository inventario;
    @Mock PasarelaPago pasarela;
    @Mock Notificador notificador;
    @Mock PedidoRepository pedidoRepository;
    @Spy CalculadoraDescuentos calculadora;
    @InjectMocks PedidoService servicio;

    @Test
    void cobraAntesDeDescontarStockYGuardarAntesDeNotificar() {
        Pedido pedido = new Pedido("PED-3", "eva@umss.edu.bo",
                List.of(new ItemPedido(new Producto("MOU-01", "Mouse", 50.0), 1)));
        when(inventario.stockDisponible("MOU-01")).thenReturn(1);
        when(pasarela.cobrar(anyString(), anyDouble())).thenReturn(ResultadoPago.aprobado("TX-1"));
        when(pedidoRepository.guardar(any())).thenReturn(pedido);

        servicio.procesarPedido(pedido, TipoCliente.REGULAR);

        InOrder orden = inOrder(inventario, pasarela, pedidoRepository, notificador);
        orden.verify(inventario).stockDisponible("MOU-01");
        orden.verify(pasarela).cobrar("eva@umss.edu.bo", 50.0);
        orden.verify(inventario).descontarStock("MOU-01", 1);
        orden.verify(pedidoRepository).guardar(pedido);
        orden.verify(notificador).enviar(anyString(), anyString());
        verifyNoMoreInteractions(pasarela, notificador); // no hubo cobros ni correos extra
    }

    @Test
    void simularMetodoEstatico() {
        // mockStatic sólo afecta dentro del bloque try-with-resources (hilo actual)
        try (MockedStatic<GeneradorId> generador = mockStatic(GeneradorId.class)) {
            generador.when(GeneradorId::nuevoId).thenReturn("PED-FIJO");

            Pedido pedido = Pedido.crear("x@umss.edu.bo", List.of());

            assertEquals("PED-FIJO", pedido.getId());
            generador.verify(GeneradorId::nuevoId, atLeastOnce());
        }
        // fuera del bloque vuelve el comportamiento real (id aleatorio)
        assertEquals(12, Pedido.crear("x@umss.edu.bo", List.of()).getId().length());
    }
}

package bo.edu.umss.pedidos.basico;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.doubleThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Ejemplo 3: Argument matchers (any, eq, argThat...) y ArgumentCaptor
 * para inspeccionar los argumentos con los que se llamó a un mock.
 */
@ExtendWith(MockitoExtension.class)
class E03_ArgumentMatchersYCaptorTest {

    @Mock InventarioRepository inventario;
    @Mock PasarelaPago pasarela;
    @Mock Notificador notificador;
    @Mock PedidoRepository pedidoRepository;
    @Captor ArgumentCaptor<String> mensajeCaptor;
    @Captor ArgumentCaptor<Pedido> pedidoCaptor;

    PedidoService servicio;

    @BeforeEach
    void setUp() {
        // Se combina un mock con un objeto real (CalculadoraDescuentos)
        servicio = new PedidoService(inventario, pasarela, notificador, pedidoRepository,
                new CalculadoraDescuentos());
    }

    @Test
    void matchersYCaptores() {
        Producto laptop = new Producto("LAP-01", "Laptop", 600.0);
        Pedido pedido = new Pedido("PED-7", "luis@umss.edu.bo", List.of(new ItemPedido(laptop, 1)));

        when(inventario.stockDisponible(anyString())).thenReturn(10);            // cualquier String
        when(pasarela.cobrar(eq("luis@umss.edu.bo"), anyDouble()))               // si se usa un matcher,
                .thenReturn(ResultadoPago.aprobado("TX-999"));                   // TODOS deben ser matchers
        when(pedidoRepository.guardar(any(Pedido.class))).thenAnswer(inv -> inv.getArgument(0));

        servicio.procesarPedido(pedido, TipoCliente.REGULAR);

        // Matcher personalizado con lambda (600 - 10% = 540).
        // Para parámetros primitivos se usa doubleThat/intThat: argThat devolvería null -> NPE
        verify(pasarela).cobrar(anyString(), doubleThat(monto -> monto == 540.0));
        verify(notificador).enviar(eq("luis@umss.edu.bo"), contains("TX-999"));

        // ArgumentCaptor: captura el argumento real para hacer aserciones detalladas
        verify(notificador).enviar(anyString(), mensajeCaptor.capture());
        assertTrue(mensajeCaptor.getValue().startsWith("Pedido PED-7 confirmado"));

        verify(pedidoRepository).guardar(pedidoCaptor.capture());
        assertEquals(540.0, pedidoCaptor.getValue().getTotal());
    }
}

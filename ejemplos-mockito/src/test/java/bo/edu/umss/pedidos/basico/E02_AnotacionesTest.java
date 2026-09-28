package bo.edu.umss.pedidos.basico;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import bo.edu.umss.pedidos.modelo.Pedido;
import bo.edu.umss.pedidos.modelo.PedidoNoEncontradoException;
import bo.edu.umss.pedidos.puertos.InventarioRepository;
import bo.edu.umss.pedidos.puertos.Notificador;
import bo.edu.umss.pedidos.puertos.PasarelaPago;
import bo.edu.umss.pedidos.puertos.PedidoRepository;
import bo.edu.umss.pedidos.servicio.CalculadoraDescuentos;
import bo.edu.umss.pedidos.servicio.PedidoService;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Ejemplo 2: @Mock + @InjectMocks + MockitoExtension.
 * La extensión crea los mocks e inyecta todos en el constructor de PedidoService.
 */
@ExtendWith(MockitoExtension.class)
class E02_AnotacionesTest {

    @Mock InventarioRepository inventario;
    @Mock PasarelaPago pasarela;
    @Mock Notificador notificador;
    @Mock PedidoRepository pedidoRepository;
    @Mock CalculadoraDescuentos calculadora;   // también se pueden simular clases concretas

    @InjectMocks PedidoService servicio;        // SUT con sus 5 dependencias falsas

    @Test
    void obtenerPedidoExistente() {
        Pedido pedido = new Pedido("PED-1", "ana@umss.edu.bo", List.of());
        when(pedidoRepository.buscarPorId("PED-1")).thenReturn(Optional.of(pedido));

        Pedido resultado = servicio.obtenerPedido("PED-1");

        assertSame(pedido, resultado);
        verify(pedidoRepository).buscarPorId("PED-1"); // times(1) implícito
    }

    @Test
    void obtenerPedidoInexistenteLanzaExcepcion() {
        when(pedidoRepository.buscarPorId("NO-EXISTE")).thenReturn(Optional.empty());

        assertThrows(PedidoNoEncontradoException.class, () -> servicio.obtenerPedido("NO-EXISTE"));
    }
}

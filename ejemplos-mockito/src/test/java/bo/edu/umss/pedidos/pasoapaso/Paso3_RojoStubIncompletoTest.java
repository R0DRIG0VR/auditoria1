package bo.edu.umss.pedidos.pasoapaso;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import bo.edu.umss.pedidos.modelo.EstadoPedido;
import bo.edu.umss.pedidos.modelo.ItemPedido;
import bo.edu.umss.pedidos.modelo.Pedido;
import bo.edu.umss.pedidos.modelo.Producto;
import bo.edu.umss.pedidos.modelo.TipoCliente;
import bo.edu.umss.pedidos.puertos.InventarioRepository;
import bo.edu.umss.pedidos.puertos.Notificador;
import bo.edu.umss.pedidos.puertos.PasarelaPago;
import bo.edu.umss.pedidos.puertos.PedidoRepository;
import bo.edu.umss.pedidos.servicio.CalculadoraDescuentos;
import bo.edu.umss.pedidos.servicio.PedidoService;
import java.util.List;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * PASO 3 - ROJO otra vez: se programa el stock, pero no la pasarela.
 * Resultado: NullPointerException, porque pasarela.cobrar(...) devuelve null
 * (valor por defecto para objetos) y el servicio hace pago.aprobado().
 * Lección: hay que seguir el camino del código y programar TODAS las
 * dependencias que ese camino consulta.
 * Ejecutar con: ./run.sh rojos
 */
@Disabled("ROJO a propósito: ejecutar con ./run.sh rojos para ver el fallo")
@ExtendWith(MockitoExtension.class)
class Paso3_RojoStubIncompletoTest {

    @Mock InventarioRepository inventario;
    @Mock PasarelaPago pasarela;
    @Mock Notificador notificador;
    @Mock PedidoRepository pedidoRepository;
    @Spy CalculadoraDescuentos calculadora;
    @InjectMocks PedidoService servicio;

    @Test
    void pedidoValidoQuedaPagado() {
        Producto monitor = new Producto("MON-01", "Monitor", 250.0);
        Pedido pedido = new Pedido("PED-1", "cliente@umss.edu.bo", List.of(new ItemPedido(monitor, 2)));
        when(inventario.stockDisponible("MON-01")).thenReturn(10);      // NUEVO: hay stock

        Pedido resultado = servicio.procesarPedido(pedido, TipoCliente.REGULAR);

        assertEquals(EstadoPedido.PAGADO, resultado.getEstado());
    }
}

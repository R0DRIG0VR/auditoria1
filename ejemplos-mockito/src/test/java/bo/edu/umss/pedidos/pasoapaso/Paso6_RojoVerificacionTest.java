package bo.edu.umss.pedidos.pasoapaso;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyString;
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
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * PASO 6 - ROJO en la verificación: el tester "olvidó" el descuento y espera
 * que se cobren 500 Bs. Mockito responde con ArgumentsAreDifferent y muestra
 * la diferencia entre lo esperado (500.0) y lo real (450.0).
 * Lección: los mensajes de Mockito dicen exactamente qué argumento no coincide.
 * Ejecutar con: ./run.sh rojos
 */
@Disabled("ROJO a propósito: ejecutar con ./run.sh rojos para ver el fallo")
@ExtendWith(MockitoExtension.class)
class Paso6_RojoVerificacionTest {

    @Mock InventarioRepository inventario;
    @Mock PasarelaPago pasarela;
    @Mock Notificador notificador;
    @Mock PedidoRepository pedidoRepository;
    @Spy CalculadoraDescuentos calculadora;
    @InjectMocks PedidoService servicio;

    @Test
    void seCobraElSubtotalSinDescuento() {
        Producto monitor = new Producto("MON-01", "Monitor", 250.0);
        Pedido pedido = new Pedido("PED-1", "cliente@umss.edu.bo", List.of(new ItemPedido(monitor, 2)));
        when(inventario.stockDisponible("MON-01")).thenReturn(10);
        when(pasarela.cobrar(anyString(), anyDouble())).thenReturn(ResultadoPago.aprobado("TX-1"));
        when(pedidoRepository.guardar(any(Pedido.class))).thenAnswer(inv -> inv.getArgument(0));

        servicio.procesarPedido(pedido, TipoCliente.REGULAR);

        verify(pasarela).cobrar("cliente@umss.edu.bo", 500.0);   // ERROR del tester: faltó el descuento
    }
}

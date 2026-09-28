package bo.edu.umss.pedidos.basico;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

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
 * Ejemplo 6: estilo BDD (Given / When / Then) con BDDMockito.
 * given() equivale a when() y then().should() equivale a verify().
 */
@ExtendWith(MockitoExtension.class)
class E06_BDDMockitoTest {

    @Mock InventarioRepository inventario;
    @Mock PasarelaPago pasarela;
    @Mock Notificador notificador;
    @Mock PedidoRepository pedidoRepository;
    @Spy CalculadoraDescuentos calculadora = new CalculadoraDescuentos(); // real + espiable
    @InjectMocks PedidoService servicio;

    @Test
    void clienteVipConPagoRechazado() {
        // GIVEN
        Pedido pedido = new Pedido("PED-9", "vip@umss.edu.bo",
                List.of(new ItemPedido(new Producto("TEC-01", "Teclado", 150.0), 2)));
        given(inventario.stockDisponible("TEC-01")).willReturn(5);
        given(pasarela.cobrar(anyString(), anyDouble()))
                .willReturn(ResultadoPago.rechazado("Fondos insuficientes"));

        // WHEN
        Pedido resultado = servicio.procesarPedido(pedido, TipoCliente.VIP);

        // THEN
        assertThat(resultado.getEstado()).isEqualTo(EstadoPedido.RECHAZADO);
        then(calculadora).should().calcularDescuento(300.0, TipoCliente.VIP);
        then(pasarela).should().cobrar("vip@umss.edu.bo", 270.0); // 300 - 10 %
        then(notificador).should().enviar("vip@umss.edu.bo", "Pago rechazado: Fondos insuficientes");
        then(inventario).should(never()).descontarStock(anyString(), anyInt());
        then(pedidoRepository).should(never()).guardar(any());
    }
}

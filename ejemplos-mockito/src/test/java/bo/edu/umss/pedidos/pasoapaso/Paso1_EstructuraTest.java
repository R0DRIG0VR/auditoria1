package bo.edu.umss.pedidos.pasoapaso;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import bo.edu.umss.pedidos.puertos.InventarioRepository;
import bo.edu.umss.pedidos.puertos.Notificador;
import bo.edu.umss.pedidos.puertos.PasarelaPago;
import bo.edu.umss.pedidos.puertos.PedidoRepository;
import bo.edu.umss.pedidos.servicio.CalculadoraDescuentos;
import bo.edu.umss.pedidos.servicio.PedidoService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * PASO 1 - Estructura. Antes de probar nada se prepara el "escenario":
 *  1.1 Se activa Mockito en la clase con @ExtendWith(MockitoExtension.class).
 *  1.2 Se declara un @Mock por cada dependencia EXTERNA del SUT (BD, banco, correo).
 *  1.3 La lógica propia (CalculadoraDescuentos) se deja REAL con @Spy.
 *  1.4 Se declara el SUT con @InjectMocks: Mockito lo construye con los mocks.
 *  1.5 Primera prueba mínima: comprobar que el SUT se pudo construir.
 */
@ExtendWith(MockitoExtension.class)
class Paso1_EstructuraTest {

    @Mock InventarioRepository inventario;
    @Mock PasarelaPago pasarela;
    @Mock Notificador notificador;
    @Mock PedidoRepository pedidoRepository;
    @Spy CalculadoraDescuentos calculadora;
    @InjectMocks PedidoService servicio;

    @Test
    void elServicioSeConstruyeConSusDobles() {
        assertNotNull(servicio);
    }
}

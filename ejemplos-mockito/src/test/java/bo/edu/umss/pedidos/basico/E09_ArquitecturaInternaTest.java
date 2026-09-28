package bo.edu.umss.pedidos.basico;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockingDetails;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import bo.edu.umss.pedidos.modelo.ResultadoPago;
import bo.edu.umss.pedidos.modelo.TipoCliente;
import bo.edu.umss.pedidos.puertos.InventarioRepository;
import bo.edu.umss.pedidos.puertos.PasarelaPago;
import bo.edu.umss.pedidos.servicio.CalculadoraDescuentos;
import java.util.ArrayList;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockingDetails;
import org.mockito.exceptions.misusing.InvalidUseOfMatchersException;
import org.mockito.exceptions.misusing.MissingMethodInvocationException;

/**
 * Ejemplo 9: se abren los componentes internos de Mockito para ver cómo trabajan.
 * MockingDetails expone lo que guarda cada mock: configuración (MockCreationSettings),
 * manejador (MockHandler), invocaciones registradas y stubs (InvocationContainer).
 */
class E09_ArquitecturaInternaTest {

    @Test
    @DisplayName("MockingDetails: la 'radiografía' de un mock")
    void radiografiaDeUnMock() {
        InventarioRepository inventario = mock(InventarioRepository.class);
        when(inventario.stockDisponible("P1")).thenReturn(5);
        inventario.stockDisponible("P1");
        inventario.descontarStock("P1", 2);

        MockingDetails detalles = mockingDetails(inventario);
        System.out.println("--- Radiografía del mock ---");
        System.out.println("¿Es mock?        " + detalles.isMock());
        System.out.println("Tipo simulado:   " + detalles.getMockCreationSettings().getTypeToMock().getSimpleName());
        System.out.println("Respuesta def.:  " + detalles.getMockCreationSettings().getDefaultAnswer());
        System.out.println("MockHandler:     " + detalles.getMockHandler().getClass().getSimpleName());
        System.out.println("Invocaciones:    " + detalles.getInvocations());
        System.out.println("Stubs:           " + detalles.getStubbings());

        assertTrue(detalles.isMock());
        assertFalse(detalles.isSpy());
        assertEquals(InventarioRepository.class, detalles.getMockCreationSettings().getTypeToMock());
        assertEquals(2, detalles.getInvocations().size()); // la llamada dentro de when(...) NO cuenta
        assertEquals(1, detalles.getStubbings().size());
    }

    @Test
    @DisplayName("La llamada 'de ensayo' dentro de when() se descarta del registro")
    void llamadaDeEnsayoNoSeRegistra() {
        InventarioRepository inventario = mock(InventarioRepository.class);

        when(inventario.stockDisponible("P1")).thenReturn(5); // aquí el mock SÍ fue invocado...

        // ...pero InvocationContainerImpl.addAnswer() la borró (removeLast) al convertirla en stub
        assertTrue(mockingDetails(inventario).getInvocations().isEmpty());
    }

    @Test
    @DisplayName("El último stub gana: los stubs se insertan al inicio de la lista (addFirst)")
    void ultimoStubGana() {
        InventarioRepository inventario = mock(InventarioRepository.class);
        when(inventario.stockDisponible("P1")).thenReturn(1);
        when(inventario.stockDisponible("P1")).thenReturn(99);

        assertEquals(99, inventario.stockDisponible("P1"));
    }

    @Test
    @DisplayName("MockMaker inline: un mock de clase concreta ES de esa misma clase (bytecode reescrito)")
    void inlineNoCreaSubclase() {
        CalculadoraDescuentos calc = mock(CalculadoraDescuentos.class);
        InventarioRepository inv = mock(InventarioRepository.class);

        System.out.println("Clase del mock de clase concreta: " + calc.getClass().getName());
        System.out.println("Clase del mock de interfaz:       " + inv.getClass().getName());

        assertSame(CalculadoraDescuentos.class, calc.getClass());   // misma clase, instrumentada
        assertNotEquals(InventarioRepository.class, inv.getClass()); // interfaz: se genera una clase que la implementa
        assertEquals(0.0, calc.calcularDescuento(1000, TipoCliente.VIP)); // su código real NO se ejecuta
    }

    @Test
    @DisplayName("Spy: se crea un mock y se copian los campos del objeto real (LenientCopyTool)")
    void spyCopiaElEstado() {
        ArrayList<String> real = new ArrayList<>();
        real.add("a");
        ArrayList<String> espia = spy(real);

        assertTrue(mockingDetails(espia).isSpy());
        assertEquals(1, espia.size());       // copió el estado
        espia.add("b");
        assertEquals(1, real.size());        // es una COPIA: el original no cambia
    }

    @Test
    @DisplayName("when() usa la ÚLTIMA invocación a un mock guardada en MockingProgress (ThreadLocal)")
    void whenTomaLaUltimaInvocacion() {
        InventarioRepository inventario = mock(InventarioRepository.class);
        ArrayList<String> listaReal = new ArrayList<>();

        inventario.stockDisponible("P1");          // MockingProgress guarda: "última invocación = stockDisponible(P1)"
        when(listaReal.size()).thenReturn(10);     // listaReal NO es mock... ¡pero when() toma esa última invocación!

        assertEquals(10, inventario.stockDisponible("P1")); // se programó el mock, no la lista
        assertEquals(0, listaReal.size());
    }

    @Test
    @DisplayName("when() sin ninguna invocación pendiente -> MissingMethodInvocationException")
    void whenSinMock() {
        InventarioRepository inventario = mock(InventarioRepository.class);
        when(inventario.stockDisponible("P1")).thenReturn(1); // consume la invocación pendiente

        ArrayList<String> listaReal = new ArrayList<>();
        assertThrows(MissingMethodInvocationException.class, () -> when(listaReal.size()).thenReturn(10));
    }

    @Test
    @DisplayName("Pila de matchers: 1 matcher registrado para 2 argumentos -> error")
    void pilaDeMatchersIncompleta() {
        PasarelaPago pasarela = mock(PasarelaPago.class);
        // "a" no es matcher: MatchersBinder encuentra 1 matcher en la pila y el método recibe 2 argumentos
        InvalidUseOfMatchersException ex = assertThrows(InvalidUseOfMatchersException.class,
                () -> when(pasarela.cobrar("a", anyDouble())).thenReturn(ResultadoPago.aprobado("X")));
        assertTrue(ex.getMessage().contains("2 matchers expected, 1 recorded"));
    }
}

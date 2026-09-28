package bo.edu.umss.pedidos.basico;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

import bo.edu.umss.pedidos.modelo.GeneradorId;
import bo.edu.umss.pedidos.modelo.TipoCliente;
import bo.edu.umss.pedidos.puertos.InventarioRepository;
import bo.edu.umss.pedidos.puertos.Notificador;
import bo.edu.umss.pedidos.servicio.CalculadoraDescuentos;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatcher;
import org.mockito.MockMakers;
import org.mockito.MockedConstruction;
import org.mockito.exceptions.base.MockitoException;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

/**
 * Ejemplo 10: componentes de Mockito que se pueden reemplazar o extender.
 * Answer, ArgumentMatcher, MockMaker (inline / subclass / proxy),
 * InvocationListener y mockConstruction.
 */
class E10_ComponentesExtensiblesTest {

    /** Componente Answer propio: calcula la respuesta a partir de la invocación. */
    static class StockSegunCodigo implements Answer<Integer> {
        @Override
        public Integer answer(InvocationOnMock invocacion) {
            String codigo = invocacion.getArgument(0);
            return codigo.startsWith("AGOTADO") ? 0 : 100;
        }
    }

    /** Componente ArgumentMatcher propio: toString() aparece en los mensajes de error. */
    static class EsCorreoUmss implements ArgumentMatcher<String> {
        @Override
        public boolean matches(String correo) {
            return correo != null && correo.endsWith("@umss.edu.bo");
        }

        @Override
        public String toString() {
            return "<correo institucional @umss.edu.bo>";
        }
    }

    @Test
    @DisplayName("Answer propio como respuesta por defecto del mock")
    void answerPropio() {
        InventarioRepository inventario = mock(InventarioRepository.class,
                withSettings().defaultAnswer(new StockSegunCodigo()));

        assertEquals(100, inventario.stockDisponible("P1"));
        assertEquals(0, inventario.stockDisponible("AGOTADO-7"));
    }

    @Test
    @DisplayName("ArgumentMatcher propio")
    void matcherPropio() {
        Notificador notificador = mock(Notificador.class);
        notificador.enviar("ana@umss.edu.bo", "hola");

        verify(notificador).enviar(argThat(new EsCorreoUmss()), anyString());
    }

    @Test
    @DisplayName("Los tres MockMaker: inline (por defecto), subclass y proxy")
    void tresMockMakers() {
        CalculadoraDescuentos inline = mock(CalculadoraDescuentos.class);
        CalculadoraDescuentos subclase = mock(CalculadoraDescuentos.class,
                withSettings().mockMaker(MockMakers.SUBCLASS));
        InventarioRepository proxy = mock(InventarioRepository.class,
                withSettings().mockMaker(MockMakers.PROXY));

        System.out.println("INLINE   -> " + inline.getClass().getName());
        System.out.println("SUBCLASS -> " + subclase.getClass().getName());
        System.out.println("PROXY    -> " + proxy.getClass().getName());

        assertSame(CalculadoraDescuentos.class, inline.getClass());                 // reescribe la clase
        assertTrue(subclase.getClass().getName().contains("MockitoMock"));          // genera una subclase
        assertSame(CalculadoraDescuentos.class, subclase.getClass().getSuperclass());
        assertTrue(Proxy.isProxyClass(proxy.getClass()));                           // java.lang.reflect.Proxy
    }

    @Test
    @DisplayName("El MockMaker subclass no puede simular clases final (el inline sí)")
    void subclassNoSimulaFinal() {
        assertThrows(MockitoException.class,
                () -> mock(GeneradorId.class, withSettings().mockMaker(MockMakers.SUBCLASS)));
        assertTrue(mock(GeneradorId.class).getClass() == GeneradorId.class); // inline: sí puede
    }

    @Test
    @DisplayName("InvocationListener: se entera de cada llamada al mock")
    void listenerDeInvocaciones() {
        List<String> bitacora = new ArrayList<>();
        Notificador notificador = mock(Notificador.class, withSettings()
                .invocationListeners(reporte -> bitacora.add(reporte.getInvocation().toString())));

        notificador.enviar("a@umss.edu.bo", "uno");
        notificador.enviar("b@umss.edu.bo", "dos");

        bitacora.forEach(System.out::println);
        assertEquals(2, bitacora.size());
    }

    @Test
    @DisplayName("mockConstruction: cada 'new' dentro del bloque devuelve un mock")
    void simularConstructor() {
        try (MockedConstruction<CalculadoraDescuentos> construidos = mockConstruction(CalculadoraDescuentos.class,
                (mockCreado, contexto) -> when(mockCreado.calcularDescuento(500, TipoCliente.VIP)).thenReturn(1.0))) {

            CalculadoraDescuentos c = new CalculadoraDescuentos();   // ¡no es el objeto real!

            assertEquals(1.0, c.calcularDescuento(500, TipoCliente.VIP));
            assertEquals(1, construidos.constructed().size());
            assertSame(c, construidos.constructed().get(0));
        }
        CalculadoraDescuentos real = new CalculadoraDescuentos();
        assertEquals(75.0, real.calcularDescuento(500, TipoCliente.VIP)); // fuera del bloque: real
        assertNotSame(null, real);
    }
}

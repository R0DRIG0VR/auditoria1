package bo.edu.umss.pedidos.basico;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import bo.edu.umss.pedidos.puertos.InventarioRepository;
import bo.edu.umss.pedidos.puertos.Notificador;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * Ejemplo 4: simular fallos (thenThrow / doThrow) y programar métodos void
 * (doNothing / doAnswer). Útil para probar el manejo de errores sin romper nada real.
 */
class E04_ExcepcionesYVoidTest {

    @Test
    void thenThrowEnMetodoConRetorno() {
        InventarioRepository inventario = mock(InventarioRepository.class);
        when(inventario.stockDisponible("P-ERR"))
                .thenThrow(new IllegalStateException("Base de datos caída"));

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> inventario.stockDisponible("P-ERR"));
        assertEquals("Base de datos caída", ex.getMessage());
    }

    @Test
    void doThrowEnMetodoVoid() {
        Notificador notificador = mock(Notificador.class);
        // when(...) no compila con métodos void: se usa la familia do...()
        doThrow(new RuntimeException("SMTP no disponible"))
                .when(notificador).enviar(anyString(), anyString());

        assertThrows(RuntimeException.class, () -> notificador.enviar("a@b.com", "hola"));
    }

    @Test
    void doNothingYDoAnswer() {
        Notificador notificador = mock(Notificador.class);
        List<String> bandejaFalsa = new ArrayList<>();

        doNothing().when(notificador).enviar("silencio@x.com", "x"); // comportamiento por defecto, explícito
        doAnswer(inv -> {                                              // lógica personalizada
            bandejaFalsa.add(inv.getArgument(0) + ": " + inv.getArgument(1));
            return null;
        }).when(notificador).enviar(anyString(), anyString());

        notificador.enviar("ana@umss.edu.bo", "Bienvenida");

        assertEquals(List.of("ana@umss.edu.bo: Bienvenida"), bandejaFalsa);
        verify(notificador).enviar("ana@umss.edu.bo", "Bienvenida");
    }
}

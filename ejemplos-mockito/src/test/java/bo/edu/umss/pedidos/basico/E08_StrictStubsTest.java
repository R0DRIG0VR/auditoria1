package bo.edu.umss.pedidos.basico;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

import bo.edu.umss.pedidos.puertos.InventarioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Ejemplo 8: "Strict stubs". Con MockitoExtension, si se programa un stub
 * que el test nunca usa, Mockito falla con UnnecessaryStubbingException.
 * Esto mantiene las pruebas limpias. lenient() desactiva la regla para un stub.
 */
@ExtendWith(MockitoExtension.class)
class E08_StrictStubsTest {

    @Mock InventarioRepository inventario;

    @Test
    void stubNoUsadoMarcadoComoLenient() {
        when(inventario.stockDisponible("A")).thenReturn(1);
        lenient().when(inventario.stockDisponible("B")).thenReturn(2); // sin lenient() el test FALLARÍA

        assertEquals(1, inventario.stockDisponible("A"));
    }
}

package bo.edu.umss.pedidos.basico;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import bo.edu.umss.pedidos.puertos.InventarioRepository;
import bo.edu.umss.pedidos.puertos.PedidoRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Ejemplo 1: el ciclo básico de Mockito sin anotaciones.
 * 1) Crear el mock  2) Programar su comportamiento (stubbing)  3) Ejecutar  4) Verificar.
 */
class E01_PrimerMockTest {

    @Test
    @DisplayName("mock() crea un objeto falso que devuelve valores por defecto")
    void valoresPorDefecto() {
        InventarioRepository inventario = mock(InventarioRepository.class);

        // Sin programar nada, un mock devuelve 0, false, null, colecciones vacías u Optional.empty()
        assertEquals(0, inventario.stockDisponible("P-001"));

        PedidoRepository repo = mock(PedidoRepository.class);
        assertFalse(repo.buscarPorId("X").isPresent());
        assertNull(repo.guardar(null));
    }

    @Test
    @DisplayName("when(...).thenReturn(...) programa la respuesta del mock")
    void stubbingBasico() {
        InventarioRepository inventario = mock(InventarioRepository.class);

        when(inventario.stockDisponible("P-001")).thenReturn(25);
        when(inventario.stockDisponible("P-002")).thenReturn(3, 2, 1); // respuestas consecutivas

        assertEquals(25, inventario.stockDisponible("P-001"));
        assertEquals(3, inventario.stockDisponible("P-002"));
        assertEquals(2, inventario.stockDisponible("P-002"));
        assertEquals(1, inventario.stockDisponible("P-002"));
        assertEquals(1, inventario.stockDisponible("P-002")); // repite la última
        assertEquals(0, inventario.stockDisponible("OTRO"));  // argumento no programado
    }

    @Test
    @DisplayName("verify(...) comprueba que el mock fue llamado")
    void verificacionBasica() {
        InventarioRepository inventario = mock(InventarioRepository.class);

        inventario.descontarStock("P-001", 2);
        inventario.descontarStock("P-001", 2);

        verify(inventario, times(2)).descontarStock("P-001", 2);
        verify(inventario, never()).descontarStock("P-999", 1);
    }
}

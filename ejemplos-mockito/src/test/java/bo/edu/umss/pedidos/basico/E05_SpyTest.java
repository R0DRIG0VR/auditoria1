package bo.edu.umss.pedidos.basico;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import bo.edu.umss.pedidos.modelo.TipoCliente;
import bo.edu.umss.pedidos.servicio.CalculadoraDescuentos;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * Ejemplo 5: spy = objeto REAL envuelto. Ejecuta el código real salvo
 * los métodos que se sobrescriban explícitamente, y permite verify().
 */
class E05_SpyTest {

    @Test
    void spyEjecutaCodigoReal() {
        CalculadoraDescuentos calculadora = spy(new CalculadoraDescuentos());

        assertEquals(10.0, calculadora.calcularDescuento(200, TipoCliente.REGULAR)); // código real: 5 %
        verify(calculadora).calcularDescuento(200, TipoCliente.REGULAR);
    }

    @Test
    void spyConMetodoSobrescrito() {
        CalculadoraDescuentos calculadora = spy(new CalculadoraDescuentos());
        // En spies se recomienda doReturn().when(): when(spy.metodo()) ejecutaría el método real
        doReturn(99.0).when(calculadora).calcularDescuento(1000, TipoCliente.VIP);

        assertEquals(99.0, calculadora.calcularDescuento(1000, TipoCliente.VIP));     // simulado
        assertEquals(50.0, calculadora.calcularDescuento(500, TipoCliente.REGULAR));  // real
    }

    @Test
    void spySobreColeccion() {
        List<String> lista = spy(new ArrayList<>());
        lista.add("uno");
        doReturn(100).when(lista).size();

        assertEquals("uno", lista.get(0)); // real
        assertEquals(100, lista.size());   // simulado
    }
}

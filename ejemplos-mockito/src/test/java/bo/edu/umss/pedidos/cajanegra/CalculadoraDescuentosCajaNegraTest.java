package bo.edu.umss.pedidos.cajanegra;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import bo.edu.umss.pedidos.modelo.TipoCliente;
import bo.edu.umss.pedidos.servicio.CalculadoraDescuentos;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

/**
 * PRUEBAS DE CAJA NEGRA: se diseñan SOLO a partir de la especificación
 * (javadoc de CalculadoraDescuentos), sin mirar el código.
 *
 * Técnicas: Partición en clases de equivalencia + Análisis de valores límite.
 *
 *  Clase | Condición de entrada        | Salida esperada
 *  CE1   | subtotal < 0 (inválida)     | IllegalArgumentException
 *  CE2   | 0 ≤ subtotal < 100          | 0 %   (+5 % si VIP)
 *  CE3   | 100 ≤ subtotal < 500        | 5 %   (+5 % si VIP)
 *  CE4   | subtotal ≥ 500              | 10 %  (+5 % si VIP)
 *  Límites: -0.01 | 0 | 99.99 | 100 | 499.99 | 500
 */
@DisplayName("Caja negra - CalculadoraDescuentos")
class CalculadoraDescuentosCajaNegraTest {

    private final CalculadoraDescuentos calculadora = new CalculadoraDescuentos();

    @ParameterizedTest(name = "[{index}] {0}: subtotal={1}, {2} -> descuento={3}")
    @CsvSource({
            // caso,                       subtotal, tipo,    descuento esperado
            "CE2 representante,             50,      REGULAR, 0.00",
            "CE2 límite inferior,           0,       REGULAR, 0.00",
            "CE2 límite superior,           99.99,   REGULAR, 0.00",
            "CE3 límite inferior,           100,     REGULAR, 5.00",
            "CE3 representante,             300,     REGULAR, 15.00",
            "CE3 límite superior,           499.99,  REGULAR, 25.00",
            "CE4 límite inferior,           500,     REGULAR, 50.00",
            "CE4 representante,             1000,    REGULAR, 100.00",
            "CE2 VIP,                       50,      VIP,     2.50",
            "CE3 VIP límite inferior,       100,     VIP,     10.00",
            "CE4 VIP límite inferior,       500,     VIP,     75.00",
            "CE4 VIP representante,         2000,    VIP,     300.00"
    })
    void clasesValidasYValoresLimite(String caso, double subtotal, TipoCliente tipo, double esperado) {
        assertEquals(esperado, calculadora.calcularDescuento(subtotal, tipo), 0.001, caso);
    }

    @ParameterizedTest(name = "CE1 inválida: subtotal={0}")
    @CsvSource({"-0.01", "-100"})
    void claseInvalidaSubtotalNegativo(double subtotal) {
        assertThrows(IllegalArgumentException.class,
                () -> calculadora.calcularDescuento(subtotal, TipoCliente.REGULAR));
    }

    @Test
    @DisplayName("Un VIP siempre recibe más descuento que un REGULAR (propiedad de la especificación)")
    void vipSiempreMayorQueRegular() {
        for (double s : new double[] {10, 100, 250, 500, 5000}) {
            double regular = calculadora.calcularDescuento(s, TipoCliente.REGULAR);
            double vip = calculadora.calcularDescuento(s, TipoCliente.VIP);
            assertEquals(true, vip > regular, "subtotal " + s);
        }
    }
}

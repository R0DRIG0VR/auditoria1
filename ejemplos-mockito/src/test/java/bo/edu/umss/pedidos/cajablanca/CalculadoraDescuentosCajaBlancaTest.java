package bo.edu.umss.pedidos.cajablanca;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import bo.edu.umss.pedidos.modelo.TipoCliente;
import bo.edu.umss.pedidos.servicio.CalculadoraDescuentos;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * PRUEBAS DE CAJA BLANCA de CalculadoraDescuentos (cobertura de decisiones).
 * Decisiones: d1 subtotal<0, d2 subtotal>=500, d3 subtotal>=100, d4 tipo==VIP → V(G) = 5.
 * Cada decisión se evalúa como verdadera y falsa al menos una vez (100 % de ramas).
 *
 *  Camino | d1 | d2 | d3 | d4 | Entrada
 *  K1     | V  | -  | -  | -  | (-1, REGULAR)
 *  K2     | F  | V  | -  | F  | (600, REGULAR)
 *  K3     | F  | F  | V  | V  | (200, VIP)
 *  K4     | F  | F  | F  | F  | (20, REGULAR)
 *  K5     | F  | V  | -  | V  | (600, VIP)
 */
@DisplayName("Caja blanca - CalculadoraDescuentos (cobertura de ramas)")
class CalculadoraDescuentosCajaBlancaTest {

    private final CalculadoraDescuentos c = new CalculadoraDescuentos();

    @Test void k1RamaNegativo()        { assertThrows(IllegalArgumentException.class, () -> c.calcularDescuento(-1, TipoCliente.REGULAR)); }
    @Test void k2RamaMayor500Regular() { assertEquals(60.0, c.calcularDescuento(600, TipoCliente.REGULAR)); }
    @Test void k3RamaMedioVip()        { assertEquals(20.0, c.calcularDescuento(200, TipoCliente.VIP)); }
    @Test void k4RamaMenor100Regular() { assertEquals(0.0,  c.calcularDescuento(20, TipoCliente.REGULAR)); }
    @Test void k5RamaMayor500Vip()     { assertEquals(90.0, c.calcularDescuento(600, TipoCliente.VIP)); }
}

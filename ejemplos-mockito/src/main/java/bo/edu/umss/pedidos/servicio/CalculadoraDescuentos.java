package bo.edu.umss.pedidos.servicio;

import bo.edu.umss.pedidos.modelo.TipoCliente;

/**
 * Reglas de negocio de descuentos (especificación usada en las pruebas de caja negra):
 * <ul>
 *   <li>subtotal &lt; 0 → IllegalArgumentException</li>
 *   <li>0 ≤ subtotal &lt; 100 → 0 %</li>
 *   <li>100 ≤ subtotal &lt; 500 → 5 %</li>
 *   <li>subtotal ≥ 500 → 10 %</li>
 *   <li>Cliente VIP → +5 % adicional sobre cualquier tramo</li>
 * </ul>
 */
public class CalculadoraDescuentos {

    public double calcularDescuento(double subtotal, TipoCliente tipo) {
        if (subtotal < 0) {
            throw new IllegalArgumentException("El subtotal no puede ser negativo");
        }
        double porcentaje;
        if (subtotal >= 500) {
            porcentaje = 0.10;
        } else if (subtotal >= 100) {
            porcentaje = 0.05;
        } else {
            porcentaje = 0.0;
        }
        if (tipo == TipoCliente.VIP) {
            porcentaje += 0.05;
        }
        return Math.round(subtotal * porcentaje * 100.0) / 100.0;
    }
}

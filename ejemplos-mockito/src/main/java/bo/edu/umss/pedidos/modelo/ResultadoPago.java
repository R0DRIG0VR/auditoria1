package bo.edu.umss.pedidos.modelo;

/** Respuesta de la pasarela de pagos. */
public record ResultadoPago(boolean aprobado, String codigoTransaccion, String mensaje) {

    public static ResultadoPago aprobado(String codigoTransaccion) {
        return new ResultadoPago(true, codigoTransaccion, "OK");
    }

    public static ResultadoPago rechazado(String motivo) {
        return new ResultadoPago(false, null, motivo);
    }
}

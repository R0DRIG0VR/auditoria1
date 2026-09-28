package bo.edu.umss.pedidos.puertos;

import bo.edu.umss.pedidos.modelo.ResultadoPago;

/** Servicio externo de cobro (en producción: API de un banco). */
public interface PasarelaPago {

    ResultadoPago cobrar(String emailCliente, double monto);
}

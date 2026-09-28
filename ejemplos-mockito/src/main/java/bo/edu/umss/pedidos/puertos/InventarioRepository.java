package bo.edu.umss.pedidos.puertos;

/** Acceso al inventario (en producción: base de datos). */
public interface InventarioRepository {

    int stockDisponible(String codigoProducto);

    void descontarStock(String codigoProducto, int cantidad);
}

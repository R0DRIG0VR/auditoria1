package bo.edu.umss.pedidos.modelo;

/** Línea de un pedido: un producto y la cantidad solicitada. */
public record ItemPedido(Producto producto, int cantidad) {

    public double subtotal() {
        return producto.precio() * cantidad;
    }
}

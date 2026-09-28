package bo.edu.umss.pedidos.modelo;

public class PedidoNoEncontradoException extends RuntimeException {

    public PedidoNoEncontradoException(String id) {
        super("No existe el pedido " + id);
    }
}

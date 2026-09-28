package bo.edu.umss.pedidos.modelo;

public class StockInsuficienteException extends RuntimeException {

    public StockInsuficienteException(String codigoProducto) {
        super("Stock insuficiente para el producto " + codigoProducto);
    }
}

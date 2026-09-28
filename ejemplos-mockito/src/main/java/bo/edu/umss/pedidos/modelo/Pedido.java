package bo.edu.umss.pedidos.modelo;

import java.util.List;

/** Pedido de un cliente. */
public class Pedido {

    private final String id;
    private final String emailCliente;
    private final List<ItemPedido> items;
    private EstadoPedido estado = EstadoPedido.CREADO;
    private double total;

    public Pedido(String id, String emailCliente, List<ItemPedido> items) {
        this.id = id;
        this.emailCliente = emailCliente;
        this.items = List.copyOf(items);
    }

    /** Fábrica que genera el identificador con una utilidad estática. */
    public static Pedido crear(String emailCliente, List<ItemPedido> items) {
        return new Pedido(GeneradorId.nuevoId(), emailCliente, items);
    }

    public String getId() { return id; }
    public String getEmailCliente() { return emailCliente; }
    public List<ItemPedido> getItems() { return items; }
    public EstadoPedido getEstado() { return estado; }
    public void setEstado(EstadoPedido estado) { this.estado = estado; }
    public double getTotal() { return total; }
    public void setTotal(double total) { this.total = total; }

    @Override
    public String toString() {
        return "Pedido[" + id + ", " + estado + ", total=" + total + "]";
    }
}

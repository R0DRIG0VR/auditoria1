package bo.edu.umss.pedidos.puertos;

import bo.edu.umss.pedidos.modelo.Pedido;
import java.util.Optional;

/** Persistencia de pedidos. */
public interface PedidoRepository {

    Pedido guardar(Pedido pedido);

    Optional<Pedido> buscarPorId(String id);
}

package bo.edu.umss.pedidos.servicio;

import bo.edu.umss.pedidos.modelo.EstadoPedido;
import bo.edu.umss.pedidos.modelo.ItemPedido;
import bo.edu.umss.pedidos.modelo.Pedido;
import bo.edu.umss.pedidos.modelo.PedidoNoEncontradoException;
import bo.edu.umss.pedidos.modelo.ResultadoPago;
import bo.edu.umss.pedidos.modelo.StockInsuficienteException;
import bo.edu.umss.pedidos.modelo.TipoCliente;
import bo.edu.umss.pedidos.puertos.InventarioRepository;
import bo.edu.umss.pedidos.puertos.Notificador;
import bo.edu.umss.pedidos.puertos.PasarelaPago;
import bo.edu.umss.pedidos.puertos.PedidoRepository;

/**
 * Clase bajo prueba (SUT). Todas sus dependencias se reciben por constructor,
 * lo que permite reemplazarlas por mocks de Mockito en las pruebas.
 */
public class PedidoService {

    private final InventarioRepository inventario;
    private final PasarelaPago pasarela;
    private final Notificador notificador;
    private final PedidoRepository pedidoRepository;
    private final CalculadoraDescuentos calculadora;

    public PedidoService(InventarioRepository inventario, PasarelaPago pasarela,
                         Notificador notificador, PedidoRepository pedidoRepository,
                         CalculadoraDescuentos calculadora) {
        this.inventario = inventario;
        this.pasarela = pasarela;
        this.notificador = notificador;
        this.pedidoRepository = pedidoRepository;
        this.calculadora = calculadora;
    }

    /**
     * Procesa un pedido: valida stock, calcula descuento, cobra, descuenta
     * inventario, guarda y notifica. Complejidad ciclomática V(G) = 7.
     */
    public Pedido procesarPedido(Pedido pedido, TipoCliente tipoCliente) {
        if (pedido == null || pedido.getItems().isEmpty()) {                     // D1, D2
            throw new IllegalArgumentException("El pedido no tiene items");
        }

        double subtotal = 0;
        for (ItemPedido item : pedido.getItems()) {                              // D3
            String codigo = item.producto().codigo();
            if (inventario.stockDisponible(codigo) < item.cantidad()) {          // D4
                throw new StockInsuficienteException(codigo);
            }
            subtotal += item.subtotal();
        }

        double descuento = calculadora.calcularDescuento(subtotal, tipoCliente);
        double total = subtotal - descuento;

        ResultadoPago pago = pasarela.cobrar(pedido.getEmailCliente(), total);
        if (!pago.aprobado()) {                                                  // D5
            pedido.setEstado(EstadoPedido.RECHAZADO);
            notificador.enviar(pedido.getEmailCliente(), "Pago rechazado: " + pago.mensaje());
            return pedido;
        }

        for (ItemPedido item : pedido.getItems()) {                              // D6
            inventario.descontarStock(item.producto().codigo(), item.cantidad());
        }
        pedido.setEstado(EstadoPedido.PAGADO);
        pedido.setTotal(total);
        Pedido guardado = pedidoRepository.guardar(pedido);
        notificador.enviar(pedido.getEmailCliente(),
                "Pedido " + pedido.getId() + " confirmado. Total: " + total
                        + " Bs. Transacción: " + pago.codigoTransaccion());
        return guardado;
    }

    public Pedido obtenerPedido(String id) {
        return pedidoRepository.buscarPorId(id)
                .orElseThrow(() -> new PedidoNoEncontradoException(id));
    }
}

package bo.edu.umss.pedidos.puertos;

/** Envío de notificaciones (en producción: servidor SMTP). */
public interface Notificador {

    void enviar(String destinatario, String mensaje);
}

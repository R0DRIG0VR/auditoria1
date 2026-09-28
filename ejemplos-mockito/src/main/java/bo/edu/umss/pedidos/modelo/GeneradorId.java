package bo.edu.umss.pedidos.modelo;

import java.util.UUID;

/** Utilidad estática (se usa para demostrar Mockito.mockStatic). */
public final class GeneradorId {

    private GeneradorId() {
    }

    public static String nuevoId() {
        return "PED-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}

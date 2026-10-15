package cl.recetaya.dispensacion.integracion;

import java.util.UUID;

public record DetalleRecetaValidada(UUID medicamentoId, int cantidad) {}

package cl.recetaya.dispensacion.transferencia;

import java.util.UUID;

public record RespuestaDetalleDispensacion(UUID medicamentoId, int cantidad) {}

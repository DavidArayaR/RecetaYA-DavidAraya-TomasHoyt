package cl.recetaya.dispensacion.transferencia;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record SolicitudDispensacion(@NotNull UUID recetaId) {}

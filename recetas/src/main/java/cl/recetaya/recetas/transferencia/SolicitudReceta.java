package cl.recetaya.recetas.transferencia;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.util.List;
import java.util.UUID;

public record SolicitudReceta(
        @NotNull @Valid PacienteSolicitud paciente,
        @NotNull UUID sucursalId,
        @NotEmpty @Size(max = 50) List<@NotNull @Valid MedicamentoSolicitud> medicamentos
) {
    public record PacienteSolicitud(
            @NotBlank @Size(max = 20) String documento,
            @NotBlank @Size(max = 120) String nombre
    ) { }

    public record MedicamentoSolicitud(
            @NotNull UUID medicamentoId,
            @NotNull @Positive Integer cantidad
    ) { }
}

package cl.recetaya.autenticacion.transferencia;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SolicitudRegistro(
        @NotBlank @Email @Size(max = 254) String correoElectronico,
        @NotBlank @Size(min = 8, max = 72) String contrasena,
        @NotBlank @Size(max = 120) String nombre) {
}

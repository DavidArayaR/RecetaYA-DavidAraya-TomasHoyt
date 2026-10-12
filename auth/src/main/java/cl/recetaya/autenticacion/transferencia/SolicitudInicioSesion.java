package cl.recetaya.autenticacion.transferencia;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record SolicitudInicioSesion(
        @NotBlank @Email String correoElectronico,
        @NotBlank String contrasena) {
}

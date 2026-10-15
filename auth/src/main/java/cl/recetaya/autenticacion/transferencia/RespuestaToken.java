package cl.recetaya.autenticacion.transferencia;

import java.util.List;

public record RespuestaToken(String tokenAcceso, String tipoToken, long expiraEnSegundos,
                             UsuarioAutenticado usuario) {
    public record UsuarioAutenticado(String id, String correoElectronico, String nombre, List<String> roles) {
    }
}

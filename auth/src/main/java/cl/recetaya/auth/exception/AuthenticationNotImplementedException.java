package cl.recetaya.auth.exception;

public class AuthenticationNotImplementedException extends RuntimeException {

    public AuthenticationNotImplementedException() {
        super("La autenticación todavía no está implementada.");
    }
}

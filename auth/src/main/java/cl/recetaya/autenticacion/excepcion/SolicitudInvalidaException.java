package cl.recetaya.autenticacion.excepcion;

public class SolicitudInvalidaException extends RuntimeException {
    public SolicitudInvalidaException() {
        super("La solicitud contiene campos inválidos o incompletos.");
    }
}

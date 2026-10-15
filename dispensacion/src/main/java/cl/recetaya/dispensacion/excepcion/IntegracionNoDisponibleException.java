package cl.recetaya.dispensacion.excepcion;

public class IntegracionNoDisponibleException extends RuntimeException {
    public IntegracionNoDisponibleException(String mensaje) { super(mensaje); }
}

package cl.recetaya.dispensacion.excepcion;

public class ConflictoDispensacionException extends RuntimeException {
    public ConflictoDispensacionException(String mensaje) { super(mensaje); }
}

package cl.recetaya.recetas.excepcion;

public class ConflictoRecetaException extends RuntimeException {
    public ConflictoRecetaException(String mensaje) { super(mensaje); }
}

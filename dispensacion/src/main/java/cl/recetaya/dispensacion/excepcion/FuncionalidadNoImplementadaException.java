package cl.recetaya.dispensacion.excepcion;

public class FuncionalidadNoImplementadaException extends RuntimeException {
    public FuncionalidadNoImplementadaException() {
        super("La consulta de dispensaciones estará disponible cuando se implemente el registro de entregas.");
    }
}

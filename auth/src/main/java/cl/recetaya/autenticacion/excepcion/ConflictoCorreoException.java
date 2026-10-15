package cl.recetaya.autenticacion.excepcion;

public class ConflictoCorreoException extends RuntimeException {
    public ConflictoCorreoException() {
        super("Ya existe una cuenta con ese correo electrónico.");
    }
}

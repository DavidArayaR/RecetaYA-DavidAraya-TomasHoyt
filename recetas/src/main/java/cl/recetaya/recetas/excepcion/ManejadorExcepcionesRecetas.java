package cl.recetaya.recetas.excepcion;

import jakarta.servlet.http.HttpServletRequest;
import java.time.Instant;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ManejadorExcepcionesRecetas {
    public record ErrorApi(Instant fechaHora, int codigoEstado, String error, String mensaje, String ruta) { }

    @ExceptionHandler(RecursoNoEncontradoException.class)
    public ResponseEntity<ErrorApi> noEncontrado(RecursoNoEncontradoException error, HttpServletRequest peticion) {
        return responder(HttpStatus.NOT_FOUND, error.getMessage(), peticion);
    }

    @ExceptionHandler({ConflictoRecetaException.class, DataIntegrityViolationException.class})
    public ResponseEntity<ErrorApi> conflicto(Exception error, HttpServletRequest peticion) {
        String mensaje = error instanceof ConflictoRecetaException ? error.getMessage()
                : "Conflicto de integridad al guardar la receta.";
        return responder(HttpStatus.CONFLICT, mensaje, peticion);
    }

    @ExceptionHandler({MethodArgumentNotValidException.class, HttpMessageNotReadableException.class,
            IllegalArgumentException.class})
    public ResponseEntity<ErrorApi> datosInvalidos(Exception error, HttpServletRequest peticion) {
        return responder(HttpStatus.BAD_REQUEST, "Solicitud inválida. Revise los campos enviados.", peticion);
    }

    private ResponseEntity<ErrorApi> responder(HttpStatus estado, String mensaje, HttpServletRequest peticion) {
        return ResponseEntity.status(estado).body(new ErrorApi(Instant.now(), estado.value(),
                estado.getReasonPhrase(), mensaje, peticion.getRequestURI()));
    }
}

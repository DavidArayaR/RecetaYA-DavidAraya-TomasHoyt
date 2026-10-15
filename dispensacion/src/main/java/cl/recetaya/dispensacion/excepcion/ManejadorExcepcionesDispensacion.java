package cl.recetaya.dispensacion.excepcion;

import cl.recetaya.dispensacion.transferencia.RespuestaError;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import java.time.Instant;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ManejadorExcepcionesDispensacion {
    @ExceptionHandler(IntegracionNoDisponibleException.class)
    public ResponseEntity<RespuestaError> integracionNoDisponible(
            IntegracionNoDisponibleException excepcion, HttpServletRequest solicitud) {
        return respuesta(HttpStatus.SERVICE_UNAVAILABLE, excepcion.getMessage(), solicitud);
    }

    @ExceptionHandler(ConflictoDispensacionException.class)
    public ResponseEntity<RespuestaError> conflicto(
            ConflictoDispensacionException excepcion, HttpServletRequest solicitud) {
        return respuesta(HttpStatus.CONFLICT, excepcion.getMessage(), solicitud);
    }

    @ExceptionHandler(RecursoNoEncontradoException.class)
    public ResponseEntity<RespuestaError> noEncontrado(
            RecursoNoEncontradoException excepcion, HttpServletRequest solicitud) {
        return respuesta(HttpStatus.NOT_FOUND, excepcion.getMessage(), solicitud);
    }

    @ExceptionHandler({MethodArgumentNotValidException.class, ConstraintViolationException.class})
    public ResponseEntity<RespuestaError> solicitudInvalida(Exception excepcion, HttpServletRequest solicitud) {
        return respuesta(HttpStatus.BAD_REQUEST, "La solicitud contiene campos inválidos.", solicitud);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<RespuestaError> accesoDenegado(
            AccessDeniedException excepcion, HttpServletRequest solicitud) {
        return respuesta(HttpStatus.FORBIDDEN, "No tiene permiso para consultar esta dispensación.", solicitud);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<RespuestaError> errorInterno(Exception excepcion, HttpServletRequest solicitud) {
        return respuesta(HttpStatus.INTERNAL_SERVER_ERROR,
                "Ocurrió un error interno al procesar la solicitud.", solicitud);
    }

    private ResponseEntity<RespuestaError> respuesta(
            HttpStatus estado, String mensaje, HttpServletRequest solicitud) {
        RespuestaError cuerpo = new RespuestaError(
                Instant.now(), estado.value(), estado.getReasonPhrase(), mensaje, solicitud.getRequestURI());
        return ResponseEntity.status(estado).body(cuerpo);
    }
}

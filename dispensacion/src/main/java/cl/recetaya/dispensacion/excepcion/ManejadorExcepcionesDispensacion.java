package cl.recetaya.dispensacion.excepcion;

import cl.recetaya.dispensacion.transferencia.RespuestaError;
import jakarta.servlet.http.HttpServletRequest;
import java.time.Instant;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ManejadorExcepcionesDispensacion {
    @ExceptionHandler(FuncionalidadNoImplementadaException.class)
    public ResponseEntity<RespuestaError> funcionalidadNoImplementada(
            FuncionalidadNoImplementadaException excepcion, HttpServletRequest solicitud) {
        return respuesta(HttpStatus.NOT_IMPLEMENTED, excepcion.getMessage(), solicitud);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<RespuestaError> errorInterno(HttpServletRequest solicitud) {
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

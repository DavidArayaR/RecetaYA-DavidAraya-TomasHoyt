package cl.recetaya.autenticacion.excepcion;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ManejadorExcepcionesAutenticacion {
    @ExceptionHandler(BadCredentialsException.class)
    ProblemDetail credencialesInvalidas() {
        return ProblemDetail.forStatusAndDetail(HttpStatus.UNAUTHORIZED, "Correo electrónico o contraseña inválidos.");
    }

    @ExceptionHandler(ConflictoCorreoException.class)
    ProblemDetail correoDuplicado() {
        return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, "Ya existe una cuenta con ese correo electrónico.");
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ProblemDetail solicitudInvalida(MethodArgumentNotValidException excepcion) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "La solicitud contiene campos inválidos o incompletos.");
    }
}

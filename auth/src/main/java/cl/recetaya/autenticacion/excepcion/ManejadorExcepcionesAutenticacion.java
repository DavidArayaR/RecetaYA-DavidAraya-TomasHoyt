package cl.recetaya.autenticacion.excepcion;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.security.core.AuthenticationException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ManejadorExcepcionesAutenticacion {
    @ExceptionHandler(AuthenticationException.class)
    ProblemDetail credencialesInvalidas() {
        ProblemDetail problema = ProblemDetail.forStatusAndDetail(HttpStatus.UNAUTHORIZED, "Correo electrónico o contraseña inválidos.");
        problema.setTitle("Credenciales inválidas");
        return problema;
    }

    @ExceptionHandler(ConflictoCorreoException.class)
    ProblemDetail correoDuplicado() {
        ProblemDetail problema = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, "Ya existe una cuenta con ese correo electrónico.");
        problema.setTitle("Correo ya registrado");
        return problema;
    }

    @ExceptionHandler(SolicitudInvalidaException.class)
    ProblemDetail contrasenaNoSegura() {
        ProblemDetail problema = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "La contraseña excede el límite seguro permitido.");
        problema.setTitle("Solicitud inválida");
        return problema;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ProblemDetail solicitudInvalida(MethodArgumentNotValidException excepcion) {
        ProblemDetail problema = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "La solicitud contiene campos inválidos o incompletos.");
        problema.setTitle("Solicitud inválida");
        return problema;
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    ProblemDetail jsonInvalido() {
        ProblemDetail problema = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "El cuerpo debe contener JSON válido.");
        problema.setTitle("JSON inválido");
        return problema;
    }
}

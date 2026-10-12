package cl.recetaya.auth.service;

import cl.recetaya.auth.dto.LoginRequest;
import cl.recetaya.auth.exception.AuthenticationNotImplementedException;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    public void login(LoginRequest request) {
        throw new AuthenticationNotImplementedException();
    }
}

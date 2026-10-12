package cl.recetaya.autenticacion.servicio;

import cl.recetaya.autenticacion.entidad.Rol;
import cl.recetaya.autenticacion.entidad.Usuario;
import cl.recetaya.autenticacion.repositorio.RepositorioRol;
import cl.recetaya.autenticacion.repositorio.RepositorioUsuario;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;
import java.util.Set;

@Component
public class ProvisionInicialFarmaceutico implements ApplicationRunner {
    private final RepositorioUsuario repositorioUsuario;
    private final RepositorioRol repositorioRol;
    private final PasswordEncoder codificadorContrasenas;
    private final String correo;
    private final String contrasena;
    private final String nombre;

    public ProvisionInicialFarmaceutico(RepositorioUsuario repositorioUsuario, RepositorioRol repositorioRol,
                                        PasswordEncoder codificadorContrasenas,
                                        @Value("${recetaya.seguridad.usuario-inicial.correo-electronico:}") String correo,
                                        @Value("${recetaya.seguridad.usuario-inicial.contrasena:}") String contrasena,
                                        @Value("${recetaya.seguridad.usuario-inicial.nombre:}") String nombre) {
        this.repositorioUsuario = repositorioUsuario;
        this.repositorioRol = repositorioRol;
        this.codificadorContrasenas = codificadorContrasenas;
        this.correo = correo;
        this.contrasena = contrasena;
        this.nombre = nombre;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments argumentos) {
        boolean sinConfiguracion = correo.isBlank() && contrasena.isBlank() && nombre.isBlank();
        if (sinConfiguracion) {
            return;
        }
        if (correo.isBlank() || contrasena.length() < 8 || nombre.isBlank()) {
            throw new IllegalStateException("Configure correo, nombre y contraseña inicial del farmacéutico; la contraseña debe tener al menos 8 caracteres.");
        }
        String correoNormalizado = correo.trim().toLowerCase(Locale.ROOT);
        if (repositorioUsuario.findByCorreoElectronicoIgnoreCase(correoNormalizado).isPresent()) {
            return;
        }
        Rol rolFarmaceutico = repositorioRol.findByCodigo("FARMACEUTICO")
                .orElseThrow(() -> new IllegalStateException("No está configurado el rol FARMACEUTICO."));
        repositorioUsuario.save(new Usuario(correoNormalizado, codificadorContrasenas.encode(contrasena),
                nombre.trim(), Set.of(rolFarmaceutico)));
    }
}

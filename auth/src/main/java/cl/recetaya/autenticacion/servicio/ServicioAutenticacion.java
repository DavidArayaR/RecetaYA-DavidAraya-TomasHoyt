package cl.recetaya.autenticacion.servicio;

import cl.recetaya.autenticacion.entidad.Rol;
import cl.recetaya.autenticacion.entidad.Usuario;
import cl.recetaya.autenticacion.excepcion.ConflictoCorreoException;
import cl.recetaya.autenticacion.excepcion.SolicitudInvalidaException;
import cl.recetaya.autenticacion.repositorio.RepositorioRol;
import cl.recetaya.autenticacion.repositorio.RepositorioUsuario;
import cl.recetaya.autenticacion.transferencia.RespuestaToken;
import cl.recetaya.autenticacion.transferencia.SolicitudInicioSesion;
import cl.recetaya.autenticacion.transferencia.SolicitudRegistro;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.Duration;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.nio.charset.StandardCharsets;

@Service
public class ServicioAutenticacion {
    private final RepositorioUsuario repositorioUsuario;
    private final RepositorioRol repositorioRol;
    private final PasswordEncoder codificadorContrasenas;
    private final AuthenticationManager administradorAutenticacion;
    private final JwtEncoder codificadorJwt;
    private final String emisor;
    private final Duration duracionToken;

    public ServicioAutenticacion(RepositorioUsuario repositorioUsuario, RepositorioRol repositorioRol,
                                 PasswordEncoder codificadorContrasenas,
                                 AuthenticationManager administradorAutenticacion, JwtEncoder codificadorJwt,
                                 @Value("${recetaya.seguridad.jwt.emisor}") String emisor,
                                 @Value("${recetaya.seguridad.jwt.duracion:PT30M}") Duration duracion) {
        this.repositorioUsuario = repositorioUsuario;
        this.repositorioRol = repositorioRol;
        this.codificadorContrasenas = codificadorContrasenas;
        this.administradorAutenticacion = administradorAutenticacion;
        this.codificadorJwt = codificadorJwt;
        this.emisor = emisor;
        if (duracion.isZero() || duracion.isNegative()) {
            throw new IllegalStateException("La duración del token debe ser mayor que cero.");
        }
        this.duracionToken = duracion;
    }

    @Transactional
    public RespuestaToken registrar(SolicitudRegistro solicitud) {
        validarLongitudContrasena(solicitud.contrasena());
        String correo = normalizarCorreo(solicitud.correoElectronico());
        if (repositorioUsuario.findByCorreoElectronicoIgnoreCase(correo).isPresent()) {
            throw new ConflictoCorreoException();
        }
        Rol rolMedico = repositorioRol.findByCodigo("MEDICO")
                .orElseThrow(() -> new IllegalStateException("No está configurado el rol MEDICO."));
        Usuario usuario = repositorioUsuario.save(new Usuario(correo,
                codificadorContrasenas.encode(solicitud.contrasena()), solicitud.nombre().trim(), Set.of(rolMedico)));
        return emitirToken(usuario);
    }

    @Transactional(readOnly = true)
    public RespuestaToken iniciarSesion(SolicitudInicioSesion solicitud) {
        if (bytesContrasena(solicitud.contrasena()) > 72) {
            throw new org.springframework.security.authentication.BadCredentialsException("Credenciales inválidas.");
        }
        String correo = normalizarCorreo(solicitud.correoElectronico());
        Authentication autenticacion = administradorAutenticacion.authenticate(
                new UsernamePasswordAuthenticationToken(correo, solicitud.contrasena()));
        Usuario usuario = repositorioUsuario.findByCorreoElectronicoIgnoreCase(autenticacion.getName())
                .orElseThrow(() -> new UsernameNotFoundException("Credenciales inválidas."));
        return emitirToken(usuario);
    }

    private RespuestaToken emitirToken(Usuario usuario) {
        Instant ahora = Instant.now();
        Instant expiracion = ahora.plus(duracionToken);
        List<String> roles = usuario.getRoles().stream().map(Rol::getCodigo).sorted().toList();
        JwtClaimsSet declaraciones = JwtClaimsSet.builder()
                .issuer(emisor)
                .issuedAt(ahora)
                .expiresAt(expiracion)
                .subject(usuario.getId().toString())
                .claim("correoElectronico", usuario.getCorreoElectronico())
                .claim("nombre", usuario.getNombre())
                .claim("roles", roles)
                .build();
        String token = codificadorJwt.encode(JwtEncoderParameters.from(
                JwsHeader.with(MacAlgorithm.HS256).build(), declaraciones)).getTokenValue();
        return new RespuestaToken(token, "Bearer", duracionToken.toSeconds(),
                new RespuestaToken.UsuarioAutenticado(usuario.getId().toString(), usuario.getCorreoElectronico(),
                        usuario.getNombre(), roles));
    }

    private String normalizarCorreo(String correo) {
        return correo.trim().toLowerCase(Locale.ROOT);
    }

    private void validarLongitudContrasena(String contrasena) {
        if (bytesContrasena(contrasena) > 72) {
            throw new SolicitudInvalidaException();
        }
    }

    private int bytesContrasena(String contrasena) {
        return contrasena.getBytes(StandardCharsets.UTF_8).length;
    }
}

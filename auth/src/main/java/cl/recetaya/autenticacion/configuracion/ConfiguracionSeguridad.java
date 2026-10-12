package cl.recetaya.autenticacion.configuracion;

import cl.recetaya.autenticacion.entidad.Usuario;
import cl.recetaya.autenticacion.repositorio.RepositorioUsuario;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import com.nimbusds.jose.jwk.source.ImmutableSecret;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

@Configuration
public class ConfiguracionSeguridad {
    @Bean
    SecurityFilterChain cadenaSeguridad(HttpSecurity http) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable)
                .headers(cabeceras -> cabeceras.frameOptions(marco -> marco.sameOrigin()))
                .sessionManagement(sesion -> sesion.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(autorizacion -> autorizacion
                        .requestMatchers("/api/v1/autenticacion/registro", "/api/v1/autenticacion/iniciar-sesion").permitAll()
                        .requestMatchers("/consola-h2/**", "/actuator/health").permitAll()
                        .anyRequest().denyAll())
                .httpBasic(basico -> basico.disable())
                .formLogin(formulario -> formulario.disable());
        return http.build();
    }

    @Bean
    UserDetailsService servicioUsuarios(RepositorioUsuario repositorioUsuario) {
        return correo -> repositorioUsuario.findByCorreoElectronicoIgnoreCase(correo)
                .map(Usuario.class::cast)
                .orElseThrow(() -> new UsernameNotFoundException("Credenciales inválidas."));
    }

    @Bean
    PasswordEncoder codificadorContrasenas() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    SecretKey claveJwt(@Value("${recetaya.seguridad.jwt.secreto:}") String secreto) {
        byte[] bytes = secreto.getBytes(StandardCharsets.UTF_8);
        if (bytes.length < 32) {
            throw new IllegalStateException("RECETAYA_JWT_SECRETO debe tener al menos 32 caracteres UTF-8.");
        }
        return new SecretKeySpec(bytes, "HmacSHA256");
    }

    @Bean
    JwtEncoder codificadorJwt(SecretKey claveJwt) {
        return new NimbusJwtEncoder(new ImmutableSecret<>(claveJwt));
    }
}

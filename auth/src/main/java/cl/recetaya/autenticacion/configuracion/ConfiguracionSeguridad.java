package cl.recetaya.autenticacion.configuracion;

import cl.recetaya.autenticacion.entidad.Usuario;
import cl.recetaya.autenticacion.repositorio.RepositorioUsuario;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import com.nimbusds.jose.jwk.source.ImmutableSecret;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

@Configuration
public class ConfiguracionSeguridad {
    @Value("${recetaya.seguridad.jwt.emisor}")
    private String emisor;

    @Bean
    SecurityFilterChain cadenaSeguridad(HttpSecurity http) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable)
                .headers(cabeceras -> cabeceras.frameOptions(marco -> marco.sameOrigin()))
                .sessionManagement(sesion -> sesion.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(autorizacion -> autorizacion
                        .requestMatchers("/api/v1/autenticacion/registro", "/api/v1/autenticacion/iniciar-sesion", "/api/v1/auth/login").permitAll()
                        .requestMatchers("/api/v1/autenticacion/perfil").hasAnyRole("MEDICO", "FARMACEUTICO")
                        .requestMatchers("/consola-h2/**", "/actuator/health").permitAll()
                        .anyRequest().denyAll())
                .httpBasic(basico -> basico.disable())
                .formLogin(formulario -> formulario.disable())
                .oauth2ResourceServer(recursos -> recursos.jwt(jwt -> jwt.jwtAuthenticationConverter(convertidorJwt())));
        return http.build();
    }

    @Bean
    JwtAuthenticationConverter convertidorJwt() {
        JwtGrantedAuthoritiesConverter convertidorRoles = new JwtGrantedAuthoritiesConverter();
        convertidorRoles.setAuthoritiesClaimName("roles");
        convertidorRoles.setAuthorityPrefix("ROLE_");
        JwtAuthenticationConverter convertidor = new JwtAuthenticationConverter();
        convertidor.setJwtGrantedAuthoritiesConverter(convertidorRoles);
        return convertidor;
    }

    @Bean
    UserDetailsService servicioUsuarios(RepositorioUsuario repositorioUsuario) {
        return correo -> repositorioUsuario.findByCorreoElectronicoIgnoreCase(correo)
                .map(Usuario.class::cast)
                .orElseThrow(() -> new UsernameNotFoundException("Credenciales inválidas."));
    }

    @Bean
    AuthenticationManager administradorAutenticacion(UserDetailsService servicioUsuarios,
                                                      PasswordEncoder codificadorContrasenas) {
        DaoAuthenticationProvider proveedor = new DaoAuthenticationProvider(servicioUsuarios);
        proveedor.setPasswordEncoder(codificadorContrasenas);
        return new ProviderManager(proveedor);
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

    @Bean
    JwtDecoder decodificadorJwt(SecretKey claveJwt) {
        NimbusJwtDecoder decodificador = NimbusJwtDecoder.withSecretKey(claveJwt)
                .macAlgorithm(org.springframework.security.oauth2.jose.jws.MacAlgorithm.HS256)
                .build();
        decodificador.setJwtValidator(JwtValidators.createDefaultWithIssuer(emisor));
        return decodificador;
    }
}

package cl.recetaya.autenticacion.entidad;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Collection;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "usuario")
public class Usuario implements UserDetails {
    @Id
    private UUID id;

    @Column(name = "correo_electronico", nullable = false, unique = true, length = 254)
    private String correoElectronico;

    @Column(name = "hash_contrasena", nullable = false, length = 255)
    private String hashContrasena;

    @Column(nullable = false, length = 120)
    private String nombre;

    @Column(nullable = false)
    private boolean activo;

    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "usuario_rol",
            joinColumns = @JoinColumn(name = "usuario_id"),
            inverseJoinColumns = @JoinColumn(name = "rol_id"))
    private Set<Rol> roles;

    protected Usuario() {
    }

    public Usuario(String correoElectronico, String hashContrasena, String nombre, Set<Rol> roles) {
        this.id = UUID.randomUUID();
        this.correoElectronico = correoElectronico;
        this.hashContrasena = hashContrasena;
        this.nombre = nombre;
        this.activo = true;
        this.fechaCreacion = LocalDateTime.now(ZoneOffset.UTC);
        this.roles = roles;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return roles.stream().map(rol -> new SimpleGrantedAuthority("ROLE_" + rol.getCodigo())).toList();
    }

    @Override public String getPassword() { return hashContrasena; }
    @Override public String getUsername() { return correoElectronico; }
    @Override public boolean isEnabled() { return activo; }
    public UUID getId() { return id; }
    public String getCorreoElectronico() { return correoElectronico; }
    public String getNombre() { return nombre; }
    public Set<Rol> getRoles() { return roles; }
    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
}

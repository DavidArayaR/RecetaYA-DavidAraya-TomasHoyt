package cl.recetaya.recetas.entidad;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "paciente")
public class Paciente {
    @Id
    private UUID id;

    @Column(name = "documento", nullable = false, length = 20, unique = true)
    private String documento;

    @Column(name = "nombre", nullable = false, length = 120)
    private String nombre;

    protected Paciente() { }

    public Paciente(String documento, String nombre) {
        this.id = UUID.randomUUID();
        this.documento = documento;
        this.nombre = nombre;
    }

    public UUID getId() { return id; }
    public String getDocumento() { return documento; }
    public String getNombre() { return nombre; }
}

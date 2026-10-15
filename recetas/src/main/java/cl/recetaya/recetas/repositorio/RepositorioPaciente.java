package cl.recetaya.recetas.repositorio;

import cl.recetaya.recetas.entidad.Paciente;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RepositorioPaciente extends JpaRepository<Paciente, UUID> {
    Optional<Paciente> findByDocumento(String documento);
}

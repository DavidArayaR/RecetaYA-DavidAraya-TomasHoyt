package cl.recetaya.recetas.repositorio;

import cl.recetaya.recetas.entidad.Receta;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RepositorioReceta extends JpaRepository<Receta, UUID> {
    @EntityGraph(attributePaths = {"paciente", "detalles"})
    Optional<Receta> findByIdAndMedicoUsuarioId(UUID id, UUID medicoUsuarioId);

    @EntityGraph(attributePaths = {"paciente", "detalles"})
    List<Receta> findByMedicoUsuarioIdOrderByFechaEmisionDesc(UUID medicoUsuarioId);
}

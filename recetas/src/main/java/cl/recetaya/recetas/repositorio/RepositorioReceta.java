package cl.recetaya.recetas.repositorio;

import cl.recetaya.recetas.entidad.Receta;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RepositorioReceta extends JpaRepository<Receta, UUID> {
    @EntityGraph(attributePaths = {"paciente", "detalles"})
    @Query("select r from Receta r where r.id = :recetaId and r.medicoUsuarioId = :medicoId")
    Optional<Receta> buscarPropia(@Param("recetaId") UUID recetaId, @Param("medicoId") UUID medicoId);

    @EntityGraph(attributePaths = {"paciente", "detalles"})
    @Query("select r from Receta r where r.medicoUsuarioId = :medicoId order by r.fechaEmision desc")
    List<Receta> listarPorMedico(@Param("medicoId") UUID medicoId);
}

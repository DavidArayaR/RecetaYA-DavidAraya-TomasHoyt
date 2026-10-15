package cl.recetaya.recetas.repositorio;

import cl.recetaya.recetas.entidad.Paciente;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;

public interface RepositorioPaciente extends JpaRepository<Paciente, UUID> {
    @Query("select p from Paciente p where p.documento = :documento")
    Optional<Paciente> buscarPorDocumento(@Param("documento") String documento);
}

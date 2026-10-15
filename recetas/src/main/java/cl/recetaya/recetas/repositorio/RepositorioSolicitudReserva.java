package cl.recetaya.recetas.repositorio;

import cl.recetaya.recetas.entidad.SolicitudReserva;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RepositorioSolicitudReserva extends JpaRepository<SolicitudReserva, UUID> {
    @Query("select s from SolicitudReserva s where s.estadoPublicacion = :estado "
            + "and s.fechaProximoIntento <= :ahora order by s.fechaCreacion asc")
    List<SolicitudReserva> buscarPendientes(@Param("estado")
            cl.recetaya.recetas.entidad.EstadoPublicacionReserva estado,
            @Param("ahora") Instant ahora, Pageable pagina);

    @Query("select s from SolicitudReserva s where s.correlacionId = :correlacionId")
    Optional<SolicitudReserva> buscarPorCorrelacionId(@Param("correlacionId") UUID correlacionId);

    @Query("select s from SolicitudReserva s where s.receta.id = :recetaId")
    Optional<SolicitudReserva> buscarPorRecetaId(@Param("recetaId") UUID recetaId);
}

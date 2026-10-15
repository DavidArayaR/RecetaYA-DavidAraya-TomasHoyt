package cl.recetaya.dispensacion.repositorio;

import cl.recetaya.dispensacion.entidad.Dispensacion;
import cl.recetaya.dispensacion.entidad.EstadoDispensacion;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RepositorioDispensacion extends JpaRepository<Dispensacion, UUID> {
    Optional<Dispensacion> findByRecetaId(UUID recetaId);
    List<Dispensacion> findAllByFarmaceuticoUsuarioIdOrderByFechaDispensacionDesc(UUID farmaceuticoUsuarioId);
    Optional<Dispensacion> findByIdAndFarmaceuticoUsuarioId(UUID id, UUID farmaceuticoUsuarioId);
    List<Dispensacion> findAllByEstadoOrderByFechaDispensacionDesc(EstadoDispensacion estado);
}

package cl.recetaya.dispensacion.repositorio;

import cl.recetaya.dispensacion.entidad.EstadoSolicitudConsumo;
import cl.recetaya.dispensacion.entidad.SolicitudConsumo;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RepositorioSolicitudConsumo extends JpaRepository<SolicitudConsumo, UUID> {
    List<SolicitudConsumo> findTop50ByEstadoAndProximoIntentoLessThanEqualOrderByFechaCreacion(
            EstadoSolicitudConsumo estado, Instant ahora);
}

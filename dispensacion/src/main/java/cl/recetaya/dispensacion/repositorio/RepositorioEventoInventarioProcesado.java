package cl.recetaya.dispensacion.repositorio;

import cl.recetaya.dispensacion.entidad.EventoInventarioProcesado;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RepositorioEventoInventarioProcesado extends JpaRepository<EventoInventarioProcesado, UUID> {}

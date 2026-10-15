package cl.recetaya.recetas.repositorio;

import cl.recetaya.recetas.entidad.EventoInventarioProcesado;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RepositorioEventoInventarioProcesado extends JpaRepository<EventoInventarioProcesado, UUID> { }

# Recetaya

Repositorio académico para el backend de Recetaya, una solución basada en microservicios Java y Spring Boot.

## Estado actual

- `recetya-ya/inventario/`: scaffold inicial de Spring Boot 4.1.1 con Java 21, Maven, H2, JPA, Flyway, Web, Security y Actuator.
- Los servicios de autenticación/usuarios, recetas y dispensación todavía no están incluidos como proyectos en este repositorio.
- El scaffold de Inventario es una base generada; todavía falta implementar entidades, endpoints REST, reglas de negocio, configuración persistente de H2 y pruebas funcionales.

## Requisitos

- JDK 21
- Maven Wrapper incluido en el servicio

## Ejecutar el scaffold de Inventario

En PowerShell:

```powershell
cd recetya-ya/inventario
.\mvnw.cmd spring-boot:run
```

La configuración actual solo nombra la aplicación; H2 está en las dependencias, pero falta configurar la URL local en modo archivo y las migraciones Flyway antes de usar persistencia de forma reproducible.

## Ramas

- `main`: base estable del repositorio.
- `develop`: integración del trabajo en curso.
- `feature/<tema>`: cambios acotados por funcionalidad, por ejemplo `feature/inventario-service`.

Crear ramas feature desde `develop` y fusionarlas primero a `develop`. Llevar a `main` los cambios revisados para una entrega.

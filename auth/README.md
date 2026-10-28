# Auth — Receta Ya

Microservicio de autenticación de Receta Ya. Registra médicos, inicia sesiones, emite JWT y permite consultar el perfil autenticado. Escucha solo en `127.0.0.1:8081` para la demostración local.

## Relación con la evaluación

| Criterio | Evidencia en este servicio |
|---|---|
| IE1 e IE2 | `ControladorAutenticacion`, `ServicioAutenticacion`, repositorios y entidades separados por responsabilidad. |
| IE3 | Peticiones de registro, inicio de sesión y perfil; mostrar respuestas correctas y errores en Postman. |
| IE4 e IE6 | H2, Flyway, JPA y relación `Usuario`–`Rol` mediante `usuario_rol`. |
| IE7 e IE8 | `pom.xml`, Maven Wrapper, pruebas y JAR ejecutable. |
| IE9 | Ramas `main`, `develop` y `feature/auth-service` en el repositorio actual. |
| IE10 | Clonación, configuración y ejecución reproducibles con los pasos siguientes. |

Auth no ofrece un CRUD completo de usuarios: su API implementa registro, inicio de sesión y lectura del perfil. El CRUD de la demostración se realiza en Inventario.

## Requisitos y obtención del código

- JDK 21, Git y acceso a Maven Central para la primera descarga de dependencias.
- Puerto `8081` libre. H2 está incluido; no requiere un servidor de base de datos aparte.
- En Windows, PowerShell. En macOS y Linux, Bash. No hace falta instalar Maven: el módulo incluye Maven Wrapper.

Mientras los cuatro servicios permanezcan en el repositorio actual, clonar y entrar en `auth`:

### Windows

```powershell
git clone https://github.com/DavidArayaR/RecetaYA-DavidAraya-TomasHoyt.git receta-ya
Set-Location .\receta-ya
git switch develop
Set-Location .\auth
java -version
```

### macOS y Linux

```bash
git clone https://github.com/DavidArayaR/RecetaYA-DavidAraya-TomasHoyt.git receta-ya
cd receta-ya
git switch develop
cd auth
java -version
```

Si Auth se publica después en un repositorio independiente, clonar su URL y ejecutar los comandos siguientes desde la raíz de ese nuevo repositorio; no habrá que entrar en `auth/`.

## Compilar, probar y empaquetar

Ejecutar desde la carpeta que contiene este README y `pom.xml`. Detener un JAR de Auth que esté corriendo antes de usar `clean`, especialmente en Windows.

### Windows

```powershell
.\mvnw.cmd clean verify
Get-Item .\target\auth-0.0.1-SNAPSHOT.jar
```

### macOS y Linux

```bash
bash ./mvnw clean verify
ls -lh target/auth-0.0.1-SNAPSHOT.jar
```

`verify` compila, ejecuta las pruebas y empaqueta. También puede usarse `clean package` para mostrar esa fase de Maven en el video. Comprobar `BUILD SUCCESS` y que el JAR existe antes de iniciarlo. El `pom.xml` declara Spring Boot 4.1.1, Java 21, Web MVC, Security, servidor de recursos OAuth2, JPA, Validation, Flyway, H2, Actuator y dependencias de prueba.

## Configurar JWT y el usuario local

Auth necesita `RECETAYA_JWT_SECRETO` de al menos 32 caracteres UTF-8. Generar un secreto aleatorio en una terminal privada y usar **el mismo valor** en Auth e Inventario. No guardarlo en Git ni mostrarlo durante el video. `RECETAYA_JWT_EMISOR` debe coincidir en ambos servicios.

Para generar un valor local de 48 bytes codificado en Base64, usar uno de estos comandos **fuera de la grabación** y copiar el resultado a las dos terminales:

```powershell
# Windows
$bytesJwt = New-Object byte[] 48
[Security.Cryptography.RandomNumberGenerator]::Create().GetBytes($bytesJwt)
[Convert]::ToBase64String($bytesJwt)
```

```bash
# macOS y Linux
head -c 48 /dev/urandom | base64 | tr -d '\n'; printf '\n'
```

Para crear un usuario local con rol `FARMACEUTICO` al arrancar Auth, definir correo, nombre y una contraseña nueva de al menos 8 caracteres. La cuenta solo se crea si ese correo todavía no existe en la H2 de Auth.

### Windows: en la terminal de Auth

```powershell
$env:RECETAYA_JWT_EMISOR = 'http://localhost:8081'
$env:RECETAYA_JWT_SECRETO = Read-Host 'Secreto JWT compartido (mínimo 32 caracteres)'
$env:RECETAYA_AUTH_USUARIO_INICIAL_CORREO = 'farmaceutico@local.test'
$env:RECETAYA_AUTH_USUARIO_INICIAL_NOMBRE = 'Farmacéutico local'
$env:RECETAYA_AUTH_USUARIO_INICIAL_CONTRASENA = Read-Host 'Contraseña local nueva (mínimo 8 caracteres)'
java -jar .\target\auth-0.0.1-SNAPSHOT.jar
```

### macOS y Linux: en la terminal de Auth

```bash
export RECETAYA_JWT_EMISOR='http://localhost:8081'
read -r -s -p 'Secreto JWT compartido (mínimo 32 caracteres): ' RECETAYA_JWT_SECRETO; printf '\n'
export RECETAYA_JWT_SECRETO
export RECETAYA_AUTH_USUARIO_INICIAL_CORREO='farmaceutico@local.test'
export RECETAYA_AUTH_USUARIO_INICIAL_NOMBRE='Farmacéutico local'
read -r -s -p 'Contraseña local nueva (mínimo 8 caracteres): ' RECETAYA_AUTH_USUARIO_INICIAL_CONTRASENA; printf '\n'
export RECETAYA_AUTH_USUARIO_INICIAL_CONTRASENA
java -jar target/auth-0.0.1-SNAPSHOT.jar
```

Ejecutar desde esta carpeta: la URL `jdbc:h2:file:./data/auth-recetaya` es relativa al directorio de trabajo. Si el usuario inicial ya existe, la provisión no cambia su contraseña; usar la contraseña anterior o elegir otro correo ficticio.

## Verificar la API con Postman

Primero comprobar `GET http://localhost:8081/actuator/health`: debe responder `200` y `status: UP`.

| Método | Ruta | Uso y respuesta esperada |
|---|---|---|
| `POST` | `/api/v1/autenticacion/registro` | Crea un médico ficticio; `201` con `tokenAcceso` y datos del usuario. |
| `POST` | `/api/v1/autenticacion/iniciar-sesion` | Inicia sesión; `200` con `tokenAcceso`. |
| `GET` | `/api/v1/autenticacion/perfil` | Requiere `Authorization: Bearer <token>`; `200` con id, correo, nombre y roles. |

Ejemplo de registro con datos ficticios y una contraseña local elegida por el evaluador:

```json
{
  "correoElectronico": "medico.demo@local.test",
  "contrasena": "<contraseña local de al menos 8 caracteres>",
  "nombre": "Médico de demostración"
}
```

Para iniciar sesión como farmacéutico, usar el correo y la contraseña definidos en las variables del usuario inicial. Mostrar también una solicitud con datos inválidos (`400`), un correo ya registrado (`409`) o credenciales incorrectas (`401`). Los errores de Auth usan el formato `ProblemDetail`. No mostrar ni exportar contraseñas, tokens o encabezados `Authorization` en las evidencias.

## Base de datos y estructura

- `src/main/resources/application.yaml`: URL de H2, Flyway, validación JPA y puerto.
- `src/main/resources/bd/migracion/V1__crear_usuarios_y_roles.sql`: tablas `usuario`, `rol` y `usuario_rol` y roles iniciales.
- `entidad/Usuario.java` y `entidad/Rol.java`: mapeo JPA; `Usuario` tiene relación `@ManyToMany` con `Rol`.
- `repositorio/`, `servicio/`, `controlador/` y `transferencia/`: acceso a datos, reglas, rutas REST y cuerpos JSON.

La consola H2 local está en `http://localhost:8081/consola-h2`. Consultar URL, usuario y contraseña en la configuración local sin mostrarlos en la grabación. Tras crear un médico, verificar la persistencia con:

```sql
SELECT correo_electronico, nombre FROM usuario;
SELECT codigo, nombre FROM rol;
```

## Ramas y entrega

En el repositorio actual, `main` representa la versión estable, `develop` integra los servicios y `feature/auth-service` identifica el trabajo de Auth. Para IE9, mostrar las ramas y los commits descriptivos con `git branch -a` y `git log --oneline`; publicar únicamente la versión revisada en el repositorio que se entregue al docente.

La evaluación solicita enlace(s) de GitHub y un video de **3 a 8 minutos**. En el video deben escucharse las voces de ambos estudiantes con audio claro; mostrar comandos y resultados ejecutados, las respuestas de Postman y la interacción real con H2. Entregar los enlaces en AVA y enviar copia al correo del docente según la pauta.

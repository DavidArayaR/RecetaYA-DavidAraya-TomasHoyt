# Receta Ya — backend

Backend académico de cuatro microservicios con Java 21, Spring Boot 4.1.1, API REST, Spring Data JPA, H2 y Flyway.

| Servicio | Carpeta | Puerto | Función |
|---|---|---:|---|
| Auth | `auth/` | 8081 | Usuarios, roles y JWT. |
| Inventario | `inventario/` | 8080 | Medicamentos, sucursales y existencias. |
| Recetas | `recetas/` | 8082 | Registro y consulta de recetas. |
| Dispensación | `dispensacion/` | 8083 | Consulta y solicitud de dispensaciones. |

## Requisitos

- JDK 21 (comprobar con `java -version`).
- Windows: PowerShell y `mvnw.cmd`. macOS y Linux: Bash y `mvnw`. No se necesita instalar Maven por separado.
- Git para clonar el repositorio. En macOS y Linux, los scripts `mvnw` están versionados sin permiso ejecutable: se invocan con `bash ./mvnw`.
- Acceso a los repositorios de Maven para descargar dependencias en la primera construcción.
- Puertos 8080–8083 libres. H2 está incluido; no hay que instalar un servidor de base de datos.

Las instrucciones para macOS y Linux comparten bloques Bash porque los comandos son iguales en ambos sistemas.

## Clonar el repositorio

Desde la carpeta donde se quiere crear `receta-ya`:

### Windows (PowerShell)

```powershell
git clone https://github.com/DavidArayaR/RecetaYA-DavidAraya-TomasHoyt.git receta-ya
Set-Location .\receta-ya
git switch develop
java -version
```

### macOS y Linux (Bash)

```bash
git clone https://github.com/DavidArayaR/RecetaYA-DavidAraya-TomasHoyt.git receta-ya
cd receta-ya
git switch develop
java -version
```

`develop` integra los cuatro servicios. Las ramas `feature/auth-service`, `feature/inventario-service`, `feature/recetas-service` y `feature/dispensacion-service` corresponden al desarrollo de cada módulo.

## Compilar y empaquetar

Ejecutar secuencialmente desde la raíz `receta-ya`. Si los servicios ya están iniciados, detenerlos antes de usar `clean`: en Windows los procesos Java pueden bloquear sus JAR.

### Windows (PowerShell)

```powershell
Set-Location .\auth
.\mvnw.cmd clean package
if ($LASTEXITCODE -ne 0) { throw 'Falló la construcción de Auth' }
Set-Location ..\inventario
.\mvnw.cmd clean package
if ($LASTEXITCODE -ne 0) { throw 'Falló la construcción de Inventario' }
Set-Location ..\recetas
.\mvnw.cmd clean package
if ($LASTEXITCODE -ne 0) { throw 'Falló la construcción de Recetas' }
Set-Location ..\dispensacion
.\mvnw.cmd clean package
if ($LASTEXITCODE -ne 0) { throw 'Falló la construcción de Dispensación' }
Set-Location ..
```

### macOS y Linux (Bash)

```bash
set -e
cd auth && bash ./mvnw clean package && cd ..
cd inventario && bash ./mvnw clean package && cd ..
cd recetas && bash ./mvnw clean package && cd ..
cd dispensacion && bash ./mvnw clean package && cd ..
```

Cada comando debe terminar en `BUILD SUCCESS`; si uno falla, corregirlo antes de pasar al siguiente módulo. `package` compila, ejecuta las pruebas y genera un archivo `<servicio>/target/<servicio>-0.0.1-SNAPSHOT.jar`. Para verificar la fase adicional del ciclo Maven, ejecutar `.\mvnw.cmd clean verify` en Windows o `bash ./mvnw clean verify` en macOS/Linux dentro de cada módulo.

Desde la raíz del repositorio, comprobar los archivos generados:

```powershell
# Windows
Get-ChildItem .\auth\target\*.jar
Get-ChildItem .\inventario\target\*.jar
Get-ChildItem .\recetas\target\*.jar
Get-ChildItem .\dispensacion\target\*.jar
```

```bash
# macOS y Linux
ls -lh auth/target/*.jar inventario/target/*.jar recetas/target/*.jar dispensacion/target/*.jar
```

## JWT y datos locales

Los cuatro servicios deben recibir el **mismo** emisor y secreto JWT. Generar el secreto **una sola vez** (48 bytes aleatorios) en una terminal privada:

### Windows (PowerShell)

```powershell
$bytesJwt = New-Object byte[] 48
[Security.Cryptography.RandomNumberGenerator]::Create().GetBytes($bytesJwt)
[Convert]::ToBase64String($bytesJwt)
```

### macOS y Linux (Bash)

```bash
head -c 48 /dev/urandom | base64 | tr -d '\n'; printf '\n'
```

Copiar el resultado sin publicarlo ni guardarlo en archivos versionados. En **cada** una de las cuatro terminales, definir antes del arranque:

#### Windows (PowerShell)

```powershell
$env:RECETAYA_JWT_EMISOR = 'http://localhost:8081'
$env:RECETAYA_JWT_SECRETO = Read-Host 'Pega el mismo secreto JWT local'
$env:RECETAYA_SQS_HABILITADA = 'false'
```

#### macOS y Linux (Bash)

```bash
export RECETAYA_JWT_EMISOR='http://localhost:8081'
read -r -s -p 'Pega el mismo secreto JWT local: ' RECETAYA_JWT_SECRETO; printf '\n'
export RECETAYA_JWT_SECRETO
export RECETAYA_SQS_HABILITADA=false
```

Si se reinician los cuatro procesos con otro secreto, iniciar sesión otra vez para obtener un JWT nuevo.

### Primer usuario farmacéutico

En la terminal de **Auth**, si se necesita crear una cuenta local `FARMACEUTICO`, definir también:

#### Windows (PowerShell)

```powershell
$env:RECETAYA_AUTH_USUARIO_INICIAL_CORREO = 'farmaceutico@local.test'
$env:RECETAYA_AUTH_USUARIO_INICIAL_NOMBRE = 'Farmacéutico local'
$env:RECETAYA_AUTH_USUARIO_INICIAL_CONTRASENA = Read-Host 'Contraseña local nueva (mínimo 8 caracteres)'
```

#### macOS y Linux (Bash)

```bash
export RECETAYA_AUTH_USUARIO_INICIAL_CORREO='farmaceutico@local.test'
export RECETAYA_AUTH_USUARIO_INICIAL_NOMBRE='Farmacéutico local'
read -r -s -p 'Contraseña local nueva (mínimo 8 caracteres): ' RECETAYA_AUTH_USUARIO_INICIAL_CONTRASENA; printf '\n'
export RECETAYA_AUTH_USUARIO_INICIAL_CONTRASENA
```

Auth solo crea esa cuenta si el correo aún no existe en su H2. Si ya existe, conserva la contraseña anterior; usarla o elegir otro correo ficticio. No usar credenciales reales.

## Iniciar los cuatro servicios

Abrir **cuatro terminales**, una por servicio. En cada una, definir primero las variables compartidas anteriores y partir de la raíz `receta-ya`. En Auth, añadir las variables del usuario inicial cuando corresponda. Cada bloque se ejecuta solo en su propia terminal.

### Windows (PowerShell)

```powershell
# Ventana Auth
Set-Location .\auth
java -jar .\target\auth-0.0.1-SNAPSHOT.jar

# Ventana Inventario
Set-Location .\inventario
java -jar .\target\inventario-0.0.1-SNAPSHOT.jar --spring.profiles.active=desarrollo

# Ventana Recetas
Set-Location .\recetas
java -jar .\target\recetas-0.0.1-SNAPSHOT.jar

# Ventana Dispensación
Set-Location .\dispensacion
java -jar .\target\dispensacion-0.0.1-SNAPSHOT.jar
```

### macOS y Linux (Bash)

```bash
# Terminal Auth
cd auth
java -jar target/auth-0.0.1-SNAPSHOT.jar

# Terminal Inventario
cd inventario
java -jar target/inventario-0.0.1-SNAPSHOT.jar --spring.profiles.active=desarrollo

# Terminal Recetas
cd recetas
java -jar target/recetas-0.0.1-SNAPSHOT.jar

# Terminal Dispensación
cd dispensacion
java -jar target/dispensacion-0.0.1-SNAPSHOT.jar
```

El directorio de trabajo importa en los tres sistemas: las URL H2 `jdbc:h2:file:./data/...` son relativas a cada módulo, cuyos archivos `data/` están ignorados por Git.

## Configuración de la base relacional

- Los `pom.xml` incluyen Spring Data JPA, Flyway y H2. Cada servicio tiene migraciones en `src/main/resources/bd/migracion/`.
- Las URL H2, opciones JPA y Flyway están en `src/main/resources/application.yaml`. Inventario usa además `application-desarrollo.yaml`, con `jdbc:h2:file:./data/inventario-recetaya`.
- Auth usa `jdbc:h2:file:./data/auth-recetaya`, Recetas usa `jdbc:h2:file:./data/recetas-recetaya` y Dispensación usa la URL de archivo que puede cambiarse con `RECETAYA_DISPENSACION_BD`.
- La consola H2 de Inventario en desarrollo está en `http://localhost:8080/consola-h2`. Tomar usuario y contraseña de la configuración local sin incluirlos en capturas.

Para comprobar una inserción desde la consola H2 de Inventario, consultar `SELECT codigo, nombre FROM medicamento;` después de crear un medicamento por la API.

## Comprobar las API

Con los cuatro procesos activos:

### Windows (PowerShell)

```powershell
Invoke-RestMethod http://localhost:8081/actuator/health
Invoke-RestMethod http://localhost:8080/actuator/health
Invoke-RestMethod http://localhost:8082/actuator/health
Invoke-RestMethod http://localhost:8083/actuator/health
```

### macOS y Linux (Bash)

```bash
curl -fsS http://localhost:8081/actuator/health
curl -fsS http://localhost:8080/actuator/health
curl -fsS http://localhost:8082/actuator/health
curl -fsS http://localhost:8083/actuator/health
```

Cada respuesta debe indicar `status: UP`. Para escribir en Inventario, iniciar sesión en Postman con `POST http://localhost:8081/api/v1/autenticacion/iniciar-sesion` y el correo y contraseña locales del farmacéutico:

```json
{"correoElectronico":"farmaceutico@local.test","contrasena":"<contraseña local>"}
```

La respuesta contiene `tokenAcceso`. Utilizarlo como `Bearer` para `POST http://localhost:8080/api/v1/inventario/medicamentos`:

```json
{"codigo":"DEMO-001","nombre":"Medicamento de prueba","presentacion":"Caja"}
```

Se espera `201` con un `id`. La lectura `GET http://localhost:8080/api/v1/inventario/medicamentos` debe incluirlo. Para un CRUD completo, usar el `id` en `GET`, `PUT` y `DELETE` de `/api/v1/inventario/medicamentos/{id}`. Elegir un código nuevo al repetir la demostración y no asociar existencias al medicamento que se eliminará.

## Límites de la integración local

Con `RECETAYA_SQS_HABILITADA=false`, una receta creada queda `ACEPTADA_PENDIENTE_RESERVA`; no implica que se haya reservado stock. Dispensación aún no puede validar una receta reservada desde Recetas y el intento de entrega puede responder `503`. No presentar ese flujo como una entrega terminada.

## Ramas

- `main`: versión estable.
- `develop`: integración y demostración de los cuatro microservicios.
- `feature/<servicio>`: desarrollo de cada módulo antes de integrarlo.

# tls-ssl · API de autenticación sobre HTTPS

API REST de autenticación construida con **Spring Boot 4** que opera exclusivamente sobre **TLS/SSL**. Permite registrar usuarios y autenticarlos con **HTTP Basic Auth**, guardando las contraseñas con **BCrypt** en **PostgreSQL**.

Proyecto del repositorio `backend_authentication`.

## Características

- HTTPS en el puerto `8443` con keystore PKCS12 (TLS 1.2 y 1.3).
- Redirección automática de HTTP (`8080`) a HTTPS (`8443`).
- Registro de usuarios con validación de datos (Bean Validation).
- Autenticación Basic Auth contra la base de datos.
- Contraseñas cifradas con BCrypt.
- API stateless: sin sesiones ni cookies.
- Scripts para generar el certificado de desarrollo (Windows y Linux/macOS).

## Tecnologías

| Componente | Detalle |
|---|---|
| Lenguaje | Java 17+ |
| Framework | Spring Boot 4.1.1 |
| Módulos | Spring Web, Spring Security 7, Spring Data JPA, Validation |
| Base de datos | PostgreSQL |
| Build | Maven (`mvnw`) |
| Pruebas manuales | Postman / curl |

## Estructura del proyecto

```
tls-ssl/
├── certs/                      # Keystore local (ignorado por Git)
│   └── .gitignore
├── docs/                       # Capturas de las pruebas en Postman
├── scripts/
│   ├── generate-keystore.ps1   # Genera el certificado (Windows)
│   └── generate-keystore.sh    # Genera el certificado (Linux/macOS)
├── src/main/java/bk_auth/tls_ssl/
│   ├── config/
│   │   ├── SecurityConfig.java       # Reglas de seguridad y BCrypt
│   │   └── HttpRedirectConfig.java   # Conector HTTP + redirección a HTTPS
│   ├── controller/AuthController.java
│   ├── dto/                          # RegisterRequest, UserResponse
│   ├── entity/AppUser.java
│   ├── exception/GlobalExceptionHandler.java
│   ├── repository/UserRepository.java
│   └── service/                      # UserService, CustomUserDetailsService
├── src/main/resources/application.properties
└── pom.xml
```

## Requisitos previos

- JDK 17 o superior (incluye `keytool`).
- PostgreSQL en ejecución.
- Postman o curl para las pruebas.

## Configuración

### 1. Base de datos

```sql
CREATE DATABASE auth_db;
```

La tabla `users` la crea Hibernate automáticamente al arrancar.

### 2. Certificado de desarrollo

Desde la raíz del proyecto.

Windows (PowerShell):

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\generate-keystore.ps1
```

Linux/macOS:

```bash
./scripts/generate-keystore.sh
```

Esto crea `certs/keystore.p12`, un certificado **autofirmado válido solo para desarrollo**.

### 3. Variables de entorno (opcionales)

| Variable | Valor por defecto | Descripción |
|---|---|---|
| `DB_URL` | `jdbc:postgresql://localhost:5432/auth_db` | URL de conexión |
| `DB_USER` | `postgres` | Usuario de la base |
| `DB_PASSWORD` | `postgres` | Contraseña de la base |
| `SSL_KEYSTORE_PASSWORD` | `changeit` | Contraseña del keystore |

En PowerShell: `$env:DB_PASSWORD = "tu_password"`.

## Ejecución

```bash
./mvnw spring-boot:run
```

En Windows: `.\mvnw.cmd spring-boot:run`.

Al arrancar debe verse `Tomcat started on port 8443 (https), 8080 (http)`.

## Endpoints

| Método | Ruta | Acceso | Descripción |
|---|---|---|---|
| `POST` | `/api/auth/register` | Público | Registra un usuario nuevo |
| `GET` | `/api/auth/me` | Basic Auth | Devuelve el usuario autenticado |

### Registro

```bash
curl -k -X POST https://localhost:8443/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username":"ana","email":"ana@mail.com","password":"Secreto123"}'
```

Respuesta `201 Created`:

```json
{ "id": 1, "username": "ana", "email": "ana@mail.com", "role": "USER" }
```

Reglas de validación: `username` de 3 a 50 caracteres, `email` con formato válido, `password` de 8 a 100 caracteres.

### Login (usuario autenticado)

No existe un endpoint de login separado. La autenticación es Basic Auth, así que las credenciales se envían en cada petición protegida.

```bash
curl -k -u ana:Secreto123 https://localhost:8443/api/auth/me
```

> En PowerShell usa `curl.exe` en lugar de `curl`. El parámetro `-k` acepta el certificado autofirmado.

### Códigos de respuesta

| Código | Situación |
|---|---|
| `200` | Credenciales válidas en `/me` |
| `201` | Usuario registrado |
| `302` | Petición HTTP redirigida a HTTPS |
| `400` | Validación fallida (errores por campo en el cuerpo) |
| `401` | Credenciales ausentes o incorrectas |
| `409` | Usuario o correo ya registrado |

## Pruebas con Postman

1. Desactiva **Settings → General → SSL certificate verification** por el certificado autofirmado.
2. Registro: `POST https://localhost:8443/api/auth/register` con body **raw → JSON**.
3. Login: `GET https://localhost:8443/api/auth/me` con **Authorization → Basic Auth**.
4. Redirección: `GET http://localhost:8080/api/auth/me` con **Automatically follow redirects** desactivado. Debe responder `302` con `Location: https://localhost:8443/api/auth/me`.

Usa siempre la URL `https://` para enviar credenciales. Las capturas de las pruebas están en la carpeta [`docs/`](docs/).

## Seguridad

- **TLS obligatorio:** Tomcat redirige todo el tráfico HTTP al puerto HTTPS mediante una restricción de transporte `CONFIDENTIAL`.
- **Basic Auth solo codifica en base64:** no cifra las credenciales, por eso depende de TLS.
- **Sin CSRF ni sesiones:** al ser una API stateless sin cookies, CSRF está deshabilitado.
- **Contraseñas:** se almacenan como hash BCrypt, nunca en texto plano.
- **Keystore fuera de Git:** `certs/.gitignore` excluye `*.p12`, `*.jks`, `*.pem` y `*.key`.
- **Producción:** usa un certificado emitido por una CA y guarda las contraseñas en variables de entorno o un gestor de secretos. Sustituye `ddl-auto=update` por migraciones (Flyway o Liquibase).

## Problemas frecuentes

| Síntoma | Causa y solución |
|---|---|
| `database "auth_db" does not exist` | Crea la base de datos con `CREATE DATABASE auth_db;` |
| `Could not load store from './certs/keystore.p12'` | Ejecuta el script del certificado desde la raíz del proyecto |
| `password authentication failed` | Define `DB_PASSWORD` con tu contraseña de PostgreSQL |
| `Port 8443 was already in use` | Cierra la otra instancia de la aplicación |
| `401` al probar por `http://localhost:8080` | El redireccionamiento descarta el header `Authorization`; usa `https://localhost:8443` |
| `SSL Error: Self-signed certificate` en Postman | Desactiva la verificación del certificado SSL |

## Flujo de trabajo con Git

| Rama | Propósito |
|---|---|
| `master` | Código estable |
| `develop` | Integración |
| `feat/...` | Desarrollo de cada funcionalidad, integrada a `develop` mediante Pull Request |

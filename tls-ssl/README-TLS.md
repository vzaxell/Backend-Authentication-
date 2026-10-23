# tls-ssl – API de autenticación sobre HTTPS

## Requisitos
- Java 17+ (incluye `keytool`)
- PostgreSQL con una base de datos `auth_db`

## 1. Base de datos
```sql
CREATE DATABASE auth_db;
```
Variables opcionales: `DB_URL`, `DB_USER`, `DB_PASSWORD`.

## 2. Generar el certificado (desarrollo)
Windows (PowerShell):
```powershell
.\scripts\generate-keystore.ps1
```
Linux/macOS:
```bash
./scripts/generate-keystore.sh
```
Variable opcional: `SSL_KEYSTORE_PASSWORD` (por defecto `changeit`).

## 3. Ejecutar
```bash
./mvnw spring-boot:run
```
- HTTPS: https://localhost:8443
- HTTP (8080) redirige automáticamente a HTTPS.

## 4. Probar
Registro (`-k` porque el certificado es autofirmado):
```bash
curl -k -X POST https://localhost:8443/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username":"ana","email":"ana@mail.com","password":"Secreto123"}'
```
Usuario autenticado:
```bash
curl -k -u ana:Secreto123 https://localhost:8443/api/auth/me
```
Redirección:
```bash
curl -kIL http://localhost:8080/api/auth/me
```

## Notas
- Contraseñas guardadas con BCrypt.
- TLS 1.2 y 1.3 habilitados; Spring Security añade HSTS sobre HTTPS.
- En producción usa un certificado de una CA y guarda la contraseña del keystore en variables de entorno o un gestor de secretos.

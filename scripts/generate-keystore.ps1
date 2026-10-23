# Genera un certificado autofirmado SOLO para desarrollo (Windows / PowerShell).
$pass = if ($env:SSL_KEYSTORE_PASSWORD) { $env:SSL_KEYSTORE_PASSWORD } else { "changeit" }
New-Item -ItemType Directory -Force -Path certs | Out-Null
keytool -genkeypair -alias tls-ssl -keyalg RSA -keysize 2048 `
  -storetype PKCS12 -keystore certs/keystore.p12 `
  -storepass $pass -validity 365 `
  -dname "CN=localhost, OU=Dev, O=Dev, C=MX" `
  -ext "SAN=dns:localhost,ip:127.0.0.1"
Write-Host "Keystore creado en certs/keystore.p12"

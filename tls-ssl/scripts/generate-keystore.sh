#!/usr/bin/env bash
# Genera un certificado autofirmado SOLO para desarrollo.
set -e
mkdir -p certs
keytool -genkeypair -alias tls-ssl -keyalg RSA -keysize 2048 \
  -storetype PKCS12 -keystore certs/keystore.p12 \
  -storepass "${SSL_KEYSTORE_PASSWORD:-changeit}" -validity 365 \
  -dname "CN=localhost, OU=Dev, O=Dev, C=MX" \
  -ext "SAN=dns:localhost,ip:127.0.0.1"
echo "Keystore creado en certs/keystore.p12"

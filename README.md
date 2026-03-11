# 🚀 File Flow

API em **Spring Boot** para gerenciamento de **jobs** e **documentos**, com geração de URL pré-assinada para upload no **Amazon S3**. 📄☁️

## 🧰 Stack

- ☕ Java 21
- 🌱 Spring Boot (Web, JPA, Security, Actuator)
- 🗄️ H2 (dev) e PostgreSQL (prod)
- 📘 OpenAPI/Swagger UI
- ☁️ AWS SDK S3
- 🧪 JUnit + Spring Test

## 📁 Estrutura funcional

- 🧩 `Job`: representa um processo com múltiplos documentos.
- 📄 `JobDocument`: representa cada documento de um job.
- 🔗 Geração de URL pré-assinada para upload de arquivo.
- 📊 Consulta de status de job e documentos.

## ▶️ Como rodar localmente

```bash
./mvnw spring-boot:run
```

No Windows (PowerShell):

```powershell
.\mvnw.cmd spring-boot:run
```

## ⚙️ Perfis

- 🧪 `dev` (default): usa H2 em memória.
- 🏭 `prod`: usa PostgreSQL.

## ☁️ Como instanciar uma EC2 com o projeto

Use o script abaixo como `User Data` ao criar uma instância EC2 Amazon Linux. Ele instala o Java 21, baixa o `.jar` publicado no GitHub Releases, define as variáveis de ambiente do profile `prod` e inicia a aplicação na porta `8080`.

```bash
#!/bin/bash
set -euxo pipefail

APP_DIR=/opt/app
PORT=8080

# =========================
# VARIAVEIS DE AMBIENTE
# =========================
export SPRING_PROFILES_ACTIVE=prod
export DB_URL=jdbc:postgresql://fileflow.cvwku8gcym4z.us-west-2.rds.amazonaws.com:5432/fileflow
export DB_USER=postgres
export DB_PASSWORD=postgres

# =========================
# INSTALA DEPENDENCIAS
# =========================
dnf -y update
dnf -y install java-21-amazon-corretto-headless wget

mkdir -p "$APP_DIR"

# =========================
# BAIXA A APLICAÇÃO
# =========================
wget -O "$APP_DIR/app.jar" "https://github.com/erickluz/file-flow/releases/download/0.0.2/file-flow-0.0.2-SNAPSHOT.jar"

ls -lh "$APP_DIR/app.jar"
file "$APP_DIR/app.jar" || true
java -version

# =========================
# INICIA A APLICAÇÃO
# =========================
nohup /usr/bin/java \
  -jar "$APP_DIR/app.jar" \
  --server.port=$PORT \
  --spring.profiles.active=$SPRING_PROFILES_ACTIVE \
  > /var/log/app.log 2>&1 &

echo "STARTED: $!" >> /var/log/app.log
```

Após a instância subir, a aplicação ficará disponível em `http://<ec2-public-ip>:8080`. Para validar, use:

```bash
curl http://<ec2-public-ip>:8080/actuator/health
```

## 🔐 Segurança

- 🔒 Autenticação HTTP Basic para os endpoints da API.
- ✅ Healthcheck liberado: `/actuator/health`
- ✅ H2 Console liberado: `/h2-console`
- 👤 Usuário/senha padrão:
  - `APP_BASIC_USER=admin`
  - `APP_BASIC_PASS=admin`

## 📚 Documentação da API

- 🧾 Swagger UI: `http://localhost:8080/swagger-ui/index.html`
- 📄 OpenAPI JSON: `api-docs.json` (arquivo no projeto)

## 🧪 Banco H2 (ambiente dev)

- 🌐 Console: `http://localhost:8080/h2-console`
- 🔌 JDBC URL: `jdbc:h2:mem:fileflow`
- 👤 User: `sa`
- 🔑 Password: *(vazio)*

## 🛣️ Endpoints principais

- `POST /jobs/create` ➜ cria job
- `POST /jobs/{jobId}/documents/create` ➜ cria documento no job
- `POST /jobs/{jobId}/documents/{documentId}/upload-url` ➜ gera URL de upload
- `GET /jobs/{jobId}` ➜ consulta job
- `GET /jobs/{jobId}/documents` ➜ lista documentos do job
- `GET /jobs/{jobId}/documents/{documentId}` ➜ consulta documento específico

## ☁️ Upload de arquivo no S3 com URL pré-assinada

Exemplo de upload usando `curl`:

```bash
curl -X PUT "https://erick-luz-files-flow.s3.us-west-2.amazonaws.com/raw/1/4485849069348867683/file2?X-Amz-Algorithm=AWS4-HMAC-SHA256&X-Amz-Date=20260302T193900Z&X-Amz-SignedHeaders=content-type%3Bhost&X-Amz-Credential=AKIAYFDWETBEVGYQVSHS%2F20260302%2Fus-west-2%2Fs3%2Faws4_request&X-Amz-Expires=600&X-Amz-Signature=0e2dfe4dd8aefebaf1046ec0e5a3de48dcfb5d8b1aba67f51a450f8c73430835" -H "Content-Type: text/plain" --data-binary "Arquivo 6 enviado via presigned URL"
```

## ✅ Healthcheck

```bash
curl http://localhost:8080/actuator/health
```

---

Feito com Java + Spring + S3 para fluxo de arquivos. 💡📦

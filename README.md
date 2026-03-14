# File Flow

API em Spring Boot para gerenciamento de jobs e documentos, com geracao de URL pre-assinada para upload no Amazon S3.

## Stack

- Java 21
- Spring Boot Web, JPA, Security e Actuator
- Springdoc OpenAPI / Swagger UI
- H2 no ambiente `dev`
- PostgreSQL no ambiente `prod`
- AWS SDK S3
- JUnit e Spring Test

## Versao atual

- Aplicacao: `0.0.3-SNAPSHOT`

## Principais funcionalidades

- Criacao e consulta de jobs
- Criacao e consulta de documentos vinculados a um job
- Geracao de URL pre-assinada para upload no S3
- Consulta de status de job e documento
- Health check via Actuator
- Autenticacao HTTP Basic nos endpoints protegidos

## Estrutura funcional

- `Job`: representa um processo com multiplos documentos
- `JobDocument`: representa cada documento de um job
- `JobStatus`: controla o estado do job
- `DocumentStatus`: controla o estado do documento

## Como rodar localmente

Linux/macOS:

```bash
./mvnw spring-boot:run
```

Windows PowerShell:

```powershell
.\mvnw.cmd spring-boot:run
```

## Gerando o jar

```bash
./mvnw clean package
```

No Windows PowerShell:

```powershell
.\mvnw.cmd clean package
```

## Perfis

- `dev` (default): usa H2 em memoria
- `prod`: usa PostgreSQL

### Configuracao `dev`

Arquivo: `src/main/resources/application-dev.properties`

- H2 console: `/h2-console`
- JDBC URL: `jdbc:h2:mem:fileflow;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE`
- Usuario: `sa`
- Senha: vazia
- Bucket S3 padrao: `erick-luz-files-flow`

### Configuracao `prod`

Arquivo: `src/main/resources/application-prod.properties`

Variaveis suportadas:

- `DB_URL`
- `DB_USERNAME`
- `DB_PASSWORD`
- `SPRING_PROFILES_ACTIVE=prod`

Defaults atuais:

- `DB_URL=jdbc:postgresql://fileflow.cvwku8gcym4z.us-west-2.rds.amazonaws.com:5432/fileflow`
- `DB_USERNAME=postgres`
- `DB_PASSWORD=postgres`

## Seguranca

- HTTP Basic habilitado
- Endpoint liberado: `/actuator/health`
- H2 Console liberado: `/h2-console`
- Credenciais padrao:
  - `APP_BASIC_USER=admin`
  - `APP_BASIC_PASS=admin`

## Documentacao da API

- Swagger UI: `http://localhost:8080/swagger-ui/index.html`
- OpenAPI local: `api-docs.json`

## Endpoints principais

- `POST /jobs/create`
- `POST /jobs/{jobId}/documents/create`
- `POST /jobs/{jobId}/documents/{documentId}/upload-url`
- `GET /jobs/{jobId}`
- `GET /jobs/{jobId}/documents`
- `GET /jobs/{jobId}/documents/{documentId}`

## Exemplo de health check

```bash
curl http://localhost:8080/actuator/health
```

## Exemplo de upload com URL pre-assinada

```bash
curl -X PUT "<PRESIGNED_URL>" -H "Content-Type: text/plain" --data-binary "Arquivo enviado via presigned URL"
```

## Rodando com Docker

O projeto agora possui `Dockerfile` e `.dockerignore`.

### Build da imagem

Antes do build, gere o jar com Maven:

```bash
./mvnw clean package
docker build -t file-flow:latest .
```

### Execucao do container

```bash
docker run --rm -p 8080:8080 \
  -e SPRING_PROFILES_ACTIVE=prod \
  -e DB_URL="jdbc:postgresql://host:5432/fileflow" \
  -e DB_USERNAME="postgres" \
  -e DB_PASSWORD="postgres" \
  -e APP_BASIC_USER="admin" \
  -e APP_BASIC_PASS="admin" \
  file-flow:latest
```

## Deploy em EC2

Exemplo de `User Data` para Amazon Linux com Java 21:

```bash
#!/bin/bash
set -euxo pipefail

APP_DIR=/opt/app
PORT=8080

export SPRING_PROFILES_ACTIVE=prod
export DB_URL=jdbc:postgresql://fileflow.cvwku8gcym4z.us-west-2.rds.amazonaws.com:5432/fileflow
export DB_USERNAME=postgres
export DB_PASSWORD=postgres

dnf -y update
dnf -y install java-21-amazon-corretto-headless wget

mkdir -p "$APP_DIR"

wget -O "$APP_DIR/app.jar" "https://github.com/erickluz/file-flow/releases/download/0.0.2/file-flow-0.0.2-SNAPSHOT.jar"

nohup /usr/bin/java -jar "$APP_DIR/app.jar" --server.port=$PORT --spring.profiles.active=$SPRING_PROFILES_ACTIVE > /var/log/app.log 2>&1 &
echo "STARTED: $!" >> /var/log/app.log
```

Validacao:

```bash
curl http://<ec2-public-ip>:8080/actuator/health
```

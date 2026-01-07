# Pet Store Vector Search Demo with Oracle Database and GraalVM

A Spring Boot application built with GraalVM that demonstrates semantic search using Oracle Vector Store and OpenAI embeddings.

## Prerequisites

- [GraalVM](https://www.graalvm.org/downloads/), or install with `sdk install java 25.0.1-graal`
- Oracle Database with Vector support (e.g., [Oracle 26ai Free](https://www.oracle.com/database/free/))
- OpenAI API key

## Configuration Options

You can use either a cloud Autonomous Database, or run Oracle locally with Podman/Docker.

### Option 1: Cloud Autonomous Database

### 1. Download Oracle Database Wallet for authentication

Configure and download your Autonomous Database [wallet](https://docs.oracle.com/en/cloud/paas/autonomous-database/serverless/adbsb/connect-download-wallet.html) and extract it to `src/main/resources/wallet/`.

Update the database service name in [application.properties](src/main/resources/application.properties#L3).

### Option 2: Local Database Setup

Start Oracle Database locally:

```shell
podman machine start
# podman rm -f oracle-free

# First run
podman run -d \
  -p 1521:1521 \
  --name oracle-free \
  -e ORACLE_PASSWORD=mypassword \
  -e APP_USER=appuser \
  -e APP_USER_PASSWORD=mypassword \
  -v oracle-data:/opt/oracle/oradata \
  gvenzl/oracle-free:latest

# Subsequent runs
podman start oracle-free

# podman ps 
# podman stop oracle-free
# (optional) podman volume rm oracle-data
```

Wait for the database to be ready, and run the application.

### 2. Set Environment Variables

```shell
export OPENAI_API_KEY=
export DB_PASSWORD=
```

## Running the Application

```shell
mvn spring-boot:run
```

The application automatically loads sample pet store inventory data on startup.

Now let's build it with Native Image:

```shell
mvn -Pnative native:compile
➜ eza -l ./target | grep "store"
... 107M ... vector-pet-store
./target/vector-pet-store
```

## API usage

You can search for the pet store items using natural language:

```shell
curl "http://localhost:8080/petstore/search?query=Treats%20for%20small%20loud%20dogs"
# Or with httpie:
http localhost:8080/petstore/search query=="find cat food with tuna"
```

Example requests:
- "Find dog toys" → finds "Labrador Bark Control Chews", "Heavy Duty Rope for Large Breeds"
- "Find cat food" → finds "Gourmet Tuna Soufflé for Cats", "Gourmet Chicken Soup for Cats"

The API returns semantically similar products using vector similarity search.

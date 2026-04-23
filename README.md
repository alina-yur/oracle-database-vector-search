# Pet Store Vector Search Demo with Oracle AI Database and GraalVM

A Spring Boot application built with GraalVM that demonstrates semantic search using Oracle Vector Store and OpenAI embeddings.

GitHub: [alina-yur/oracle-database-vector-search](https://github.com/alina-yur/oracle-database-vector-search)

Find full details in a [blog post](https://medium.com/oracledevs/fast-ai-search-with-graalvm-spring-boot-and-oracle-database-4e8ba46c9a74).

## Prerequisites

- [GraalVM](https://www.graalvm.org/downloads/), or install with `sdk install java 25.0.1-graal`
- Oracle Database with Vector support (for example, Autonomous Database 26ai)
- OpenAI API key

## Configuration

By default, this project uses [Oracle Autonomous Database](https://www.oracle.com/autonomous-database/) via TLS connection.

Required environment variables:

```shell
export OPENAI_API_KEY=
export DB_PASSWORD=
export ORACLE_JDBC_URL='jdbc:oracle:thin:@(description=(address=(protocol=tcps)(port=1522)(host=<your-adb-host>))(connect_data=(service_name=<your-service-name>))(security=(ssl_server_dn_match=yes)))'
```

Optional environment variable:

```shell
export DB_USERNAME=ADMIN
```

If you start with only the `(description=...)` descriptor body, prepend `jdbc:oracle:thin:@`.

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
# or with httpie
http localhost:8080/petstore/search query=="find cat food with tuna"
```

Example requests:
- "Find dog toys" -> finds "Labrador Bark Control Chews", "Heavy Duty Rope for Large Breeds"
- "Find cat food" -> finds "Gourmet Tuna Souffle for Cats", "Gourmet Chicken Soup for Cats"

The API returns semantically similar products using vector similarity search.

## Alternative: Run Oracle Database Locally

Using Docker or Podman:

```shell
podman run -d   -p 1521:1521   --name oracle-free   -e ORACLE_PASSWORD=mypassword   -e APP_USER=appuser   -e APP_USER_PASSWORD=mypassword   -v oracle-data:/opt/oracle/oradata   gvenzl/oracle-free:latest
```

Point `ORACLE_JDBC_URL` at the local database and run the app.

## Performance with GraalVM Native Image

- **Startup**: ~1.5 seconds
- **Package size**: ~107 MB standalone executable
- **Memory**: Significantly lower

Native Image compiles your Spring Boot application ahead-of-time into a self-contained executable with faster startup (typically 20-30x), lower memory footprint (typically 3-5x), and compact deployment.

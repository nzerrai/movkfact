# Setup & Development Guide - movkfact Backend

## Prerequisites

Before setting up the backend, ensure you have:

- **Java Development Kit (JDK) 17+**
  - Verify: `java -version`
  - Download: https://www.oracle.com/java/technologies/downloads/

- **Maven 3.8+**
  - Verify: `mvn -version`
  - Download: https://maven.apache.org/download.cgi

- **Git**
  - Verify: `git --version`
  - Download: https://git-scm.com/

## Initial Setup

### 1. Clone the Repository

```bash
git clone <repository-url> movkfact
cd movkfact
```

### 2. Set JWT Secret Environment Variable

For development, set a temporary JWT secret:

```bash
# Linux / macOS
export JWT_SECRET="movkfact-dev-secret-key-change-in-production-immediately"

# Windows (PowerShell)
$env:JWT_SECRET = "movkfact-dev-secret-key-change-in-production-immediately"

# Or configure in IDE run configuration
```

### 3. Build the Project

```bash
mvn clean install
```

This will:
- Download all dependencies
- Compile the source code
- Run tests
- Generate reports

## Running the Application

### Development Mode

```bash
mvn spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=dev"
```

The application will:
- Start on `http://localhost:8080`
- Use H2 in-memory database
- Log startup messages to console

### Production Mode (Local)

```bash
# Build JAR
mvn clean package

# Run JAR
java -Dspring.profiles.active=prod \
     -DJWT_SECRET="your-secure-key-here" \
     -jar target/movkfact-backend-1.0.0-SNAPSHOT.jar
```

## Testing

### Run All Tests

```bash
mvn clean test
```

### Run Specific Test Class

```bash
mvn test -Dtest=MoveFactApplicationTests
mvn test -Dtest=JwtUtilTests
```

### Generate Code Coverage Report

```bash
mvn clean test jacoco:report
```

Coverage report will be at: `target/site/jacoco/index.html`

## Accessing Services

### Health Check Endpoint

```bash
curl http://localhost:8080/api/health
```

Expected response:
```json
{"status":"UP"}
```

### H2 Database Console (Dev Only)

Open in browser: `http://localhost:8080/h2-console`

- **JDBC URL:** `jdbc:h2:mem:movkfactdb`
- **User Name:** `sa`
- **Password:** (leave blank)

## Configuration

### Application Profiles

#### Development (`application-dev.yml`)
- H2 in-memory database
- SQL logging enabled
- JWT secret via environment or default
- DDL auto: create-drop (recreate schema on startup)

#### Production (`application-prod.yml`)
- PostgreSQL database (configure connection)
- SQL logging disabled
- H2 console disabled
- DDL auto: validate (no schema changes)

### Environment Variables

| Variable | Purpose | Default |
|----------|---------|---------|
| `JWT_SECRET` | Secret key for JWT signing | `movkfact-dev-secret-key-change-in-production-immediately` |
| `SPRING_PROFILES_ACTIVE` | Active Spring profile | `dev` |
| `SERVER_PORT` | Server port | `8080` |

## Troubleshooting

### Port 8080 Already in Use

Change the port in `application-dev.yml`:
```yaml
server:
  port: 8081
```

Or set environment variable:
```bash
export SERVER_PORT=8081
mvn spring-boot:run
```

### H2 Console Not Accessible

Verify `application-dev.yml` contains:
```yaml
spring:
  h2:
    console:
      enabled: true
      path: /h2-console
```

### Build Fails: "Cannot find symbol"

- Ensure Java 17+ is installed: `java -version`
- Clean Maven cache: `mvn clean`
- Reimport Maven project in IDE (IntelliJ/Eclipse)

### Tests Fail: "Connection refused"

- H2 should auto-start with dev profile
- Check `application-dev.yml` datasource configuration
- Clear target directory: `rm -rf target`

### JWT Secret Not Read

Make sure environment variable is set BEFORE starting application:
```bash
export JWT_SECRET="your-secret-key"
mvn spring-boot:run
```

## Docker Setup

### Build Docker Image

```bash
docker build -t movkfact-backend:1.0.0 .
```

### Run Container

```bash
docker run -p 8080:8080 \
           -e JWT_SECRET="your-secret-key" \
           -e SPRING_PROFILES_ACTIVE=prod \
           movkfact-backend:1.0.0
```

## IDE Setup

### IntelliJ IDEA

1. Open project: File → Open → Select `pom.xml`
2. Let IntelliJ download dependencies
3. Run Main class: Right-click `MoveFactApplication` → Run
4. Set VM options in Run Configuration:
   - Add: `-DJWT_SECRET="your-secret-key"`

### Eclipse / Spring Tool Suite

1. Import project: File → Import → Existing Maven Projects
2. Select project folder
3. Right-click project → Maven → Update Project
4. Run as Spring Boot App: Right-click → Run As → Spring Boot App
5. Set environment variable before running

## Next Steps

1. Verify all tests pass: `mvn clean test`
2. Confirm application starts: `mvn spring-boot:run`
3. Test health endpoint: `curl http://localhost:8080/api/health`
4. Start implementing Story S1.2 (Domain Entity)

## Additional Resources

- [Spring Boot Documentation](https://spring.io/projects/spring-boot)
- [JWT Documentation](https://github.com/jwtk/jjwt)
- [H2 Database](http://www.h2database.com)
- [Maven Documentation](https://maven.apache.org/guides/)

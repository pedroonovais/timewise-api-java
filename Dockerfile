# Estágio 1: Build da aplicação
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app

# Copia arquivos de configuração do Maven
COPY pom.xml .
# Baixa dependências (cache layer)
RUN mvn dependency:go-offline -B

# Copia código fonte
COPY src ./src
# Compila a aplicação
RUN mvn clean package -DskipTests

# Estágio 2: Runtime
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Cria usuário não-root para segurança
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

# Copia o JAR do estágio de build
COPY --from=build /app/target/*.jar app.jar

# Expõe a porta da aplicação
EXPOSE 8080

# Comando para iniciar a aplicação
ENTRYPOINT ["java", "-jar", "app.jar"]


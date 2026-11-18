# ---------- Etapa 1: build com Maven ----------
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn -q dependency:go-offline
COPY src ./src
RUN mvn -q clean package -DskipTests

# ---------- Etapa 2: imagem final só com o JRE ----------
FROM eclipse-temurin:21-jre
WORKDIR /app
ENV TZ=America/Sao_Paulo
COPY --from=build /app/target/medlink.jar app.jar
EXPOSE 8080
# Limite de memória pensado no plano gratuito do Render (512 MB)
ENTRYPOINT ["java", "-Xmx380m", "-jar", "app.jar"]

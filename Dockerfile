FROM amazoncorretto:21.0.10

COPY target/file-flow-0.0.3-SNAPSHOT.jar /app/app.jar

ENV JAVA_TOOL_OPTIONS="-javaagent:otel/opentelemetry-javaagent.jar"
ENV OTEL_SERVICE_NAME=file-flow
ENV SPRING_PROFILES_ACTIVE=prod
ENV JAVA_OPTS=""

EXPOSE 8080

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]
FROM eclipse-temurin:21-jre-alpine

RUN addgroup -S eip && adduser -S eip -G eip

VOLUME /tmp

ARG JAR_FILE
COPY ${JAR_FILE} app.jar

USER eip

ENTRYPOINT ["java", \
  "-XX:+UseContainerSupport", \
  "-XX:MaxRAMPercentage=75.0", \
  "-Djava.security.egd=file:/dev/./urandom", \
  "-jar", "/app.jar"]

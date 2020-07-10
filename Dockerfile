FROM adoptopenjdk/openjdk11:alpine-jre
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring
VOLUME /core
ARG JAR_FILE=build/libs/*.jar
COPY ${JAR_FILE} core.jar
ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","/core.jar"]

FROM openjdk:11
ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} account-service.jar
ENTRYPOINT ["java","-jar","/account-service.jar"]
EXPOSE 8080
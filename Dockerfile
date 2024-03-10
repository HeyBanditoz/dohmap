FROM eclipse-temurin:21-jdk-alpine
RUN apk --no-cache add chromium chromium-chromedriver
RUN mkdir /app
RUN mkdir /.cache
COPY *.jar /app/dohmap.jar
WORKDIR /app
ENTRYPOINT ["java","-jar","/app/dohmap.jar"]

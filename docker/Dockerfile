FROM amazoncorretto:17-al2-jdk

WORKDIR /microservice

COPY ecommerceapp-backend.jar app.jar
RUN jar xvf app.jar
RUN rm app.jar

ENTRYPOINT ["java", "org.springframework.boot.loader.JarLauncher"]
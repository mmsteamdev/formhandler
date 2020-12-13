# the first stage of our build will use a maven 3.6.1 parent image
FROM maven:3.6.1-jdk-11 AS MAVEN_BUILD
# copy code to the container
COPY ./ ./
# package our application code
RUN mvn clean compile assembly:single


# the second stage of our build will use open jdk 11
FROM openjdk:11.0.9-jre

# copy only the artifacts we need from the first stage and discard the rest
COPY --from=MAVEN_BUILD /target/formhandler-1.0-SNAPSHOT-jar-with-dependencies.jar ./target/demo.jar
#COPY ./resources ./resources

#set environmental variables to database
#ENV MYSQL_URL
#ENV MYSQL_USER
#ENV MYSQL_PASSWORD
#ENV MYSQL_ROOT_PASSWORD
#ENV MYSQL_DATABASE
#ENV MAIL_TO
#ENV MAIL_SUBJECT
#ENV MAIL_HANDLER_URI

#expose ports
EXPOSE 80
EXPOSE 4000

# set the startup command to execute the jar
CMD ["java", "-jar", "/target/demo.jar"]

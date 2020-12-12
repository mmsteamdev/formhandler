# the first stage of our build will use a maven 3.6.1 parent image
FROM maven:3.6.1-jdk-11 AS MAVEN_BUILD
# copy the pom and src code to the container
COPY ./ ./
# package our application code
RUN mvn clean compile assembly:single

# the second stage of our build will use open jdk 8 on alpine 3.9
FROM openjdk:11.0.9-jre
# copy only the artifacts we need from the first stage and discard the rest
COPY --from=MAVEN_BUILD /target/twojewykladzinyformhandler-1.0-SNAPSHOT-jar-with-dependencies.jar ./target/demo.jar
COPY ./resources ./resources
#COPY --from=MAVEN_BUILD /home/~/.m2/ /home/~/.m2/
#set environmental variables to database
ENV MYSQL_URL='jdbc:mysql://twojewykladzinybaza_wykladzinymysql_1:3306'
ENV MYSQL_USER='wykladziny'
ENV MYSQL_PASSWORD='12wykladziny34#12'
ENV MYSQL_ROOT_PASSWORD='12wykladziny34#12'
ENV MYSQL_DATABASE='wykladziny'
ENV MAIL_TO='mkoziel045@gmail.com'
ENV MAIL_SUBJECT='Order'
ENV MAIL_HANDLER_URI = 'http://fitjarmail:587/mail'
#expose ports
EXPOSE 80
EXPOSE 4000
# set the startup command to execute the jar
CMD ["java", "-jar", "/target/demo.jar"]

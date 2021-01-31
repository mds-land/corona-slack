#FROM oracle/graalvm-ce:20.0.0-java8 as graalvm
# For JDK 11
FROM ghcr.io/graalvm/graalvm-ce:ol8-java11-21.0.0 as graalvm
# RUN gu install native-image

COPY . /home/app/corona-slack
WORKDIR /home/app/corona-slack

# Native image currently not working because AWT
# RUN native-image --no-server -cp target/corona-slack-*.jar

#FROM frolvlad/alpine-glibc
#RUN apk update && apk add libstdc++
RUN mkdir -p /app/data
RUN mkdir -p /app/db
RUN ls /home/app/corona-slack
EXPOSE 8080
RUN cp /home/app/corona-slack/target/corona-slack-*.jar /app/corona-slack
#COPY --from=graalvm /home/app/corona-slack/corona-slack /app/corona-slack
ENTRYPOINT ["java", "-Xmx200M", "-jar", "/app/corona-slack"]

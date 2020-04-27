#FROM oracle/graalvm-ce:20.0.0-java8 as graalvm
# For JDK 11
FROM oracle/graalvm-ce:20.0.0-java11 as graalvm
RUN gu install native-image

COPY . /home/app/corona-slack
WORKDIR /home/app/corona-slack

RUN native-image --no-server -cp target/corona-slack-*.jar

FROM frolvlad/alpine-glibc
RUN apk update && apk add libstdc++
EXPOSE 8080
COPY --from=graalvm /home/app/corona-slack/corona-slack /app/corona-slack
ENTRYPOINT ["/app/corona-slack"]

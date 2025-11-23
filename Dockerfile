FROM adoptopenjdk/openjdk11:alpine-jre
RUN echo 'http://dl-cdn.alpinelinux.org/alpine/${ALPINE_VERSION}/main' >> /etc/apk/repositories && \
    apk update && apk upgrade && apk --no-cache add curl=7.77.0-r1
COPY build/libs/*all.jar alpha-adapter-0.1.jar
COPY envoy-ready.sh envoy-ready.sh
RUN addgroup -g 1007 -S alpha
RUN adduser -u 1007 -S alpha -G alpha
USER alpha
CMD sh envoy-ready.sh alpha-adapter-0.1.jar

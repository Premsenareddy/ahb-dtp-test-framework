FROM mockserver/mockserver:mockserver-5.11.1

# expose ports.
EXPOSE 1080

# don't run MockServer as root
USER nonroot
COPY config config

ENV MOCKSERVER_PROPERTY_FILE=/config/mockserver.properties
ENV MOCKSERVER_INITIALIZATION_JSON_PATH=/config/json/TemenosExpectationsMk4.json

ENTRYPOINT ["java", "-Dfile.encoding=UTF-8", "-cp", "/mockserver-netty-jar-with-dependencies.jar:/libs/*", "-Dmockserver.propertyFile=/config/mockserver.properties", "org.mockserver.cli.Main"]

CMD ["-serverPort", "1080"]

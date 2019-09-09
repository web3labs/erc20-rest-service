FROM frolvlad/alpine-java

RUN apk update && apk upgrade && \
    apk add --no-cache bash git openssh

RUN mkdir -p /app/erc20-rest-service
RUN git clone https://github.com/blk-io/erc20-rest-service.git

# We exclude running tests as we need a Ethereum/Quorum network to be running
RUN cd erc20-rest-service \
  && ./gradlew build -x test \
  && cp build/libs/erc20-rest-service-0.1.0.jar /app/erc20-rest-service

ENV PORT=8080
ENV ENDPOINT="http://localhost:22000"
ENV FROMADDR="0xed9d02e382b34818e88b88a309c7fe71e65f419d"

ENTRYPOINT ["/usr/bin/java"]
# We can define an environment variable to interpolate these values, as Docker does not support
# this functionality, hence we have to do it in the command.
CMD ["-jar", "/app/erc20-rest-service/erc20-rest-service-0.1.0.jar", \
  "--spring.application.json={\"nodeEndpoint\":\"${ENDPOINT}\",\"fromAddress\":\"${FROMADDR}\"}"]

# ERC-20 RESTful service

This application provides a RESTful service for creating and managing 
[ERC-20 tokens](https://github.com/ethereum/EIPs/issues/20). 
It has been built using [Spring Boot](https://projects.spring.io/spring-boot/), and 
[web3j](https://web3j.io).

It works with both [Geth](https://github.com/ethereum/go-ethereum), 
[Parity](https://github.com/paritytech/parity), and 
[Quorum](https://github.com/jpmorganchase/quorum).

For Quorum, the RESTful semantics are identical, with the exception that if you wish to create 
a private transaction, you populate a HTTP header name *privateFor* with a comma-separated
list of public keys


## Build

To build a runnable jar file:

```bash
./gradlew clean build
```

## Run

Using Java 1.8+:

```bash
java -jar build/libs/azure-demo-0.1.jar 
```

By default the application will log to a file named erc20-web3j.log. 


## Configuration

The following default properties are used in the application:

```properties
# Port for service to bind to
port=8080
# Log file path and name
logging.file=logs/erc20-rest-service.log

# Endpoint of an Ethereum or Quorum node we wish to use. 
# To use IPC simply provide a file path to the socket, such as /path/to/geth.ipc
nodeEndpoint=http://localhost:22000
# The Ethereum or Quorum address we wish to use when transacting.
# Note - this address must be already unlocked in the client
fromAddress=0xed9d02e382b34818e88b88a309c7fe71e65f419d
```

You can override any of these properties by creating a file name 
*application.properties* in the root directory of your application, or in 
*config/application.properties* relative to your root. If you'd rather use yaml, 
simply change the filename to *application.yml*.


## Usage

All available application endpoints are documented using [Swagger](http://swagger.io/).

You can view the Swagger UI at http://localhost:8080/swagger-ui.html. From here you
can perform all POST and GET requests easily to facilitate deployment of, transacting 
with, and querying state of ERC-20 tokens.

![alt text](https://github.com/blk-io/erc20-rest-service/raw/master/images/full-swagger-ui.png "Swagger UI screen capture")


## Docker

We can use [Docker](https://www.docker.com/) to easily spin up a arbritrary instance 
of our service connecting to an already running Ethereum or Quorum network.

All you need to do is build the Dockerfile:

```docker
docker build -f docker/Dockerfile -t blk-io/erc20-service .
```

Then either run it with default configuration:
```docker
docker run -p 8080:8080 -v "$PWD/logs":/logs blk-io/erc20-service
```
 
Or with a custom configuration:

```docker
export PORT=8081
docker run -p ${PORT}:${PORT} -v "$PWD/logs":/logs \
    -e ENDPOINT="http://localhost:22001" \
    -e FROMADDR="0xca843569e3427144cead5e4d5999a3d0ccf92b8e" \
    -e PORT="$PORT" \
    blk-io/erc20-service
```

# BankConnector
### A web client that performs operations on Your bank account through the API.
Banks expose APIs that allow their websites to perform any kind of operations reliant on data. Through the magic of reverse-engineering, this client performs those operations using the bare minimal data required replacing the browser in the process. Just like the browser, this client uses HTTPS to communicate with the APIs. No additional requests are made, no data retained at any point. Supports ipko.pl

## Prerequisites
* Java 11 JDK
* Gradle for building and running

## Setup
There are no external dependencies. This is a simple commandline application that just needs to be built.

## Running the application
```shell
$ ./gradlew run --console=plain
```

## Example usage
```shell
$ ./gradlew run --console=plain
> Type in Your username:
$ login
> Type in Your password:
$ password
> Accounts:
...
```

## Unit tests
```shell
$ ./gradlew test
```

## Populating test data
To run integration tests, populate the existing 'src/integration-test/resources/application.properties' file with valid credentials such as this.
```text
username=your-username
password=your-password
```


## Integration tests [(test data required)](#populating-test-data)
```shell
$ ./gradlew integrationTest
```


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
To run integration tests, create a file with valid credentials and bank account/value pairs such as this.
```text
username=password

987654321=250.0
123456789=2000.0
```
Then save it as application.properties over at 'src/integration-test/resources'


## Integration tests [(test data required)](#populating-test-data)
```shell
$ ./gradlew integrationTest
```


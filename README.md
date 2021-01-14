# BankNavigator
### A web client that performs operations on Your bank account through the API.
Banks expose APIs that allow their websites to perform any kind of operations reliant on data. Through the magic of reverse-engineering, this client performs those operations using the bare minimal data required replacing the browser in the process.

## Table of contents
* [Technologies](#technologies)
* [Secure?](#secure?)
* [Setup](#setup)
* [Running the application](#running-the-application)
* [Usage](#usage)
* [Running tests](#running-tests)
* [Populating test data](#populating-test-data)

## Technologies
* Java 11
* JSoup
* JUnit 5
* Mockito

## Secure?
Just like the browser, this client uses HTTPS to communicate with the APIs. No additional requests are made, no data retained at any point.

## Setup
There are no external dependencies. This is a simple commandline application that just needs to be built.

## Running the application
```shell
$ ./gradlew run --console=plain
```

## Usage
```shell
$ ./gradlew run --console=plain
> Welcome to the BankNavigator app.
> Type in Your username:
$ login
> Type in Your password:
$ password
> Login successful.
> Accounts:
...
```

## Running tests
### Unit tests
```shell
$ ./gradlew test
```

### Integration tests [(test data required)](#populating-test-data)
```shell
$ ./gradlew integrationTest
```

## Populating test data
To run integration tests, create a file with valid credentials and bank account/value pairs such as this.
```text
username=password

987654321=250.0
123456789=2000.0
```
Then save it as application.properties over at 'src/integration-test/resources'



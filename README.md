# Periskop Scala Client

[![Build Status](https://api.cirrus-ci.com/github/periskop-dev/periskop-scala.svg)](https://cirrus-ci.com/github/periskop-dev/periskop-scala)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.periskopdev/periskop-scala_2.12/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.periskopdev/periskop-scala_2.12)

[Periskop](https://github.com/periskop-dev/periskop) requires collecting and aggregating exceptions on the client side,
as well as exposing them via an HTTP endpoint using a well defined format.

This library provides low level collection and rendering capabilities. Higher level libraries can be built be
for better integration with specific libraries and frameworks.

## Usage

Add `periskop-scala` to your SBT dependencies:

```scala
libraryDependencies += "io.github.periskop-dev" %% "periskop-scala" % "<version>"
````

Create an exception collector instance:

```scala
val exceptionCollector = new ExceptionCollector
```

Collect some exceptions:

```scala
// An Exception Without Context
exceptionCollector.add(new RuntimeException("ups!"))

// A Message Without Context
exceptionCollector.addMessage("key", "some message")

// An Exception With Context
exceptionCollector.addWithContext(
  ExceptionWithContext(
    throwable = new RuntimeException("another ups!"),
    severity = Severity.Warning,
    httpContext = Some(HttpContext(
      requestMethod = "GET",
      requestUrl = "http://example.com/path?foo=bar",
      requestHeaders = Map("Accept" -> "text/plain"),
      requestBody = Some("body")
    )))
)

// A Message With Context
exceptionCollector.addWithContext(
  ExceptionMessage(
    aggregationKey = "key2",
    message = "some other message",
    severity = Severity.Info,
    httpContext = Some(HttpContext(
      requestMethod = "GET",
      requestUrl = "http://example.com/path?foo=bar",
      requestHeaders = Map("Accept" -> "text/plain"),
      requestBody = Some("body")
    )))
)
```

Export the exceptions to a format understandable by Periskop:

```scala
val exporter = new ExceptionExporter(exceptionCollector)
exporter.export
```

The exported exceptions need to be exposed via an HTTP endpoint in order for Periskop to be able to scrape them.
See [Periskop Documentation](https://github.com/periskop-dev/periskop) for more information on how to configure a
Periskop server.

### Using push gateway

You can also use [pushgateway](https://github.com/periskop-dev/periskop-pushgateway) in case you want to push your metrics 
instead of using pull method. Use only in case you really need it (e.g a batch job) as it could degrade the performance
of your application. In the following example, we assume that we deployed an instance of periskop-pushgateway 
on `http://localhost:6767`:

```scala
val exporter = new ExceptionExporter(exceptionCollector)
exceptionCollector.add(new RuntimeException("ups!"))
exporter.pushToGateway("http://localhost:6767")
```


## Contributing

Please see [CONTRIBUTING.md](CONTRIBUTING.md).

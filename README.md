# Periskop Scala Client

[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.soundcloud/periskop-scala_2.12/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.soundcloud/periskop-scala_2.12)

[Periskop](https://github.com/soundcloud/periskop) requires collecting and aggregating exceptions on the client side,
as well as exposing them via an HTTP endpoint using a well defined format.

This library provides low level collection and rendering capabilities. Higher level libraries can be built be
for better integration with specific libraries and frameworks.

## Usage

Add `periskop-scala` to your SBT dependencies:

```scala
libraryDependencies += "com.soundcloud" %% "periskop-scala" % "<version>"
````

Create an exception collector instance:

```scala
val exceptionCollector = new ExceptionCollector
```

Collect some exceptions:

```scala
// Without Context
exceptionCollector.add(new RuntimeException("ups!"))

// With Context
exceptionCollector.addWithContext(
  ExceptionWithContext(
    throwable = new RuntimeException("another ups!"),
    severity = Severity.Warning,
    httpContext = Some(HttpContext(
      requestMethod = "GET",
      requestUrl = "http://example.com/path?foo=bar",
      requestHeaders = Map("Accept" -> "text/plain")
    )))
)
```

Export the exceptions to a format understandable by Periskop:

```scala
val exporter = new ExceptionExporter(exceptionCollector)
exporter.export
```

The exported exceptions need to be exposed via an HTTP endpoint in order for Periskop to be able to scrape them.
See [Periskop Documentation](https://github.com/soundcloud/periskop) for more information on how to configure a
Periskop server.

## Contributing

Please see [CONTRIBUTING.md](CONTRIBUTING.md)

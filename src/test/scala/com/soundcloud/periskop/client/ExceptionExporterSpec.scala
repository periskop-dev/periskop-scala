package com.soundcloud.periskop.client

import java.time.ZonedDateTime
import java.util.UUID

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import org.mockito.Mockito._
import org.specs2.mock.Mockito
import org.specs2.mutable.Specification

import scala.collection.immutable.Queue

class ExceptionExporterSpec extends Specification with Mockito {
  private val jsonMapper = new ObjectMapper().registerModule(DefaultScalaModule)

  private def parseJson(s: String) = jsonMapper.readValue(s, classOf[Map[String, Any]])

  class FakeException(msg: String, cause: Throwable = null, stackTraceLine: Int = 1)
      extends RuntimeException(msg, cause) {
    override def getStackTrace: Array[StackTraceElement] = Array(
      new StackTraceElement("kls", "mthd", "file", stackTraceLine),
      new StackTraceElement("kls", "mthd", "file", stackTraceLine + 1)
    )
  }

  "dumps the response in chunks" >> {
    val uuid1 = UUID.fromString("c3c24195-27ae-4455-ba5a-7b504a7699a4")
    val uuid2 = UUID.fromString("f12feecd-7518-46c3-88a6-38d57804e81a")
    val uuid3 = UUID.fromString("ceeefdf5-cdee-4f1b-b139-b2c739d16dcf")

    val timestamp1 = ZonedDateTime.parse("2018-01-02T11:22:33+00:00")
    val timestamp2 = ZonedDateTime.parse("2018-01-02T11:22:55+00:00")

    val exceptionAggregates: Seq[ExceptionAggregate] = Seq(
      ExceptionAggregate(
        totalCount = 3,
        severity = Severity.Error,
        createdAt = timestamp1,
        latestExceptions = Queue(
          ExceptionWithContext(
            throwable = new FakeException("foo1", new FakeException("foo1parent")),
            severity = Severity.Error,
            uuid = uuid1,
            timestamp = timestamp1,
            httpContext = Some(
              HttpContext(
                requestMethod = "POST",
                requestHeaders = Map("User-Agent" -> "Foobrowser", "Accept" -> "text/html"),
                requestUrl = "http://foo.com/boo",
                requestBody = Some("body")
              )
            )
          ),
          ExceptionWithContext(
            throwable = new FakeException("foo1", new FakeException("foo1parent")),
            severity = Severity.Error,
            uuid = uuid1,
            timestamp = timestamp1,
            httpContext = Some(
              HttpContext(
                requestMethod = "POST",
                requestHeaders = Map("User-Agent" -> "Foobrowser", "Accept" -> "text/html"),
                requestUrl = "http://foo.com/boo",
                requestBody = None
              )
            )
          ),
          ExceptionWithContext(
            throwable = new FakeException("foo2"),
            severity = Severity.Error,
            uuid = uuid2,
            timestamp = timestamp2,
            httpContext = None
          )
        )
      ),
      ExceptionAggregate(
        totalCount = 1,
        severity = Severity.Error,
        createdAt = timestamp1,
        latestExceptions = Queue(
          ExceptionWithContext(
            throwable = new FakeException("bar1", stackTraceLine = 42),
            severity = Severity.Error,
            uuid = uuid3,
            timestamp = timestamp1,
            httpContext = None
          )
        )
      )
    )

    val collector = smartMock[ExceptionCollector]
    when(collector.getExceptionAggregates).thenReturn(exceptionAggregates)

    val exporter = new ExceptionExporter(collector)

    parseJson(exporter.export) ==== parseJson(
      s"""|{
          |  "aggregated_errors": [
          |    {
          |      "aggregation_key": "${exceptionAggregates(0).latestExceptions.head.aggregationKey}",
          |      "total_count": 3,
          |      "severity": "error",
          |      "created_at": "2018-01-02T11:22:33.000Z",
          |      "latest_errors": [
          |        {
          |          "error": {
          |            "class": "com.soundcloud.periskop.client.ExceptionExporterSpec$$FakeException",
          |            "message": "foo1",
          |            "stacktrace": [
          |              "kls.mthd(file:1)",
          |              "kls.mthd(file:2)"
          |            ],
          |            "cause": {
          |              "class": "com.soundcloud.periskop.client.ExceptionExporterSpec$$FakeException",
          |              "message": "foo1parent",
          |              "stacktrace": [
          |                "kls.mthd(file:1)",
          |                "kls.mthd(file:2)"
          |              ],
          |              "cause": null
          |            }
          |          },
          |          "severity": "error",
          |          "uuid": "c3c24195-27ae-4455-ba5a-7b504a7699a4",
          |          "timestamp": "2018-01-02T11:22:33.000Z",
          |          "http_context": {
          |            "request_method": "POST",
          |            "request_url": "http://foo.com/boo",
          |            "request_headers": {
          |              "User-Agent": "Foobrowser",
          |              "Accept": "text/html"
          |            },
          |            "request_body": "body"
          |          }
          |        },
          |        {
          |          "error": {
          |            "class": "com.soundcloud.periskop.client.ExceptionExporterSpec$$FakeException",
          |            "message": "foo1",
          |            "stacktrace": [
          |              "kls.mthd(file:1)",
          |              "kls.mthd(file:2)"
          |            ],
          |            "cause": {
          |              "class": "com.soundcloud.periskop.client.ExceptionExporterSpec$$FakeException",
          |              "message": "foo1parent",
          |              "stacktrace": [
          |                "kls.mthd(file:1)",
          |                "kls.mthd(file:2)"
          |              ],
          |              "cause": null
          |            }
          |          },
          |          "severity": "error",
          |          "uuid": "c3c24195-27ae-4455-ba5a-7b504a7699a4",
          |          "timestamp": "2018-01-02T11:22:33.000Z",
          |          "http_context": {
          |            "request_method": "POST",
          |            "request_url": "http://foo.com/boo",
          |            "request_headers": {
          |              "User-Agent": "Foobrowser",
          |              "Accept": "text/html"
          |            },
          |            "request_body": null
          |          }
          |        },
          |        {
          |          "error": {
          |            "class": "com.soundcloud.periskop.client.ExceptionExporterSpec$$FakeException",
          |            "message": "foo2",
          |            "stacktrace": [
          |              "kls.mthd(file:1)",
          |              "kls.mthd(file:2)"
          |            ],
          |            "cause": null
          |          },
          |          "severity": "error",
          |          "uuid": "f12feecd-7518-46c3-88a6-38d57804e81a",
          |          "timestamp": "2018-01-02T11:22:55.000Z",
          |          "http_context": null
          |        }
          |      ]
          |    },
          |    {
          |      "aggregation_key": "${exceptionAggregates(1).latestExceptions.head.aggregationKey}",
          |      "total_count": 1,
          |      "severity": "error",
          |      "created_at": "2018-01-02T11:22:33.000Z",
          |      "latest_errors": [
          |        {
          |          "error": {
          |            "class": "com.soundcloud.periskop.client.ExceptionExporterSpec$$FakeException",
          |            "message": "bar1",
          |            "stacktrace": [
          |              "kls.mthd(file:42)",
          |              "kls.mthd(file:43)"
          |            ],
          |            "cause": null
          |          },
          |          "severity": "error",
          |          "uuid": "ceeefdf5-cdee-4f1b-b139-b2c739d16dcf",
          |          "timestamp": "2018-01-02T11:22:33.000Z",
          |          "http_context": null
          |        }
          |      ]
          |    }
          |  ]
          |}
          |""".stripMargin
    )
  }

}

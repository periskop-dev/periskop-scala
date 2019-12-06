package com.soundcloud.periskop.client

import java.time.format.DateTimeFormatter

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.scala.DefaultScalaModule

class ExceptionExporter(exceptionCollector: ExceptionCollector) {
  private val rfc3339TimeFormat = DateTimeFormatter
    .ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSXXX")

  private val jsonMapper = new ObjectMapper()
    .configure(JsonGenerator.Feature ESCAPE_NON_ASCII, true)
    .registerModule(DefaultScalaModule)

  def export: String = {
    val payload = jsonResult(
      exceptionAggregates = exceptionCollector.getExceptionAggregates
    )

    jsonMapper.writeValueAsString(payload)
  }

  private def jsonException(t: Throwable): Map[String, Any] = Map(
    "class" -> t.getClass.getName,
    "message" -> t.getMessage,
    "stacktrace" -> t.getStackTrace.map(_.toString),
    "cause" -> Option(t.getCause).map(jsonException)
  )

  private def jsonHttpContext(httpContext: HttpContext): Map[String, Any] = Map(
    "request_method" -> httpContext.requestMethod,
    "request_url" -> httpContext.requestUrl,
    "request_headers" -> httpContext.requestHeaders
  )

  private def jsonErrorWithContext(e: ExceptionWithContext): Map[String, Any] = Map(
    "error" -> jsonException(e.throwable),
    "severity" -> Severity.toString(e.severity),
    "uuid" -> e.uuid.toString,
    "timestamp" -> e.timestamp.format(rfc3339TimeFormat),
    "http_context" -> e.httpContext.map(jsonHttpContext)
  )

  private def jsonAggregatedErrors(aggregate: ExceptionAggregate): Map[String, Any] = Map(
    "aggregation_key" -> aggregate.aggregationKey,
    "total_count" -> aggregate.totalCount,
    "severity" -> Severity.toString(aggregate.severity),
    "latest_errors" -> aggregate.latestExceptions.map(jsonErrorWithContext)
  )

  private def jsonResult(exceptionAggregates: Seq[ExceptionAggregate]): Map[String, Any] = Map(
    "aggregated_errors" -> exceptionAggregates.map(jsonAggregatedErrors)
  )

}
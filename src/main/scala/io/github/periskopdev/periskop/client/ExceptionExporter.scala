package io.github.periskopdev.periskop.client

import com.fasterxml.jackson.core.json.JsonWriteFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import scalaj.http._

import java.time.format.DateTimeFormatter

class ExceptionExporter(exceptionCollector: ExceptionCollector) {

  private val rfc3339TimeFormat = DateTimeFormatter
    .ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSXXX")

  private val jsonMapper = new ObjectMapper()
    .configure(JsonWriteFeature.ESCAPE_NON_ASCII.mappedFeature(), true)
    .registerModule(DefaultScalaModule)

  def export: String = {
    val payload = jsonResult(
      exceptionAggregates = exceptionCollector.getExceptionAggregates
    )

    jsonMapper.writeValueAsString(payload)
  }

  def pushToGateway(addr: String): HttpResponse[String] = {
    Http(s"$addr/errors").postData(export).asString
  }

  private def jsonExceptionWithContext(t: Throwable): Map[String, Any] = Map(
    "class" -> t.getClass.getName,
    "message" -> t.getMessage,
    "stacktrace" -> t.getStackTrace.map(_.toString),
    "cause" -> Option(t.getCause).map(jsonExceptionWithContext)
  )

  private def jsonExceptionMessage(m: String): Map[String, Any] = Map(
    "message" -> m
  )

  private def jsonHttpContext(httpContext: HttpContext): Map[String, Any] =
    Map(
      "request_method" -> httpContext.requestMethod,
      "request_url" -> httpContext.requestUrl,
      "request_headers" -> httpContext.requestHeaders,
      "request_body" -> httpContext.requestBody
    )

  private def jsonErrorWithContext(e: ExceptionOccurrence): Map[String, Any] = {
    val error = e match {
      case ExceptionWithContext(throwable, _, _, _, _) => jsonExceptionWithContext(throwable)
      case ExceptionMessage(_, message, _, _, _, _) => jsonExceptionMessage(message)
      case _ => Map.empty
    }

    Map(
      "error" -> error,
      "severity" -> Severity.toString(e.severity),
      "uuid" -> e.uuid.toString,
      "timestamp" -> e.timestamp.format(rfc3339TimeFormat),
      "http_context" -> e.httpContext.map(jsonHttpContext)
    )
  }

  private def jsonAggregatedErrors(aggregate: ExceptionAggregate): Map[String, Any] = Map(
    "aggregation_key" -> aggregate.aggregationKey,
    "total_count" -> aggregate.totalCount,
    "severity" -> Severity.toString(aggregate.severity),
    "created_at" -> aggregate.createdAt.format(rfc3339TimeFormat),
    "latest_errors" -> aggregate.latestExceptions.map(jsonErrorWithContext)
  )

  private def jsonResult(exceptionAggregates: Seq[ExceptionAggregate]): Map[String, Any] = Map(
    "aggregated_errors" -> exceptionAggregates.map(jsonAggregatedErrors),
    "target_uuid" -> exceptionCollector.uuid
  )

}

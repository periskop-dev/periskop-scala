package io.github.periskopdev.periskop.client

import java.time.ZonedDateTime
import java.util.UUID
import scala.util.hashing.MurmurHash3

/** Additional HTTP-related context for ExceptionWithContext.
  */
case class HttpContext(
    requestMethod: String,
    requestUrl: String,
    requestHeaders: Map[String, String],
    requestBody: Option[String]
)

trait ExceptionOccurrence {
  def exceptionThrowable: Option[Throwable]
  def severity: Severity
  def uuid: UUID
  def timestamp: ZonedDateTime
  def httpContext: Option[HttpContext]
  def aggregationKey: String
  def message: String
}

/** Wraps an exception with useful metadata.
  */
case class ExceptionWithContext(
    throwable: Throwable,
    val severity: Severity,
    val uuid: UUID = UUID.randomUUID,
    val timestamp: ZonedDateTime = ZonedDateTime.now,
    val httpContext: Option[HttpContext] = None
) extends ExceptionOccurrence {
  val className: String = throwable.getClass.getName
  val message: String = throwable.getMessage()
  val exceptionThrowable = Some(throwable)

  /** Key used to group exceptions (and then limit the number of kept exceptions FIFO-style).
    *
    * Derived from exception class and backtrace to be as specific as possible, without potentially
    * including information that is different for each exception (e.g. request ids).
    *
    * We only use the first 5 lines of the backtrace to avoid having different hashes for the same
    * exception group that happen when using non-deterministic/recursive calls (i.e. Netty event loop).
    */
  val aggregationKey: String = {
    val backtraceHead = throwable.getStackTrace.take(5).map(_.toString)
    val backtraceHash = MurmurHash3.arrayHash(backtraceHead)
    s"$className@${backtraceHash.toHexString}"
  }
}

/** A simple message without throwable.
  */
case class ExceptionMessage(
    val aggregationKey: String,
    val message: String,
    val severity: Severity,
    val uuid: UUID = UUID.randomUUID,
    val timestamp: ZonedDateTime = ZonedDateTime.now,
    val httpContext: Option[HttpContext] = None
) extends ExceptionOccurrence {
  lazy val exceptionThrowable = None
}

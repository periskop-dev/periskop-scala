package com.soundcloud.periskop.client

import java.time.LocalDateTime
import java.util.UUID

import scala.util.hashing.MurmurHash3

/**
  * Additional HTTP-related context for ExceptionWithContext.
  */
case class HttpContext(requestMethod: String, requestUrl: String, requestHeaders: Map[String, String])

/**
  * Wraps an exception with useful metadata.
  */
case class ExceptionWithContext
(
  throwable: Throwable,
  severity: Severity,
  uuid: UUID = UUID.randomUUID,
  timestamp: LocalDateTime = LocalDateTime.now,
  httpContext: Option[HttpContext] = None
) {
  val className: String = throwable.getClass.getName

  /**
    * Key used to group exceptions (and then limit the number of kept exceptions FIFO-style).
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

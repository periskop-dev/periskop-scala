package com.soundcloud.periskop.client

import scala.collection.immutable.Queue

private[client] case class ExceptionAggregate
(
  totalCount: Long = 0,
  severity: Severity = Severity.Error ,
  latestExceptions: Queue[ExceptionWithContext] = Queue.empty
) {
    // limit memory consumption keep only N exceptions per aggregation key
    val maxExceptions = 10

    def aggregationKey: String = latestExceptions.head.aggregationKey

    def add(exceptionWithContext: ExceptionWithContext): ExceptionAggregate = {
      require(latestExceptions.isEmpty || aggregationKey == exceptionWithContext.aggregationKey)

      val truncatedLatest = if (latestExceptions.size < maxExceptions) latestExceptions else latestExceptions.dequeue._2
      copy(
        totalCount = totalCount + 1,
        severity = exceptionWithContext.severity,
        latestExceptions = truncatedLatest enqueue exceptionWithContext
      )
    }
}

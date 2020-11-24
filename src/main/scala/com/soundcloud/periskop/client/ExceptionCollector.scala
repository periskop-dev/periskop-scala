package com.soundcloud.periskop.client

import java.util.concurrent.ConcurrentHashMap
import java.util.function.BiFunction
import scala.jdk.CollectionConverters._

class ExceptionCollector {
  private val exceptions = new ConcurrentHashMap[String, ExceptionAggregate]

  /** Collect an exception without providing an HTTP context.
    */
  def add(throwable: Throwable, severity: Severity = Severity.Error): Unit = addExceptionWithContext(
    ExceptionWithContext(throwable, severity)
  )

  /** Collect an exception providing an HTTP context.
    */
  def addWithContext(exceptionWithContext: ExceptionWithContext): Unit = addExceptionWithContext(exceptionWithContext)

  /** Get a dump of all exception aggregates.
    */
  def getExceptionAggregates: Seq[ExceptionAggregate] = {
    exceptions.values.asScala.toSeq
  }

  private def addExceptionWithContext(exception: ExceptionWithContext): Unit = {
    exceptions.compute(
      exception.aggregationKey,
      new BiFunction[String, ExceptionAggregate, ExceptionAggregate] {
        override def apply(k: String, v: ExceptionAggregate): ExceptionAggregate = {
          Option(v)
            .getOrElse(ExceptionAggregate())
            .add(exception)
        }
      }
    )
    ()
  }
}

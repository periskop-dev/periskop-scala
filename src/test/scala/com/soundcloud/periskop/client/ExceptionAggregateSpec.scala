package com.soundcloud.periskop.client

import org.specs2.mutable.Specification
import org.specs2.specification.Scope

class ExceptionAggregateSpec extends Specification {

  trait Context extends Scope {
    val e: Throwable = new RuntimeException("foo")

    val empty = ExceptionAggregate()
  }

  "starts with empty exceptions and zero count" in new Context {
    empty.totalCount ==== 0
    empty.latestExceptions.isEmpty ==== true
  }

  "adds new exceptions to the queue and count" in new Context {
    val a: ExceptionAggregate = empty
      .add(ExceptionWithContext(e, Severity.Error))
      .add(ExceptionWithContext(e, Severity.Error))
      .add(ExceptionWithContext(e, Severity.Error))

    a.totalCount ==== 3
    a.latestExceptions.length ==== 3
    a.latestExceptions.head.asInstanceOf[ExceptionWithContext].throwable ==== e
    a.latestExceptions.head.severity ==== Severity.Error

    a.latestExceptions.last.asInstanceOf[ExceptionWithContext].throwable ==== a.latestExceptions.head.asInstanceOf[ExceptionWithContext].throwable
    a.latestExceptions.last !=== a.latestExceptions.head
  }

  "returns the aggregation key" in new Context {
    val a: ExceptionAggregate = empty
      .add(ExceptionWithContext(e, Severity.Error))

    a.aggregationKey ==== ExceptionWithContext(e, Severity.Error).aggregationKey
  }

  "keeps only N exceptions in queue" in new Context {
    val a: ExceptionAggregate = (1 to 15).foldLeft(empty) { case (agg, i) =>
      agg.add(ExceptionWithContext(new RuntimeException(s"foo $i"), Severity.Error))
    }

    a.totalCount ==== 15
    a.latestExceptions.length ==== 10
    a.latestExceptions.head.asInstanceOf[ExceptionWithContext].throwable.getMessage ==== "foo 6"
    a.latestExceptions.last.asInstanceOf[ExceptionWithContext].throwable.getMessage ==== "foo 15"
  }

  "throws if the aggregation key does not match" in new Context {
    val a: ExceptionAggregate = empty
      .add(ExceptionWithContext(new RuntimeException("foo"), Severity.Error))

    a.add(ExceptionWithContext(new RuntimeException("foo"), Severity.Error)) must throwA[IllegalArgumentException]
  }
}

package com.soundcloud.periskop.client

import org.specs2.mutable.Specification
import org.specs2.specification.Scope

class ExceptionOccurrenceSpec extends Specification {

  class TestExceptionWithStacktrace(stacktrace: Array[StackTraceElement]) extends RuntimeException {
    override def getStackTrace: Array[StackTraceElement] = stacktrace
  }

  "ExceptionWithContext" >> {
    trait Context extends Scope {
      val e: Throwable = new RuntimeException("foo")
    }

    "className is the exception class name" in new Context {
      ExceptionWithContext(e, Severity.Error).className == "java.lang.RuntimeException"
    }

    "aggregationKey is based on the class name and stacktrace only" in new Context {
      val eArr = Array(1, 2).map { i => new RuntimeException(s"foo $i") }
      val (e1, e2) = (eArr(0), eArr(1))
      val e3: Throwable = new RuntimeException("foo 2")

      // do not match on exact string, backtrace hash changes when we change code
      ExceptionWithContext(e1, Severity.Error).aggregationKey must beMatching(
        """\Ajava.lang.RuntimeException@[0-9a-f]{4,8}\z""".r
      )

      ExceptionWithContext(e1, Severity.Error).aggregationKey ===
        ExceptionWithContext(e2, Severity.Error).aggregationKey

      ExceptionWithContext(e2, Severity.Error).aggregationKey !===
        ExceptionWithContext(e3, Severity.Error).aggregationKey
    }

    "aggregationKey is using only first 5 lines of stacktrace" in new Context {
      val e1: Throwable = new TestExceptionWithStacktrace(
        Array(
          new StackTraceElement("x", "x", "x", 1),
          new StackTraceElement("x", "x", "x", 1),
          new StackTraceElement("x", "x", "x", 1),
          new StackTraceElement("x", "x", "x", 1),
          new StackTraceElement("x", "x", "x", 1),
          new StackTraceElement("x", "x", "x", 1)
        )
      )

      val e2: Throwable = new TestExceptionWithStacktrace(
        Array(
          new StackTraceElement("x", "x", "x", 1),
          new StackTraceElement("x", "x", "x", 1),
          new StackTraceElement("x", "x", "x", 1),
          new StackTraceElement("x", "x", "x", 1),
          new StackTraceElement("x", "x", "x", 1),
          new StackTraceElement("x", "x", "x", 99)
        )
      )

      val e3: Throwable = new TestExceptionWithStacktrace(
        Array(
          new StackTraceElement("x", "x", "x", 1),
          new StackTraceElement("x", "x", "x", 1),
          new StackTraceElement("x", "x", "x", 1),
          new StackTraceElement("x", "x", "x", 1),
          new StackTraceElement("x", "x", "x", 99),
          new StackTraceElement("x", "x", "x", 1)
        )
      )

      ExceptionWithContext(e1, Severity.Error).aggregationKey ===
        ExceptionWithContext(e2, Severity.Error).aggregationKey

      ExceptionWithContext(e1, Severity.Error).aggregationKey !===
        ExceptionWithContext(e3, Severity.Error).aggregationKey
    }

    "UUID values are different for each instance" in new Context {
      ExceptionWithContext(e, Severity.Error).uuid !=== ExceptionWithContext(e, Severity.Error).uuid
    }
  }

  "ExceptionMessage" >> {
    trait Context extends Scope {}

    "UUID values are different for each instance" in new Context {
      ExceptionMessage("key", "message", Severity.Info).uuid !=== ExceptionMessage("key2", "message2", Severity.Info).uuid
    }
  }
}

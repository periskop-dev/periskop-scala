package com.soundcloud.periskop.client

import org.specs2.mock.Mockito
import org.specs2.mutable.Specification
import org.specs2.specification.Scope

class ExceptionCollectorSpec extends Specification with Mockito {

  "#add" >> {
    class FooException(msg: String) extends RuntimeException

    trait Context extends Scope {

      val collector = new ExceptionCollector

      val e: Throwable = new FooException("bar")
      val ewc = ExceptionWithContext(e, Severity.Error)
    }

    "saves exceptions internally and exposes them" in new Context {
      collector.add(e)
      collector.add(e)
      collector.add(new RuntimeException("bar"))

      val a: Seq[ExceptionAggregate] = collector.getExceptionAggregates.sortBy(_.aggregationKey)
      a.length ==== 2

      a.head.totalCount === 2
      a.head.latestExceptions.head.asInstanceOf[ExceptionWithContext].throwable ==== e

      a.last.totalCount === 1
    }

    "accepts raw exceptions" in new Context {
      collector.add(e)

      collector.getExceptionAggregates.head.latestExceptions.head.asInstanceOf[ExceptionWithContext].throwable ==== e
    }
  }

  "#addMessage" >> {
    trait Context extends Scope {
      val collector = new ExceptionCollector
    }

    "saves messages internally and exposes them" in new Context {
      collector.addMessage("key1", "message1", Severity.Info)
      collector.addMessage("key1", "message2", Severity.Info)
      collector.addMessage("key2", "message3", Severity.Info)

      val a: Seq[ExceptionAggregate] = collector.getExceptionAggregates.sortBy(_.aggregationKey)
      a.length ==== 2

      a.head.totalCount === 2
      a.head.latestExceptions.head.asInstanceOf[ExceptionMessage].message ==== "message1"

      a.last.totalCount === 1
    }
  }
}

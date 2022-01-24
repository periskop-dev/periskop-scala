package io.github.periskopdev.periskop.client

sealed trait Severity

object Severity {
  object Info extends Severity
  object Warning extends Severity
  object Error extends Severity

  def parse(severityString: String): Severity = {
    severityString match {
      case "info" => Info
      case "warning" => Warning
      case _ => Error
    }
  }

  def toString(severity: Severity): String = {
    severity match {
      case Info => "info"
      case Warning => "warning"
      case Error => "error"
    }
  }
}

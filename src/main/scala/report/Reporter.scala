package report

// the classes implement this trait define its own report summary format
trait Reporter {
  def report()
}


object Reporter {
  
  private var registeredReporters: Seq[Reporter] = Seq()
  
  def addReporter(reporter: Reporter*): Seq[Reporter] = {
    registeredReporters = registeredReporters ++ reporter
    registeredReporters
  }
  
  def getReporters: Seq[Reporter] = registeredReporters
}

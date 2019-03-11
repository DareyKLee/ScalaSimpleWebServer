import java.io._
import java.net._
import java.util.NoSuchElementException

import scala.io._

object SimpleWebServer {
  def main(args: Array[String]): Unit ={
    val server = new ServerSocket(9999)

    while (true) {
     serve(server)
    }
  }

  def serve(server: ServerSocket): Unit ={
    val s = server.accept()
    val in = new BufferedSource(s.getInputStream).getLines
    val out = new PrintStream(s.getOutputStream)

    getResource(in, out)

    s.close()
  }

  def getResource(in: Iterator[String], out: PrintStream): Unit ={
    try {
      val resourceName = parseInput(in.next())

      if (resourceName == "INVALID") {
        output500Error(out)
      } else {
        val sourceHTML = Source.fromResource(resourceName).getLines.mkString("\n")

        outputOkayHeader(out)
        outputHTML(sourceHTML, out)

        out.flush()
      }
    }
    catch {
      case emptyNext: NoSuchElementException => ()
      case nullPointer: NullPointerException => output404Error(out)
    }
  }

  def parseInput(input: String): String ={
    val inputChopped = input.split(" ")
    val verb = inputChopped(0)
    val httpHeader = inputChopped(1)

    if (verb == "GET"){
      if (httpHeader == "/"){
        "index.html"
      } else {
        val resourceName = httpHeader.substring(1)

        if (!resourceName.contains(".html")) {
          resourceName + ".html"
        } else {
          resourceName
        }
      }
    } else {
      "INVALID"
    }
  }

  def outputOkayHeader(out: PrintStream): Unit ={
    out.println("HTTP/1.1 200 OK" + "\n"
                + "Content-Type: text/html; charset=UTF-8" + "\n")
  }

  def outputHTML(sourceHTML: String, out: PrintStream): Unit ={
    out.println(sourceHTML)
  }

  def output404Error(out: PrintStream): Unit ={
    out.println("HTTP/1.1 404 NOT FOUND" + "\n"
                + "Content-Type: text/html; charset=UTF-8" + "\n\n"
                + "RESOURCE NOT FOUND")
  }

  def output500Error(out: PrintStream): Unit ={
    out.println("HTTP/1.1 500 Internal Error" + "\n"
      + "Content-Type: text/html; charset=UTF-8" + "\n\n"
      + "ONLY GET REQUESTS ACCEPTED")
  }
}
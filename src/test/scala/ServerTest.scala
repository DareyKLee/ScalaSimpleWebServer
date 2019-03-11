import java.io._
import java.net._

import org.scalatest._
import org.mockito.Mockito._
import org.scalatest.mockito.MockitoSugar

import scala.io.Source

class ServerTest extends FlatSpec with Matchers with MockitoSugar {
  "parseInput with hello or hello.html" should "be hello.html" in {
    SimpleWebServer.parseInput("GET /hello HTTP/1.1") should be ("hello.html")
    SimpleWebServer.parseInput("GET /hello.html HTTP/1.1") should be ("hello.html")
  }

  "parseInput with /" should "be index.html" in {
    SimpleWebServer.parseInput("GET / HTTP/1.1") should be ("index.html")
  }

  "outputOkHeader" should "be a 200 OK http header" in {
    val mock_PrintStream = mock[PrintStream]
    val test_ByteArrayOutputStream = new ByteArrayOutputStream

    when (mock_PrintStream.println(test_ByteArrayOutputStream)).thenCallRealMethod()

    SimpleWebServer.outputOkayHeader(mock_PrintStream)

    verify(mock_PrintStream).println("HTTP/1.1 200 OK" + "\n"
                                     + "Content-Type: text/html; charset=UTF-8" + "\n")
  }

  "outputHTML" should "be the contents of goodbye.html file" in {
    val mock_PrintStream = mock[PrintStream]
    val test_ByteArrayOutputStream = new ByteArrayOutputStream
    val sourceHTML = Source.fromResource("goodbye.html").getLines.mkString("\n")

    when (mock_PrintStream.println(test_ByteArrayOutputStream)).thenCallRealMethod()

    SimpleWebServer.outputHTML(sourceHTML, mock_PrintStream)

    verify(mock_PrintStream).println(sourceHTML)
  }

  "output404Error" should "be a 404 NOT FOUND HTTP HEADER" in {
    val mock_PrintStream = mock[PrintStream]

    when (mock_PrintStream.println(mock_PrintStream)).thenCallRealMethod()

    SimpleWebServer.output404Error(mock_PrintStream)

    verify(mock_PrintStream).println("HTTP/1.1 404 NOT FOUND" + "\n"
                                     + "Content-Type: text/html; charset=UTF-8" + "\n\n"
                                     + "RESOURCE NOT FOUND")
  }

  "getResource" should "flush() for a existing resource and NOT encounter a NullPointerException" in {
    val mock_Iterator = mock[Iterator[String]]
    val mock_PrintStream = mock[PrintStream]

    when (mock_Iterator.next()).thenReturn("GET /hello HTTP/1.1")

    val stub: Unit = SimpleWebServer.getResource(mock_Iterator, mock_PrintStream)

    verify(mock_PrintStream).flush()
    noException should be thrownBy stub
  }

  "getResource" should "throw NullPointerException" in {
    val mock_Iterator = mock[Iterator[String]]
    val mock_PrintStream = mock[PrintStream]

    when (mock_Iterator.next()).thenReturn("GET /nothing HTTP/1.1")

    assertThrows[NullPointerException] {
      SimpleWebServer.getResource(mock_Iterator, mock_PrintStream)
    }
  }

  "InputStream of existing resource name" should "be a 200 OK http header and resource contents in OutputStream" in {
    val mock_ServerSocket = mock[ServerSocket]
    val mock_Socket = mock[Socket]
    val test_ByteArrayInputStream = new ByteArrayInputStream("GET /hello HTTP/1.1".getBytes)
    val test_ByteArrayOutputStream = new ByteArrayOutputStream

    when(mock_ServerSocket.accept).thenReturn(mock_Socket)
    when(mock_Socket.getInputStream).thenReturn(test_ByteArrayInputStream)
    when(mock_Socket.getOutputStream).thenReturn(test_ByteArrayOutputStream)

    SimpleWebServer.serve(mock_ServerSocket)

    val test_OutputString = test_ByteArrayOutputStream.toString
    val sourceHTML = Source.fromResource("hello.html").getLines.mkString("\n")

    assert(test_OutputString.contains("HTTP/1.1 200 OK" + "\n"
                                      + "Content-Type: text/html; charset=UTF-8" + "\n"))
    assert(test_OutputString.contains(sourceHTML))
    verify(mock_Socket).close()
  }

  "output505Error" should "send 500 http internal error" in {
    val mock_PrintStream = mock[PrintStream]

    SimpleWebServer.output500Error(mock_PrintStream)

    verify(mock_PrintStream).println("HTTP/1.1 500 Internal Error" + "\n"
      + "Content-Type: text/html; charset=UTF-8" + "\n\n"
      + "ONLY GET REQUESTS ACCEPTED")
  }
}

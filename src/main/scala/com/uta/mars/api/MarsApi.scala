package com.uta.mars.api

import java.net.{ConnectException, HttpCookie, SocketTimeoutException}

import com.typesafe.scalalogging.LazyLogging
import org.scalactic._
import play.api.libs.json.{Json, Reads}
import scala.util.{Failure, Success, Try}
import scalacache.guava.GuavaCache
import scalaj.http.{HttpResponse, Http, HttpRequest}
import scalacache._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.duration._

case class Err(code: Int, msg: String)

trait Session {
  def cookies: Seq[HttpCookie]
}

object MarsApi extends AnyRef with LazyLogging {
  private implicit val scalaCache = ScalaCache(GuavaCache())
  private val baseUrl = "http://52.33.35.165:8080/api"

  private def POST(route: String) = Http(baseUrl + route).timeout(10000, 10000).method("POST")
  private def GET(route: String)  = Http(baseUrl + route).timeout(10000, 10000).method("GET")

  def login(username: String, password: String): FutureOr[IndexedSeq[HttpCookie], Err] = {
    val request = Future {
      Try(POST("/session/login").auth(username, password).asString) match {
        case Success(response) =>
          response.code match {
            case 200 => scalacache.removeAll(); Good(response.cookies)
            case code => logger.info(s"${response.code}-${response.body}"); Bad(Err(code, response.body))
          }
        case Failure(ex) => Bad(exceptionToError(ex))
      }
    }
    FutureOr(request)
  }

  def accountInfo(implicit session: Session): FutureOr[Account, Err] = cachingCall[Account](GET("/account"))

  def assistantInfo(implicit session: Session): FutureOr[Assistant, Err] = cachingCall[Assistant](GET("/assistant"))

  /**
   * This method will check if the response to the request has been cached to avoid unnecessary trips over network.
   * If the the response is not cached, the request happens and the result is cached on status code 200 for future usage.
   */
  private def cachingCall[R: Reads](request: HttpRequest)(implicit session: Session): FutureOr[R, Err] = {
    def doRequestCall(): Or[R, Err] = {
      Try(request.cookies(session.cookies).asString) match {
        case Success(HttpResponse(body, 200, _))  => sync.cachingWithTTL(request)(10.minutes)(body); Good(Json.parse(body).as[R])
        case Success(HttpResponse(body, code, _)) => logger.info(s"$code -> $body"); Bad(Err(code, body))
        case Failure(ex)                          => Bad(exceptionToError(ex))
      }
    }

    FutureOr(
      scalacache.get[String](request).map {
        case Some(cacheHit) => Good(Json.parse(cacheHit).as[R]) // cache hit
        case None           => doRequestCall() // cache missed; do the http request to get the data.
      }
    )
  }

  private def exceptionToError(ex: Throwable): Err = ex match {
    case ex: ConnectException =>
      logger.info(s"ConnectException: ${ex.getMessage}")
      Err(498, "No Internet connection.")
    case ex: SocketTimeoutException =>
      logger.info(s"SocketTimeoutException: ${ex.getMessage}")
      Err(503, "The server is currently unavailable (because it is overloaded or down for maintenance).")
    case _ =>
      logger.error(ex.getMessage, ex)
      Err(499, "The application has encounter an unexpected error.")
  }
}

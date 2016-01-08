package com.uta.mars.api

import java.net.{ConnectException, HttpCookie, SocketTimeoutException}

import com.typesafe.scalalogging.LazyLogging
import play.api.libs.json.{Json, Reads}
import scala.util.{Failure, Success, Try}
import scalacache.guava.GuavaCache
import scalaj.http.{Http, HttpRequest}
import scalacache._
import memoization._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.duration._


sealed trait ApiResponse[+A]
case class Ok[A](data: A) extends ApiResponse[A]
case class Err(code: Int, msg: String) extends ApiResponse[Nothing]

trait Session {
  def authnCookies: Seq[HttpCookie]
}

object MarsApi extends AnyRef with LazyLogging {
  private implicit val scalaCache = ScalaCache(GuavaCache())
  private val baseUrl = "http://52.33.35.165:8080/api"

  private def POST(route: String) = Http(baseUrl + route).timeout(10000, 10000).method("POST")
  private def GET(route: String)  = Http(baseUrl + route).timeout(10000, 10000).method("GET")

  def login(username: String, password: String): Future[ApiResponse[Seq[HttpCookie]]] = Future {
    Try(POST("/session/login").auth(username, password).asString) match {
      case Success(response) =>
        response.code match {
          case 200  => scalacache.removeAll(); Ok(response.cookies)
          case code => logger.info(s"${response.code}-${response.body}"); Err(code, response.body)
        }
      case Failure(ex) => exceptionToError(ex)
    }
  }

  def accountInfo(implicit session: Session): Future[ApiResponse[Account]] =
    memoize(10.minutes)(Future(submit[Account](GET("/account"))))

  def assistantInfo(implicit session: Session): Future[ApiResponse[Assistant]] =
    memoize(10.minutes)(Future(submit[Assistant](GET("/assistant"))))

  private def submit[R: Reads](request: HttpRequest)(implicit session: Session): ApiResponse[R] = {
    Try(request.cookies(session.authnCookies).asString) match {
      case Success(response) =>
        response.code match {
          case 200  => Ok(Json.parse(response.body).as[R])
          case code => logger.info(s"${response.code}-${response.body}"); Err(code, response.body)
        }
      case Failure(ex)       => exceptionToError(ex)
    }
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

package com.uta.mars.api

import java.net.HttpCookie

import play.api.libs.json.Json

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import scalaj.http.{HttpResponse, Http}

sealed trait ApiResponse[+A]
case class Ok[A](data: A) extends ApiResponse[A]
case class Err(code: Int, msg: String) extends ApiResponse[Nothing]

trait Session {
  def authnCookies: Seq[HttpCookie]
}

object MarsApi {
  private val baseUrl = "http://52.33.35.165:8080/api"

  private def POST(route: String) = Http(baseUrl + route).method("POST")
  private def GET(route: String) = Http(baseUrl + route).method("GET")

  def login(username: String, password: String): Future[ApiResponse[Seq[HttpCookie]]] = Future {
    val response = POST("/session/login").auth(username, password).asString
    response.code match {
      case 200  => Ok(response.cookies)
      case 403  => Err(403, "Invalid username or password")
      case _    => defaultErrHandler(response)
    }
  }

  def accountInfo(implicit session: Session): Future[ApiResponse[Account]] = Future {
    val response = GET("/account").cookies(session.authnCookies).asString
    response.code match {
      case 200 => Ok(Json.parse(response.body).as[Account])
      case _   => defaultErrHandler(response)
    }
  }

  def assistantInfo(implicit session: Session): Future[ApiResponse[Assistant]] = Future {
    val response = GET("/assistant").cookies(session.authnCookies).asString
    response.code match {
      case 200 => Ok(Json.parse(response.body).as[Assistant])
      case _   => defaultErrHandler(response)
    }
  }

  private def defaultErrHandler(response: HttpResponse[String]): Err = {
    // todo: logging

    response.code match {
      case 400 => Err(400, "The request contains bad syntax or cannot be fulfilled.")
      case 401 => Err(403, "The supplied authentication is not authorized to access this resource.")
      case 403 => Err(403, "The supplied authentication is not authorized to access this resource.")
      case 404 => Err(404, "The requested resource that does not exist.")
      case 409 => Err(409, "The request could not be processed because of conflict in the request")
      case 410 => Err(410, "The resource requested is no longer available")
      case 500 => Err(500, "There was an internal server error. Please contact the server administrator.")
      case 503 => Err(503, "The server is currently unavailable (because it is overloaded or down for maintenance).")
      case _   => Err(499, "The application has encounter an unexpected error.")
    }
  }
}

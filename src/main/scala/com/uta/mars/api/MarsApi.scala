package com.uta.mars.api

import java.net.{ConnectException, HttpCookie, SocketTimeoutException}

import com.typesafe.scalalogging.LazyLogging
import org.scalactic._
import play.api.libs.json.{JsNull, JsValue, Json}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.duration._
import scala.util.{Failure, Success, Try}
import scalacache._
import scalacache.guava.GuavaCache
import scalaj.http.{Http, HttpRequest, HttpResponse, MultiPart}

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
            case 200 => Good(response.cookies)
            case code => logger.info(s"${response.code}-${response.body}"); Bad(Err(code, response.body))
          }
        case Failure(ex) => Bad(exceptionToError(ex))
      }
    }
    FutureOr(request)
  }

  def clearCache(): Future[Unit] = scalacache.removeAll()

  def accountInfo(ttl: Duration=20.minutes)(implicit sess: Session): FutureOr[Account, Err] =
    call(GET("/account"), ttl).map(_.as[Account])

  def assistantInfo(ttl: Duration=20.minutes)(implicit sess: Session): FutureOr[Assistant, Err] =
    call(GET("/assistant"), ttl).map(_.as[Assistant])

  def updateAssistant(rate: Double, dept: String, title: String, code: String)(implicit sess: Session): FutureOr[Unit, Err] = {
    scalacache.remove(s"GET-$baseUrl/assistant")
    call(POST("/assistant").postForm.params("rate" -> rate.toString, "dept" -> dept, "title" -> title, "titlecode" -> code)).map(_ => Unit)
  }

  def clockIn(compId: Option[String]=None)(implicit sess: Session): FutureOr[Unit, Err] =
    call(POST("/records/clock-in").postForm(Seq("computerid" -> compId.getOrElse("")))).map(_ => Unit)

  def clockOut(compId: Option[String]=None)(implicit sess: Session): FutureOr[Unit, Err] =
    call(POST("/records/clock-out").postForm(Seq("computerid" -> compId.getOrElse("")))).map(_ => Unit)

  def facialRecognition(img: Array[Byte])(implicit sess: Session): FutureOr[RecognitionResult, Err] =
    call(POST("/face/recognition").postMulti(MultiPart("img", "face.jpg", "image/jpg", img))).map(_.as[RecognitionResult])

  def addFaceForRecognition(img: Array[Byte])(implicit sess: Session): FutureOr[Unit, Err] =
    call(POST("/face").postMulti(MultiPart("img", "face.jpg", "image/jpg", img))).map(_ => Unit)

  def verifyUUID(uuid: String)(implicit sess: Session): FutureOr[Unit, Err] =
    call(GET(s"/register-uuid/verify/$uuid")).map(_ => Unit)

  def recordsFromThisPayPeriod(ttl: Duration=20.seconds)(implicit sess: Session): FutureOr[Records, Err] =
    call(GET("/records?filter=pay-period"), ttl).map(_.as[Records])

  def faceImages(ttl: Duration=1.hour)(implicit sess: Session) : FutureOr[FaceImages, Err] =
    call(GET("/face"), ttl).map(_.as[FaceImages])

  def emailTimeSheet()(implicit sess: Session): FutureOr[Unit, Err] = {
    import com.github.nscala_time.time.Imports._
    val today = LocalDate.now()
    val (y, m) = (today.getYear, today.getMonthOfYear)

    if (today.getDayOfMonth <= 15) call(GET(s"/time-sheet/first-half-month?year=$y&month=$m")).map(_ => Unit)
    else                           call(GET(s"/time-sheet/second-half-month?year=$y&month=$m")).map(_ => Unit)
  }

  def createAcc(user: String, passwd: String, asst: Assistant)(implicit sess: Session): FutureOr[Unit, Err] = {
    val request = POST("/account/assistant").postForm.params(
      "netid" -> asst.netId,      "user"  -> user,               "pass"      -> passwd,
      "email" -> asst.email,      "rate"  -> asst.rate.toString, "job"       -> asst.job,
      "dept"  -> asst.department, "first" -> asst.firstName,     "last"      -> asst.lastName,
      "empid" -> asst.employeeId, "title" -> asst.title,         "titlecode" -> asst.titleCode
    )
    call(request).map(_ => Unit)
  }

  /**
   * This method will check if the response to the request has been cached to avoid unnecessary trips over network.
   * If the the response is not cached, the request happens and the result is cached on status code 200 and ttl is > 0.
   */
  private def call(request: HttpRequest, ttl: Duration=0.milli)(implicit sess: Session): FutureOr[JsValue, Err] = {
    def doRequestCall(): Or[JsValue, Err] = {
      Try(request.cookies(sess.cookies).asString) match {
        case Success(HttpResponse(body, 200, _))  =>
          logger.info(s"Call successful: 200 -> $body")
          if (ttl.toMillis > 0) sync.cachingWithTTL(s"${request.method}-${request.url}")(ttl)(body)
          Good(Try(Json.parse(body)).getOrElse(JsNull))
        case Success(HttpResponse(body, code, _)) =>
          logger.info(s"Call failure: $code -> $body")
          Bad(Err(code, body))
        case Failure(ex) => Bad(exceptionToError(ex))
      }
    }

    FutureOr(
      scalacache.get[String](request.url).map {
        case Some(cacheHit) => Good(Json.parse(cacheHit)) // cache hit
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

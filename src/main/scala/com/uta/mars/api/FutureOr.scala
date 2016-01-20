package com.uta.mars.api

import org.scalactic._

import scala.concurrent.{ExecutionContext, Future}

final case class FutureOr[G, B](future: Future[Or[G, B]])(implicit ec: ExecutionContext) {

  def value: Future[Or[G, B]] = future

  def flatMap[H, C >: B](f: G => FutureOr[H, C]): FutureOr[H, C] = new FutureOr(
    future.flatMap {
      case Good(g) => f.apply(g).future
      case Bad(b) => Future.successful(Bad(b))
    }
  )

  def map[H](f: G => H): FutureOr[H, B] = new FutureOr(
    future.map {
      case Good(g) => Good(f.apply(g))
      case Bad(b) => Bad(b)
    }
  )

  def badMap[C](f: B => C): FutureOr[G, C] = new FutureOr(
    future.map {
      case Good(g) => Good(g)
      case Bad(b) => Bad(f.apply(b))
    }
  )

}

object FutureOr {
  def apply[G, B](or: Or[G, B])(implicit ec: ExecutionContext): FutureOr[G, B] = new FutureOr(Future(or))(ec)
}

package com.github.kmizu.arith

abstract class SComb[R] {
  val input: String
  def root: Parser[R]
  def isEOF(index: Int): Boolean = index >= input.length
  def current(index: Int): String = input.substring(index)
  def parse: ParseResult[R] = root(0)
  sealed abstract class ParseResult[+T] {
    def index: Int
  }
  object ParseResult {
    case class Success[+T](value: T, override val index: Int) extends ParseResult[T]
    case class Failure(message: String, override val index: Int) extends ParseResult[Nothing]
    case class Fatal(override val index: Int) extends ParseResult[Nothing]
  }

  type Parser[+T] = Int => ParseResult[T]

  def oneOf(seqs: Seq[Char]*): Parser[String] = index => {
    if(isEOF(index) || !seqs.exists(seq => seq.exists(ch => ch == current(index).charAt(0))))
      ParseResult.Failure(s"expected: ${seqs.mkString("[", ",", "]")}", index)
    else ParseResult.Success(current(index).substring(0, 1), index + 1)
  }

  def string(literal: String): Parser[String] = index => {
    if(isEOF(index)) {
      ParseResult.Failure(s"expected: ${literal} actual: EOF", index)
    } else if(current(index).startsWith(literal)) {
      ParseResult.Success(literal, index + literal.length)
    } else {
      ParseResult.Failure(s"expected: ${literal} actual: ${current(index).substring(0, literal.length)}", index)
    }
  }

  def branch[A](cases: (Char, Parser[A])*): Parser[A] = index => {
    def newFailureMessage(head: Char, cases: Map[Char, Parser[A]]): String = {
      val expectation = cases.map{ case (ch, _) => ch}.mkString("[", ",", "]")
      s"expect: ${expectation} actual: ${head}"
    }

    if(isEOF(index)) {
      val map: Map[Char, Parser[A]] = Map(cases :_*)
      ParseResult.Failure(newFailureMessage(0, map), index)
    } else {
      val head = current(index).charAt(0)
      val map = Map(cases:_*)
      map.get(head) match {
        case Some(clause) => clause(index)
        case None =>
          ParseResult.Failure(newFailureMessage(head, map), index)
      }
    }
  }

  implicit class RichParser[T](val self: Parser[T]) {
    def * : Parser[List[T]] = index => {
      def repeat(index: Int): (List[T], Int) = self(index) match {
        case ParseResult.Success(value, next1) =>
          val (result, next2) = repeat(next1)
          (value::result, next2)
        case ParseResult.Failure(message, next) =>
          (Nil, next)
      }
      val (result, next) = repeat(index)
      ParseResult.Success(result, next)
    }

    def ~[U](right: Parser[U]) : Parser[(T, U)] = index => {
      self(index) match {
        case ParseResult.Success(value1, next1) =>
          right(next1) match {
            case ParseResult.Success(value2, next2) =>
              ParseResult.Success((value1, value2), next2)
            case failure@ParseResult.Failure(_, _) =>
              failure
          }
        case failure@ParseResult.Failure(message, next) =>
          failure
      }
    }

    def chainl(q: Parser[(T, T) => T]): Parser[T] = {
      (self ~ (q ~ self).*).map { case (x, xs) =>
        xs.foldLeft(x) { case (a, (f, b)) =>
          f(a, b)
        }
      }
    }

    def |(right: Parser[T]): Parser[T] = index => {
      self(index) match {
        case success@ParseResult.Success(_, _) => success
        case ParseResult.Failure(_, _) => right(index)
      }
    }

    def filter(predicate: T => Boolean): Parser[T] = index => {
      self(index) match {
        case ParseResult.Success(value, next) =>
          if(predicate(value))
            ParseResult.Success(value, next)
          else
            ParseResult.Failure("not matched to predicate", index)
        case failure@ParseResult.Failure(_, _) =>
          failure
      }
    }

    def withFilter(predicate: T => Boolean): Parser[T] = filter(predicate)

    def map[U](function: T => U): Parser[U] = index => {
      self(index) match {
        case ParseResult.Success(value, next) => ParseResult.Success(function(value), next)
        case failure@ParseResult.Failure(_, _) => failure
      }
    }

    def flatMap[U](function: T => Parser[U]): Parser[U] = index => {
      self(index) match {
        case ParseResult.Success(value, next) =>
          function(value)(next)
        case failure@ParseResult.Failure(_, _) =>
          failure
      }
    }
  }
}


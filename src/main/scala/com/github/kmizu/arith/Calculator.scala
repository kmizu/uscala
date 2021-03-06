package com.github.kmizu.arith

import jdk.nashorn.internal.codegen.CompilerConstants.Call

class Calculator(override val input: String) extends SComb[Int] {
  def root: Parser[Int] = expression
  def expression: Parser[Int] = A
  def A: Parser[Int] = M.chainl {
    string("+").map{op => (lhs: Int, rhs: Int) => lhs + rhs} |
      string("-").map{op => (lhs: Int, rhs: Int) => lhs - rhs}
  }
  def M: Parser[Int] = P.chainl {
    string("*").map{op => (lhs: Int, rhs: Int) => lhs * rhs} |
      string("/").map{op => (lhs: Int, rhs: Int) => lhs / rhs}
  }
  def P: Parser[Int] = (for {
    _ <- string("("); e <- expression; _ <- string(")") } yield e) | number
  def number: Parser[Int] = oneOf('0'to'9').*.map{digits => digits.mkString.toInt}
}

object Calculator {
  def apply(input: String): Calculator = new Calculator(input)

  def main(args: Array[String]): Unit = {
    println(Calculator("1+2*3").parse)
    println(Calculator("1+5*3/4").parse)
    println(Calculator("(1+5)*3/2").parse)
    println(Calculator("(1-5)*3/2").parse)
  }
}

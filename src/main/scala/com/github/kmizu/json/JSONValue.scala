package com.github.kmizu.json

import scala.language.dynamics

object JSONValue {
  /**
    * Represent an integer value in NSON
    * @param value a Long value
    */
  final case class JLong(override val value: Long) extends JSONValue

  /**
    * Represent an floating point value in NSON
    * @param value a Double value
    */
  final case class JDouble(override val value: Double) extends JSONValue

  /**
    * Represent an floating point value in NSON
    * @param value a Boolean value
    */
  final case class JBoolean(override val value: Boolean) extends JSONValue

  /**
    * Represent an String value in NSON
    * @param value a String value
    */
  final case class JString(override val value: String) extends JSONValue

  /**
    * Represent null value in NSON
    */
  case object JNull extends JSONValue {
    override val value: Null = null
  }

  /**
    * Represent an array in NSON
    * @param content a Seq of NValue
    */
  final case class JArray(val content: Seq[JSONValue]) extends JSONValue {
    override val value: Seq[Any] = content.map{_.value}

    override def apply(index: Int): JSONValue = content(index)
  }

  /**
    * Represent an object in NSON
    * @param content a Map value which is key to value mappings
    */
  final case class JObject(val content: Map[String, JSONValue]) extends JSONValue {
    override val value: Map[String, Any] = content.map{ case (k, v) => (k, v.value)}

    override def apply(name: String): JSONValue = content(name)

    override def selectDynamic(name: String): JSONValue = content(name)
  }
  object JObject {
    def apply(): JObject = JObject(Map.empty[String, JSONValue])
    def apply(kvs: (String, JSONValue)*): JObject = {
      JObject(kvs.toMap)
    }
  }
}
sealed trait JSONValue extends Dynamic {
  /**
    * Extract a plain Scala value
    * @return a value corresponding to NValue
    */
  def value: Any


  /**
    * Fetch index-th element of NArray
    * @param index
    * @return index-th element of NArray
    * @throws UnsupportedOperationException if it's not a NArray
    */
  def apply(index: Int): JSONValue = throw new UnsupportedOperationException("JValue#apply(Int)")

  /**
    * Fetch a property of NObject
    * @param name
    * @return property name
    * @throws UnsupportedOperationException if it's not a NObject
    */
  def apply(name: String): JSONValue = throw new UnsupportedOperationException("JValue#apply(String)")

  /**
    * Fetch a property of NObject.
    * ```
    * obj.x // obj.apply("x")
    * ```
    * @param name
    * @return property name
    * @throws UnsupportedOperationException if it's not a NObject
    */
  def selectDynamic(name: String): JSONValue = throw new UnsupportedOperationException("JValue#selectDynamic(String)")
}

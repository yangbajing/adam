package test

import com.alibaba.fastjson.JSON

object Test extends App {

  val bytes = Array(10, 49, 48, 49, 57, 57, 53, 51, 54, 53, 54, 10).map(_.toByte)

  println(bytes)
  println(java.util.Arrays.toString(bytes))

  val json = JSON.parseObject[Int](bytes, classOf[Integer])
  //  val json: Int = JsonHelper.getInt(bytes)
  println(json)

}

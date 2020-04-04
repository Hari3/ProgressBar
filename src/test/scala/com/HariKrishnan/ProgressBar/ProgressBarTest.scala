package com.HariKrishnan.ProgressBar

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.{Assertion, BeforeAndAfter, PrivateMethodTester}

class ProgressBarTest extends AnyFlatSpec with PrivateMethodTester with BeforeAndAfter {

  var width: Int = jline.TerminalFactory.get().getWidth
  if(width == 0){
    //jline sometimes returns 0 on non un supported consoles...
    width = 100
  }
  val max = 100
  var pb: ProgressBar = _
  var generateProgressBar: PrivateMethod[String] = PrivateMethod[String](methodName = Symbol("generateProgressBar"))

  before {
    pb = new ProgressBar()
  }

  "Progress Bar" should "span the width of the screen" in {
    test
  }
  it should "scale when max is set" in {
    pb.setMax(270)
    test
  }
  it should "scale when prefix is set" in {
    pb.setPrefix("Example : ")
    test
  }
  it should "scale when precision is set" in {
    pb.setPrecision(0)
    test
  }
  it should "scale when width is set" in {
    width = 150
    pb.setWidth(width)
    test
  }

  "Progress" should "reflect the actual progress" in {
    for (i <- 0 until max) {
      val output = pb.invokePrivate(generateProgressBar())
      val progress = output.substring(0, output.indexOf('%')).toDouble
      assert(i == progress)
      pb.step()
    }
  }
  "A Progress Leak Exception" should "be thrown" in {
    for (i <- 1 to max) pb.step()
    assertThrows[ProgressLeakException](pb.step())
  }

  private def test: Assertion = {
    val output = pb.invokePrivate(generateProgressBar())
    assert(output.length == width)
  }


}

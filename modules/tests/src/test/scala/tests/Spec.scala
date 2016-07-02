package tests

import org.scalatest.{MustMatchers, WordSpec}
import play.api.Play
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.FakeApplication

class SpecMultiJvmNode1 extends WordSpec with MustMatchers {
  "A node" should {
    "be able to say hello" in {
      val playApp = new GuiceApplicationBuilder().build()
      Play.start(playApp)

      val message = "Hello from node 1"
      message must be("Hello from node 1")

      Play.stop(playApp)
    }
  }
}

class SpecMultiJvmNode2 extends WordSpec with MustMatchers {
  "A node" should {
    "be able to say hello" in {

      val message = "Hello from node 2"
      message must be("Hello from node 2")

    }
  }
}

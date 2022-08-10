package jp.bucketeer.sdk

import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify

class MockitoKotlinExample {
  @Test
  fun doAction_doesSomething() {
    /* Given */
    val mock = mock<MyInterface> {
      on { doSomething(any()) } doReturn "text"
    }

    /* When */
    MyClass(mock).doAction()

    /* Then */
    verify(mock).doSomething(any())
    verify(mock, never()).nop()
  }

  interface MyInterface {
    fun doSomething(s: String): String
    fun nop()
  }

  class MyClass(private val myInterface: MyInterface) {
    fun doAction() {
      myInterface.doSomething("test")
    }
  }
}

package jp.bucketeer.sdk

import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.never
import com.nhaarman.mockito_kotlin.verify
import org.junit.Test

class MockitoKotlinExample {
  @Test fun doAction_doesSomething() {
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

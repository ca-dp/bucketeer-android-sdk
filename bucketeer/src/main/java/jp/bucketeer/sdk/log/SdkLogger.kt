package jp.bucketeer.sdk.log

import android.util.Log

internal object SdkLogger {
  private val logHandlers: MutableList<LogHandler> = mutableListOf()

  fun addLogger(logHandler: LogHandler) {
    logHandlers.add(logHandler)
  }

  fun log(
      priority: Int,
      msgCreator: (() -> String?)? = null,
      throwable: Throwable? = null,
      isLogForUser: Boolean = true
  ) {
    logHandlers.forEach {
      it.log(priority, msgCreator, throwable, isLogForUser)
    }
  }
}

// TODO: Add msgCreator log methods
fun logd(
    throwable: Throwable? = null,
    isLogForUser: Boolean = true,
    msgCreator: (() -> String?)? = null
) {
  SdkLogger.log(
      Log.DEBUG,
      msgCreator = msgCreator,
      throwable = throwable,
      isLogForUser = isLogForUser
  )
}

fun loge(
    throwable: Throwable? = null,
    isLogForUser: Boolean = true,
    msgCreator: (() -> String?)? = null
) {
  SdkLogger.log(
      priority = Log.ERROR,
      msgCreator = msgCreator,
      throwable = throwable,
      isLogForUser = isLogForUser
  )
}

fun logi(
    throwable: Throwable? = null,
    isLogForUser: Boolean = true,
    msgCreator: (() -> String?)? = null
) {
  SdkLogger.log(priority = Log.INFO,
      msgCreator = msgCreator,
      throwable = throwable,
      isLogForUser = isLogForUser
  )
}

fun logv(
    throwable: Throwable? = null,
    isLogForUser: Boolean = true,
    msgCreator: (() -> String?)? = null
) {
  SdkLogger.log(
      priority = Log.VERBOSE,
      msgCreator = msgCreator,
      throwable = throwable,
      isLogForUser = isLogForUser
  )
}

fun logw(
    throwable: Throwable? = null,
    isLogForUser: Boolean = true,
    msgCreator: (() -> String?)? = null
) {
  SdkLogger.log(
      priority = Log.WARN,
      msgCreator = msgCreator,
      throwable = throwable,
      isLogForUser = isLogForUser
  )
}

fun logwtf(
    throwable: Throwable? = null,
    isLogForUser: Boolean = true,
    msgCreator: (() -> String?)? = null
) {
  SdkLogger.log(
      priority = Log.ASSERT,
      msgCreator = msgCreator,
      throwable = throwable,
      isLogForUser = isLogForUser
  )
}

internal abstract class LogHandler {
  abstract fun log(
      priority: Int,
      msgCreator: (() -> String?)? = null,
      th: Throwable?,
      isLogForUser: Boolean = true
  )
}

internal class SdkInsideLogHandler(private val tag: String) : LogHandler() {
  override fun log(
      priority: Int,
      msgCreator: (() -> String?)?,
      th: Throwable?,
      isLogForUser: Boolean
  ) {
    if (!Log.isLoggable(tag, priority)) return

    val message = buildString {
      msgCreator?.invoke()?.let { append(it) }
      if (th != null) append("\n")
      if (th != null) append(Log.getStackTraceString(th))
    }
    if (message.isBlank()) return

    Log.println(priority, tag, message)
  }
}

internal class UserLogHandler(private val tag: String) : LogHandler() {
  override fun log(
      priority: Int,
      msgCreator: (() -> String?)?,
      th: Throwable?,
      isLogForUser: Boolean
  ) {
    if (!isLogForUser) return

    if (!Log.isLoggable(tag, priority)) return

    val message = buildString {
      msgCreator?.invoke()?.let { append(it) }
      if (th != null) append("\n")
      if (th != null) append(Log.getStackTraceString(th))
    }
    if (message.isBlank()) return

    Log.println(priority, tag, message)
  }
}

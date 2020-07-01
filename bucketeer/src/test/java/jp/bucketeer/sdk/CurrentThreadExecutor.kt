package jp.bucketeer.sdk

import java.util.concurrent.Executor

object CurrentThreadExecutor : Executor {
  override fun execute(r: Runnable) {
    r.run()
  }
}

package jp.bucketeer.sdk.util

open class SingletonHolder<out T : Any, in Application, in BKTConfig, in BKTUser>(
  private val constructor: (Application, BKTConfig, BKTUser) -> T
) {

  @Volatile
  private var instance: T? = null

  fun initialize(application: Application, config: BKTConfig, user: BKTUser): T {
    return when {
      instance != null -> instance!!
      else -> synchronized(this) {
        instance = constructor(application, config, user)
        instance!!
      }
    }
  }

  fun destroy() {
    synchronized(this) {
      instance = null
    }
  }
}

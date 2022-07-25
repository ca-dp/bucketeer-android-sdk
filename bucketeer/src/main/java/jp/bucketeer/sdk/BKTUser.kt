package jp.bucketeer.sdk

import bucketeer.user.UserOuterClass

data class BKTUser private constructor(
  val id: String,
  var attributes: Map<String, String>
) {
  class Builder {
    private lateinit var id: String
    private var attributes: Map<String, String> = mutableMapOf()

    fun id(id: String): Builder {
      this.id = id
      return this
    }

    fun customAttributes(attributes: Map<String, String>): Builder {
      this.attributes = attributes
      return this
    }

    fun build(): BKTUser {
      if (id.isEmpty()) {
        throw BucketeerException.IllegalArgumentException(
          "The user id is required."
        )
      }
      return BKTUser(
        id,
        attributes
      )
    }
  }

  fun customAttributes(attributes: Map<String, String>) {
    this.attributes = attributes
  }

  internal fun userOf(): UserOuterClass.User {
    return UserOuterClass.User.newBuilder().setId(id).putAllData(attributes).build()
  }
}

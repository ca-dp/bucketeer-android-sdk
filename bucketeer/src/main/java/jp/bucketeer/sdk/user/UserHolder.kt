package jp.bucketeer.sdk.user

import bucketeer.user.UserOuterClass

internal open class UserHolder {
  private val defaultUser = UserOuterClass
    .User
    .newBuilder()
    .build()
  var user: UserOuterClass.User = defaultUser
    protected set

  val hasUser: Boolean get() = this.user != defaultUser

  val userId: String
    get() = this.user.id

  class UpdatableUserHolder : UserHolder() {
    fun updateUser(user: UserOuterClass.User) {
      this.user = user
    }
  }
}

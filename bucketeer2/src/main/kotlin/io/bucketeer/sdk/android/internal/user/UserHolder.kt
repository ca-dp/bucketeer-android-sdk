package io.bucketeer.sdk.android.internal.user

import io.bucketeer.sdk.android.BKTUser
import io.bucketeer.sdk.android.internal.model.User

class UserHolder(
  private var user: User,
) {
  val userId: String = user.id

  fun get(): User = user

  fun update(user: User) {
    this.user = user
  }

  fun update(updater: (user: User) -> User) {
    this.user = updater(user)
  }
}

internal fun BKTUser.toUser(): User {
  return User(
    id = this.id,
    data = this.attributes,
  )
}

internal fun User.toBKTUser(): BKTUser {
  return BKTUser(
    id = this.id,
    attributes = this.data,
  )
}

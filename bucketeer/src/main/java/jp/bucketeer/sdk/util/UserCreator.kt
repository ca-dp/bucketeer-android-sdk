@file:Suppress("ktlint:filename")

package jp.bucketeer.sdk.util

import bucketeer.user.UserOuterClass

internal fun userOf(userId: String, userData: Map<String, String>): UserOuterClass.User {
  return UserOuterClass.User.newBuilder().setId(userId).putAllData(userData).build()
}

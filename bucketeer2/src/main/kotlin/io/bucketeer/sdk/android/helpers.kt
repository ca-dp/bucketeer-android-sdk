@file:Suppress("ktlint:filename")

package io.bucketeer.sdk.android

import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

@OptIn(ExperimentalContracts::class)
internal inline fun require(value: Boolean, lazyMessage: () -> Any) {
  contract {
    returns() implies value
  }
  if (!value) {
    val message = lazyMessage()
    throw BKTException.IllegalArgumentException(message.toString())
  }
}

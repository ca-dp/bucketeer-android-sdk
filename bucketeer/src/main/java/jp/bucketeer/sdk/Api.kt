package jp.bucketeer.sdk

import bucketeer.event.client.EventOuterClass
import bucketeer.gateway.Service
import bucketeer.user.UserOuterClass

interface Api {
  fun fetchEvaluation(
    user: UserOuterClass.User,
    userEvaluationsId: String
  ): Result<Service.GetEvaluationsResponse>

  fun registerEvent(
    events: List<EventOuterClass.Event>
  ): Result<Service.RegisterEventsResponse>

  fun setFetchEvaluationApiCallback(f: FetchEvaluationsApiCallback)

  sealed class Result<T> {
    data class Success<T>(val value: T) : Result<T>()
    data class Fail<T>(val e: BucketeerException) : Result<T>()
  }
}

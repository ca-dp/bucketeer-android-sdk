package jp.bucketeer.sdk

import android.app.Application

/**
 * Created by Alessandro Yuichi Okimoto on 2022/07/25.
 */
class Sample {

 fun init(application: Application) {
   val config = BKTConfig.Builder()
     .apiKey("")
     .apiURL("")
     .featureTag("")
     // Optionals
     .pollingInterval(600)
     .backgroundPollingInterval(2400)
     .debugMode(true)
     .build()

   val user = BKTUser.Builder()
     .id("user-id")
     .customAttributes(mapOf())
     .build()

   val client = BKTClient.initialize(application, config, user)

   client.booleanVariation("feature-id", false)

   client.flush()

   BKTClient.destroy()
 }
}

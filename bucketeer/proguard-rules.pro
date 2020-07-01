# https://stackoverflow.com/questions/8540905/proguard-and-android
-dontwarn javax.naming.**
# https://stackoverflow.com/questions/9120338/proguard-configuration-for-guava-with-obfuscation-and-optimization/47680287#47680287
-dontwarn javax.lang.model.element.Modifier
-dontwarn io.grpc.protobuf.StatusProto

# for toString debug
-keepclassmembers class * extends com.google.protobuf.GeneratedMessageLite {
  *** get*();
  *** set*(***);
  *** has*();
}

# gRPC
-keepclassmembers class io.grpc.okhttp.OkHttpChannelBuilder {
  io.grpc.okhttp.OkHttpChannelBuilder forTarget(java.lang.String);
  io.grpc.okhttp.OkHttpChannelBuilder scheduledExecutorService(java.util.concurrent.ScheduledExecutorService);
  io.grpc.okhttp.OkHttpChannelBuilder sslSocketFactory(javax.net.ssl.SSLSocketFactory);
  io.grpc.okhttp.OkHttpChannelBuilder transportExecutor(java.util.concurrent.Executor);
}

-keep public class jp.bucketeer.sdk.Bucketeer { *; }
-keep public class jp.bucketeer.sdk.Bucketeer$* { *; }
-keep public class jp.bucketeer.sdk.BucketeerConfig { *; }
-keep public class jp.bucketeer.sdk.BucketeerConfig$* { *; }
-keep public class jp.bucketeer.sdk.BucketeerUser { *; }
-keep public class jp.bucketeer.sdk.BucketeerVariation { *; }
-keep public class jp.bucketeer.sdk.BucketeerException { *; }
-keep public class jp.bucketeer.sdk.BucketeerException$* { *; }
-keep public class jp.bucketeer.sdk.Evaluation { *; }
-keep public class jp.bucketeer.sdk.User { *; }

# Only keep inner class names for Kotlin IntelliJ Plugin
-keepnames class jp.bucketeer.sdk.**$Companion {}
-keepnames class jp.bucketeer.sdk.user.UserHolder$UpdatableUserHolder {}
-keepnames class jp.bucketeer.sdk.evaluation.dto.RefreshManuallyStateChangedAction$*$* {}
-keepnames class jp.bucketeer.sdk.Api$*$* {}

-keep public class com.google.protobuf.DescriptorProtos$* { *; }
-keep public class com.google.protobuf.Any { *; }

-dontwarn com.google.protobuf.**
-dontwarn com.google.j2objc.annotations.*

# Configuration for Guava 18.0
#
# disagrees with instructions provided by Guava project: https://code.google.com/p/guava-libraries/wiki/UsingProGuardWithGuava

-keep class com.google.common.io.Resources {
    public static <methods>;
}
-keep class com.google.common.collect.Lists {
    public static ** reverse(**);
}
-keep class com.google.common.base.Charsets {
    public static <fields>;
}

-keep class com.google.common.base.Joiner {
    public static com.google.common.base.Joiner on(java.lang.String);
    public ** join(...);
}

-keep class com.google.common.collect.MapMakerInternalMap$ReferenceEntry
-keep class com.google.common.cache.LocalCache$ReferenceEntry

# http://stackoverflow.com/questions/9120338/proguard-configuration-for-guava-with-obfuscation-and-optimization
-dontwarn javax.annotation.**
-dontwarn javax.inject.**
-dontwarn sun.misc.Unsafe

# Guava 19.0
-dontwarn java.lang.ClassValue
-dontwarn com.google.j2objc.annotations.Weak
-dontwarn org.codehaus.mojo.animal_sniffer.IgnoreJRERequirement

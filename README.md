[![Build Status](https://app.bitrise.io/app/16337d5c0c8d6081/status.svg?token=l2E3TXU8-dnAmep6MJ8cIA&branch=master)](https://app.bitrise.io/app/16337d5c0c8d6081)

# Bucketeer SDK for Android

## Setup

Install prerequisite tools.

- Android Studio
- Java

Then, you need to create `local.properties`.

```
# build
sdk.dir=<SDK_DIR_PATH> # e.g. /Users/<USER_NAME>/Library/Android/sdk

# test
api_key=<API_KEY>
api_url=<API_URL> # e.g. api-uat.bucketeer.jp

# sample
sample.use_released_sdk=false
sample.sdk_version=<SDK_VERSION>

# publish (if needed)
# sdk.version=<SDK_VERSION>
# bintray.user=<BINTRAY_USER>
# bintray.api_key=<BINTRAY_API_KEY>
```

## Development

### Development with Android Studio

Open Android Studio and import `bucketeer-android-sdk`.

### Development with command line

#### project :bucketeer (SDK)

Displays the tasks runnable from project ':bucketeer'.

```
./gradlew :bucketeer:tasks
```

Runs lint on the Debug build.

```
./gradlew :bucketeer:lintDebug
```

Run unit tests for the debug build.

```
./gradlew :bucketeer:testDebugUnitTest
```

Deletes the build directory and assembles all Release builds. (Create `./bucketeer/build/outputs/aar/bucketeer-release.aar`)

```
./gradlew clean :bucketeer:assembleRelease
```

Installs and runs the integration tests for debug on connected devices.
Open Android Emulator, then run the command below.

```
./gradlew :bucketeer:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.package=jp.bucketeer.sdk.test.integration
```

Installs and runs the e2e tests for debug on connected devices.
Open Android Emulator, then run the command below.

```
./gradlew :bucketeer:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.package=jp.bucketeer.sdk.test.e2e
```

Publishes SDK to JFrog Bintray and jcenter.

```
./gradlew :bucketeer:bintrayUpload
```

#### project :sample (Sample)

Displays the tasks runnable from project ':sample'.

```
./gradlew :sample:tasks
```

Deletes the build directory and assembles all Release builds. (Create `./sample/build/outputs/apk/sample-release.apk`)

```
./gradlew clean :sample:assembleRelease
```

## SDK User Docs

- [Tutorial](https://bucketeer.io/docs/#/sdk-tutorial-android)
- [Integration](https://bucketeer.io/docs/#/sdk-reference-guides-android)

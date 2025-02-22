# Firebase Vertex AI SDK

For developer documentation, please visit https://firebase.google.com/docs/vertex-ai.
This README is for contributors building and running tests for the SDK.

## Building

All Gradle commands should be run from the root of this repository.

`./gradlew :firebase-vertexai:publishToMavenLocal`

## Running Tests

> [!IMPORTANT]
> These unit tests require mock response files, which can be downloaded by running
`./firebase-vertexai/update_responses.sh` from the root of this repository.

Unit tests:

`./gradlew :firebase-vertexai:check`

Integration tests, requiring a running and connected device (emulator or real):

`./gradlew :firebase-vertexai:deviceCheck`

## Code Formatting

Format Kotlin code in this SDK in Android Studio using
the [spotless plugin]([https://plugins.jetbrains.com/plugin/14912-ktfmt](https://github.com/diffplug/spotless)
by running:

`./gradlew firebase-vertexai:spotlessApply`

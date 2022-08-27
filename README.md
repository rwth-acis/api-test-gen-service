# API Test Generation Service

![Java CI with Gradle](https://github.com/rwth-acis/api-test-gen-service/workflows/Java%20CI%20with%20Gradle/badge.svg?branch=main)
[![codecov](https://codecov.io/gh/rwth-acis/api-test-gen-service/branch/main/graph/badge.svg?token=Q6QZPKHH77)](https://codecov.io/gh/rwth-acis/api-test-gen-service)

This service generates API test cases based on a given OpenAPI documentation (v2 or v3).
The test cases are represented using the [las2peer API Test Model](https://github.com/rwth-acis/las2peer-api-test-model).

## ⚡️ Quick start

The easiest way to use the service is to build (or pull) the Docker image and run the service as a container.
The following methods are provided by the service and can be called via las2peer RMI:

### `String openAPIToTests(String docs)`

Generates test cases using the given OpenAPI (v2) documentation.
Returns a JSONArray (as String) containing the generated test cases and their descriptions.

### `String openAPIV3ToTests(String docs)`

Generates test cases using the given OpenAPI (v3) documentation.
Returns a JSONArray (as String) containing the generated test cases and their descriptions.

### `String openAPIDiffToTest(String docsOld, String docsUpdated)`

Tries to generate a test case for an operation that was added to the OpenAPI doc or that was updated.
Returns a JSONObject (as String) containing the generated test case and its description, if a test case could be generated.

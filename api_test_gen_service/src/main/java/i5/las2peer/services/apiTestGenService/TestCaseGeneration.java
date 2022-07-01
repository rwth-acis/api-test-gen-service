package i5.las2peer.services.apiTestGenService;

import i5.las2peer.apiTestModel.RequestAssertion;
import i5.las2peer.apiTestModel.StatusCodeAssertion;
import i5.las2peer.apiTestModel.TestCase;
import i5.las2peer.apiTestModel.TestRequest;
import io.swagger.models.HttpMethod;
import io.swagger.models.Model;
import io.swagger.models.Operation;
import io.swagger.models.Swagger;
import io.swagger.models.parameters.BodyParameter;
import io.swagger.models.parameters.Parameter;
import org.json.simple.JSONObject;

import java.util.*;

public class TestCaseGeneration {

    /**
     * Generates test cases for given OpenAPI/Swagger operation.
     *
     * @param swagger   Swagger object
     * @param method    HttpMethod
     * @param operation Operation for which test cases should be generated.
     * @param path      Path
     * @return Map containing generated test cases as keys and descriptions as values.
     */
    public static Map<TestCase, String> openAPIOperationToTests(Swagger swagger, HttpMethod method, Operation operation, String path) {
        Map<TestCase, String> testCases = new HashMap<>();

        addTestCaseIfNotNull(testCases, generateSimpleGETTest(method, operation, path));
        addTestCaseIfNotNull(testCases, generateMissingBodyPropTest(swagger, method, operation, path));
        addTestCaseIfNotNull(testCases, generateResourceNotFoundTest(method, operation, path));

        return testCases;
    }


    /**
     * If the operation requires path parameters, this method generates a test case that should use path parameters
     * for which no resource can be found and asserts on status code 404.
     *
     * @param method    HttpMethod
     * @param operation Operation for which a test case should be generated.
     * @param path      Path
     * @return Map entry with TestCase object as key and description as value if test could be generated, null otherwise.
     */
    private static Map.Entry<TestCase, String> generateResourceNotFoundTest(HttpMethod method, Operation operation, String path) {
        List<Parameter> pathParams = operation.getParameters().stream().filter(param -> param.getIn().equals("path")).toList();
        if (pathParams.size() > 0) {
            // check if 404 response is possible
            if (operation.getResponses().keySet().contains("404")) {
                // create test that uses path parameter value for which no resource can be found
                JSONObject pathParamsJSON = new JSONObject();
                for (Parameter pathParam : pathParams) {
                    pathParamsJSON.put(pathParam.getName(), "");
                }

                StatusCodeAssertion assertion = new StatusCodeAssertion(0, 404);
                TestRequest request = new TestRequest(method.name(), path, pathParamsJSON, -1, "", Arrays.asList(assertion));
                TestCase generatedTestCase = createTestCase(method.name() + " " + path + " not found test", request);

                String description = "";
                return Map.entry(generatedTestCase, description);
            }
        }
        return null;
    }

    /**
     * If the operation requires a body and there is a schema defined for it, this method generates a test case
     * that intentionally violates this schema and asserts on status code 400.
     *
     * @param swagger   Swagger object used to search for definitions.
     * @param method    HttpMethod
     * @param operation Operation for which a test case should be generated.
     * @param path      Path
     * @return Map entry with TestCase object as key and description as value if test could be generated, null otherwise.
     */
    private static Map.Entry<TestCase, String> generateMissingBodyPropTest(Swagger swagger, HttpMethod method, Operation operation, String path) {
        // independent of HTTP method
        Parameter parameter = operation.getParameters().stream().filter(param -> param.getIn().equals("body") && param.getRequired()).findFirst().orElse(null);
        if (parameter != null) {
            // requires body
            BodyParameter bodyParameter = (BodyParameter) parameter;
            if (bodyParameter.getSchema() != null) {
                String ref = bodyParameter.getSchema().getReference();
                for (Map.Entry<String, Model> entry : swagger.getDefinitions().entrySet()) {
                    String schemaName = entry.getKey();
                    if (ref.split("#/definitions/")[1].equals(schemaName)) {
                        Model model = entry.getValue();
                        if (model.getProperties().keySet().size() > 0) {
                            // definition contains at least one property
                            // create test that sends empty JSONObject as body
                            StatusCodeAssertion assertion = new StatusCodeAssertion(0, 400);
                            TestRequest request = createTestRequest(method.name(), path, "{}", assertion);
                            TestCase generatedTestCase = createTestCase(method.name() + " " + path + " bad request (missing body property)", request);

                            String description = "The method " + method.name() + " " + path + " requires a body following the " + schemaName + " schema. Intentionally violating it should return status code 400 (Bad request).";
                            return Map.entry(generatedTestCase, description);
                        }
                    }
                }
            }
        }
        return null;
    }

    /**
     * If the operation has no parameters and response status code 200 is allowed,
     * this method generates a test case that sends a request to the given path and
     * asserts on status code 200.
     *
     * @param method    HttpMethod; if not GET then no test case will be returned.
     * @param operation Operation for which a test case should be generated.
     * @param path      Path
     * @return Map entry with TestCase object as key and description as value if simple
     * GET test could be generated, null otherwise.
     */
    private static Map.Entry<TestCase, String> generateSimpleGETTest(HttpMethod method, Operation operation, String path) {
        if (method.equals(HttpMethod.GET)) {
            // check if operation has no parameters
            if (operation.getParameters().size() == 0) {
                // no parameters => we can easily perform a request
                if (operation.getResponses().keySet().contains("200")) {
                    StatusCodeAssertion assertion = new StatusCodeAssertion(0, 200);
                    TestRequest request = createTestRequest("GET", path, assertion);
                    TestCase generatedTestCase = createTestCase("Simple GET " + path + " test", request);

                    String description = "The method GET " + path + " has no parameters and therefore all requests should return status code 200 (OK).";
                    return Map.entry(generatedTestCase, description);
                }
            }
        }
        return null;
    }

    /**
     * Creates a test case with the given name that only contains the given request.
     *
     * @param name    Name of the test case.
     * @param request Request that should be included in the test case.
     * @return TestCase object.
     */
    private static TestCase createTestCase(String name, TestRequest request) {
        return new TestCase(name, Arrays.asList(request));
    }

    /**
     * Creates a test request with given type and path that only contains the given assertion.
     *
     * @param type      Type, e.g., GET.
     * @param path      Request path.
     * @param assertion Assertion that should be included.
     * @return TestRequest object.
     */
    private static TestRequest createTestRequest(String type, String path, RequestAssertion assertion) {
        return new TestRequest(type, path, Arrays.asList(assertion));
    }

    /**
     * Creates a test request with given type, path and body that only contains the given assertion.
     *
     * @param type      Type, e.g., GET.
     * @param path      Request path.
     * @param body      Request body.
     * @param assertion Assertion that should be included.
     * @return TestRequest object.
     */
    private static TestRequest createTestRequest(String type, String path, String body, RequestAssertion assertion) {
        return new TestRequest(type, path, new JSONObject(), -1, body, Arrays.asList(assertion));
    }

    private static void addTestCaseIfNotNull(Map<TestCase, String> testCases, Map.Entry<TestCase, String> testCase) {
        if (testCase != null) testCases.put(testCase.getKey(), testCase.getValue());
    }
}

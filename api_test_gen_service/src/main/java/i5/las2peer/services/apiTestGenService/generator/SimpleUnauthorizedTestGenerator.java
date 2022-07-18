package i5.las2peer.services.apiTestGenService.generator;

import i5.las2peer.apiTestModel.StatusCodeAssertion;
import i5.las2peer.apiTestModel.TestCase;
import i5.las2peer.apiTestModel.TestRequest;
import io.swagger.models.HttpMethod;
import io.swagger.models.Operation;
import io.swagger.models.Swagger;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.PathItem;

import java.util.Map;

import static i5.las2peer.services.apiTestGenService.generator.GenerationHelper.*;

public class SimpleUnauthorizedTestGenerator implements TestCaseGenerator {

    /**
     * If the operation allows response status code 401 or 403, a User or Service Agent might be required to
     * call the method. Then, a test case is generated that uses the Anonymous Agent and calls the method, then expects
     * status code 401 or 403.
     *
     * @param method    HttpMethod
     * @param operation Operation for which a test case should be generated.
     * @param path      Path
     * @return Map entry with TestCase object as key and description as value if test could be generated, null otherwise.
     */
    @Override
    public Map.Entry<TestCase, String> generateTestCase(Swagger swagger, HttpMethod method, Operation operation, String path) {
        // check if status code 401 or 403 are possible
        if (operation.getResponses().keySet().stream().anyMatch(statusCode -> statusCode.equals("401") || statusCode.equals("403"))) {
            int statusCode = operation.getResponses().keySet().stream().anyMatch(s -> s.equals("401")) ? 401 : 403;
            StatusCodeAssertion assertion = new StatusCodeAssertion(0, statusCode);
            TestRequest request = createTestRequest(method.name(), path, 0, assertion); // 0 for anonymous agent
            setEmptyPathParameters(request, operation);

            return buildTestCase(method.name(), path, request, statusCode);
        }
        return null;
    }

    /**
     * If the operation allows response status code 401 or 403, a User or Service Agent might be required to
     * call the method. Then, a test case is generated that uses the Anonymous Agent and calls the method, then expects
     * status code 401 or 403.
     *
     * @param method    HttpMethod
     * @param operation Operation for which a test case should be generated.
     * @param path      Path
     * @return Map entry with TestCase object as key and description as value if test could be generated, null otherwise.
     */
    @Override
    public Map.Entry<TestCase, String> generateTestCaseV3(OpenAPI openAPI, PathItem.HttpMethod method,
                                                        io.swagger.v3.oas.models.Operation operation, String path) {
        // check if status code 401 or 403 are possible
        if (operation.getResponses().keySet().stream().anyMatch(statusCode -> statusCode.equals("401") || statusCode.equals("403"))) {
            int statusCode = operation.getResponses().keySet().stream().anyMatch(s -> s.equals("401")) ? 401 : 403;
            StatusCodeAssertion assertion = new StatusCodeAssertion(0, statusCode);
            TestRequest request = createTestRequest(method.name(), path, 0, assertion); // 0 for anonymous agent
            setEmptyPathParameters(request, operation);

            return buildTestCase(method.name(), path, request, statusCode);
        }
        return null;
    }

    private Map.Entry<TestCase, String> buildTestCase(String methodName, String path, TestRequest request, int statusCode) {
        TestCase generatedTestCase = createTestCase("Unauthorized test for " + methodName + " " + path, request);

        String description = "The method " + methodName + " " + path
                + " might require a las2peer User- or Service-Agent. Therefore, a request using the Anonymous Agent"
                + " should result in status code " + statusCode + ". All parameters (if there are any) can be set to arbitrary values.";
        return Map.entry(generatedTestCase, description);
    }
}

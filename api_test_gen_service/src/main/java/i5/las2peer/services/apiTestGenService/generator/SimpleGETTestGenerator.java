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

public class SimpleGETTestGenerator implements TestCaseGenerator {

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
    @Override
    public Map.Entry<TestCase, String> generateTestCase(Swagger swagger, HttpMethod method, Operation operation, String path) {
        if(!operation.getResponses().keySet().contains("200")) return null;

        if (method.equals(HttpMethod.GET) && hasNoParameters(operation)) {
            // no parameters => we can easily perform a request
            return buildTestCase(path);
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
    @Override
    public Map.Entry<TestCase, String> generateTestCaseV3(OpenAPI openAPI, PathItem.HttpMethod method, io.swagger.v3.oas.models.Operation operation, String path) {
        if(!operation.getResponses().keySet().contains("200")) return null;

        if (method.equals(PathItem.HttpMethod.GET) && hasNoParameters(operation)) {
            // no parameters => we can easily perform a request
            return buildTestCase(path);
        }
        return null;
    }

    private Map.Entry<TestCase, String> buildTestCase(String path) {
        StatusCodeAssertion assertion = new StatusCodeAssertion(0, 200);
        TestRequest request = createTestRequest("GET", path, assertion);
        TestCase generatedTestCase = createTestCase("Simple GET " + path + " test", request);

        String description = "The method GET " + path + " has no parameters and therefore all requests should return status code 200 (OK).";
        return Map.entry(generatedTestCase, description);
    }
}

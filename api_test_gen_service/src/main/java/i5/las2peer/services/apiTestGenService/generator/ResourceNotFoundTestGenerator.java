package i5.las2peer.services.apiTestGenService.generator;

import i5.las2peer.apiTestModel.StatusCodeAssertion;
import i5.las2peer.apiTestModel.TestCase;
import i5.las2peer.apiTestModel.TestRequest;
import io.swagger.models.HttpMethod;
import io.swagger.models.Operation;
import io.swagger.models.Swagger;
import io.swagger.models.parameters.Parameter;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.PathItem;
import org.json.simple.JSONObject;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static i5.las2peer.services.apiTestGenService.generator.GenerationHelper.*;

public class ResourceNotFoundTestGenerator implements TestCaseGenerator {

    /**
     * If the operation requires path parameters, this method generates a test case that should use path parameters
     * for which no resource can be found and asserts on status code 404.
     *
     * @param method    HttpMethod
     * @param operation Operation for which a test case should be generated.
     * @param path      Path
     * @return Map entry with TestCase object as key and description as value if test could be generated, null otherwise.
     */
    @Override
    public Map.Entry<TestCase, String> generateTestCase(Swagger swagger, HttpMethod method, Operation operation, String path) {
        List<Parameter> pathParams = getOperationPathParams(operation);
        // check if there are path params and 404 response is possible
        if (pathParams.size() > 0 && statusCodeAllowed(operation, 404)) {
            // create test that uses path parameter value for which no resource can be found
            StatusCodeAssertion assertion = new StatusCodeAssertion(0, 404);
            TestRequest request = new TestRequest(method.name(), path, new JSONObject(), -1, "", Arrays.asList(assertion));
            setEmptyPathParameters(request, operation);
            return buildTestCase(method.name(), path, request);
        }
        return null;
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
    @Override
    public Map.Entry<TestCase, String> generateTestCaseV3(OpenAPI openAPI, PathItem.HttpMethod method,
                                                          io.swagger.v3.oas.models.Operation operation, String path) {
        List<io.swagger.v3.oas.models.parameters.Parameter> pathParams = getOperationPathParams(operation);
        // check if there are path params and 404 response is possible
        if (pathParams.size() > 0 && statusCodeAllowed(operation, 404)) {
            // create test that uses path parameter value for which no resource can be found
            StatusCodeAssertion assertion = new StatusCodeAssertion(0, 404);
            TestRequest request = new TestRequest(method.name(), path, new JSONObject(), -1, "", Arrays.asList(assertion));
            setEmptyPathParameters(request, operation);
            return buildTestCase(method.name(), path, request);
        }
        return null;
    }

    private Map.Entry<TestCase, String> buildTestCase(String methodName, String path, TestRequest request) {
        TestCase generatedTestCase = createTestCase(methodName + " " + path + " not found test", request);

        String description = "The method " + methodName + " " + path +
                " requires a path parameter. Choosing a value for it, so that no resource can be found, should lead to status code 404 (Not found).";
        return Map.entry(generatedTestCase, description);
    }
}

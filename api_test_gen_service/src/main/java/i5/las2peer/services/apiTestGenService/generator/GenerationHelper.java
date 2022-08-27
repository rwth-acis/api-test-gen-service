package i5.las2peer.services.apiTestGenService.generator;

import i5.las2peer.apiTestModel.RequestAssertion;
import i5.las2peer.apiTestModel.TestCase;
import i5.las2peer.apiTestModel.TestRequest;
import io.swagger.models.Model;
import io.swagger.models.Operation;
import io.swagger.models.Swagger;
import io.swagger.models.parameters.BodyParameter;
import io.swagger.models.parameters.Parameter;
import io.swagger.v3.oas.models.media.Schema;
import org.json.simple.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class GenerationHelper {

    /**
     * If the operation contains path parameters, sets them to "" in the test request.
     *
     * @param request   TestRequest
     * @param operation Operation from Swagger documentation.
     */
    public static void setEmptyPathParameters(TestRequest request, Operation operation) {
        JSONObject pathParams = request.getPathParams();
        operation.getParameters().stream().forEach(parameter -> {
            if (parameter.getIn().equals("path")) {
                pathParams.put(parameter.getName(), "");
            }
        });
    }

    /**
     * If the operation contains path parameters, sets them to "" in the test request.
     *
     * @param request   TestRequest
     * @param operation Operation from Swagger documentation.
     */
    public static void setEmptyPathParameters(TestRequest request, io.swagger.v3.oas.models.Operation operation) {
        JSONObject pathParams = request.getPathParams();
        if(operation.getParameters() == null) return;
        operation.getParameters().stream().forEach(parameter -> {
            if (parameter.getIn().equals("path")) {
                pathParams.put(parameter.getName(), "");
            }
        });
    }

    /**
     * Creates a test case with the given name that only contains the given request.
     *
     * @param name    Name of the test case.
     * @param request Request that should be included in the test case.
     * @return TestCase object.
     */
    public static TestCase createTestCase(String name, TestRequest request) {
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
    public static TestRequest createTestRequest(String type, String path, RequestAssertion assertion) {
        return new TestRequest(type, path, Arrays.asList(assertion));
    }

    public static TestRequest createTestRequest(String type, String path, int authSelectedAgent, RequestAssertion assertion) {
        return new TestRequest(type, path, new JSONObject(), authSelectedAgent, "", Arrays.asList(assertion));
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
    public static TestRequest createTestRequest(String type, String path, String body, RequestAssertion assertion) {
        return new TestRequest(type, path, new JSONObject(), -1, body, Arrays.asList(assertion));
    }

    public static void addTestCaseIfNotNull(Map<TestCase, String> testCases, Map.Entry<TestCase, String> testCase) {
        if (testCase != null) testCases.put(testCase.getKey(), testCase.getValue());
    }

    public static boolean operationHasBodyParameter(Operation operation) {
        Parameter parameter = operation.getParameters().stream().filter(param -> param.getIn().equals("body")).findFirst().orElse(null);
        return parameter != null;
    }

    public static BodyParameter getOperationBodyParameter(Operation operation) {
        Parameter parameter = operation.getParameters().stream().filter(param -> param.getIn().equals("body")).findFirst().orElse(null);
        return (BodyParameter) parameter;
    }

    public static Model getBodyParameterSchema(Swagger swagger, BodyParameter bodyParameter) {
        for (Map.Entry<String, Model> entry : swagger.getDefinitions().entrySet()) {
            String schemaName = entry.getKey();
            if (getBodyParameterSchemaName(bodyParameter).equals(schemaName)) {
                Model model = entry.getValue();
                return model;
            }
        }
        return null;
    }

    public static String getBodyParameterSchemaName(BodyParameter bodyParameter) {
        String ref = bodyParameter.getSchema().getReference();
        return ref.split("#/definitions/")[1];
    }

    public static String getBodyParameterSchemaName(Schema schema) {
        String ref = schema.get$ref();
        return ref.split("#/components/schemas/")[1];
    }

    public static List<Parameter> getOperationPathParams(Operation operation) {
        return operation.getParameters().stream().filter(param -> param.getIn().equals("path")).toList();
    }

    public static List<io.swagger.v3.oas.models.parameters.Parameter> getOperationPathParams(io.swagger.v3.oas.models.Operation operation) {
        if(operation.getParameters() == null) return new ArrayList<>();
        return operation.getParameters().stream().filter(param -> param.getIn().equals("path")).toList();
    }

    public static boolean hasNoParameters(Operation operation) {
        return operation.getParameters().size() == 0;
    }

    public static boolean hasNoParameters(io.swagger.v3.oas.models.Operation operation) {
        if(operation.getParameters() == null) return true;
        return operation.getParameters().size() == 0;
    }

    public static boolean statusCodeAllowed(Operation operation, int statusCode) {
        return operation.getResponses().keySet().contains(String.valueOf(statusCode));
    }

    public static boolean statusCodeAllowed(io.swagger.v3.oas.models.Operation operation, int statusCode) {
        return operation.getResponses().keySet().contains(String.valueOf(statusCode));
    }
}

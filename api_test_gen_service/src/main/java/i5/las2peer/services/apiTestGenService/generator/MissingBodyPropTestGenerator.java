package i5.las2peer.services.apiTestGenService.generator;

import i5.las2peer.apiTestModel.StatusCodeAssertion;
import i5.las2peer.apiTestModel.TestCase;
import i5.las2peer.apiTestModel.TestRequest;
import io.swagger.models.HttpMethod;
import io.swagger.models.Model;
import io.swagger.models.Operation;
import io.swagger.models.Swagger;
import io.swagger.models.parameters.BodyParameter;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.RequestBody;

import java.util.Map;

import static i5.las2peer.services.apiTestGenService.generator.GenerationHelper.*;

public class MissingBodyPropTestGenerator implements TestCaseGenerator {

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
    @Override
    public Map.Entry<TestCase, String> generateTestCase(Swagger swagger, HttpMethod method, Operation operation, String path) {
        // independent of HTTP method
        if (operationHasBodyParameter(operation)) {
            // requires body
            BodyParameter bodyParameter = getOperationBodyParameter(operation);
            if (bodyParameter.getSchema() != null) {
                Model model = getBodyParameterSchema(swagger, bodyParameter);
                String schemaName = getBodyParameterSchemaName(bodyParameter);
                if (model.getProperties().keySet().size() > 0) {
                    // definition contains at least one property
                    // create test that sends empty JSONObject as body
                    return buildTestCase(false, operation, method.name(), path, schemaName);
                }
            }
        }
        return null;
    }

    /**
     * If the operation requires a body and there is a schema defined for it, this method generates a test case
     * that intentionally violates this schema and asserts on status code 400.
     *
     * @param openAPI   OpenAPI object used to search for definitions.
     * @param method    HttpMethod
     * @param operation Operation for which a test case should be generated.
     * @param path      Path
     * @return Map entry with TestCase object as key and description as value if test could be generated, null otherwise.
     */
    @Override
    public Map.Entry<TestCase, String> generateTestCaseV3(OpenAPI openAPI, PathItem.HttpMethod method,
                                                          io.swagger.v3.oas.models.Operation operation, String path) {
        // independent of HTTP method
        RequestBody bodyParameter = operation.getRequestBody();
        if (bodyParameter != null) {
            // requires body
            Content content = bodyParameter.getContent();
            if (content != null) {
                MediaType mediaType = content.get("application/json");
                if (mediaType != null) {
                    Schema schema = mediaType.getSchema();
                    if (schema != null) {
                        String schemaName = getBodyParameterSchemaName(schema);
                        Schema component = openAPI.getComponents().getSchemas().get(schemaName);
                        if(component.getProperties().keySet().size() > 0) {
                            // definition contains at least one property
                            // create test that sends empty JSONObject as body
                            return buildTestCase(true, operation, method.name(), path, schemaName);
                        }
                    }
                }
            }
        }
        return null;
    }

    private Map.Entry<TestCase, String> buildTestCase(boolean v3, Object operation, String methodName, String path, String schemaName) {
        StatusCodeAssertion assertion = new StatusCodeAssertion(0, 400);
        TestRequest request = createTestRequest(methodName, path, "{}", assertion);
        if(v3) {
            setEmptyPathParameters(request, (io.swagger.v3.oas.models.Operation) operation);
        } else {
            setEmptyPathParameters(request, (Operation) operation);
        }
        TestCase generatedTestCase = createTestCase(methodName + " " + path
                + " bad request (missing body property)", request);

        String description = "The method " + methodName + " " + path + " requires a body following the "
                + schemaName + " schema. Intentionally violating it should return status code 400 (Bad request).";
        return Map.entry(generatedTestCase, description);
    }
}

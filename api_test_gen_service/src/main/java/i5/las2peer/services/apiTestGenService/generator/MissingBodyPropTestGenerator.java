package i5.las2peer.services.apiTestGenService.generator;

import i5.las2peer.apiTestModel.StatusCodeAssertion;
import i5.las2peer.apiTestModel.TestCase;
import i5.las2peer.apiTestModel.TestRequest;
import io.swagger.models.HttpMethod;
import io.swagger.models.Model;
import io.swagger.models.Operation;
import io.swagger.models.Swagger;
import io.swagger.models.parameters.BodyParameter;

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
                    StatusCodeAssertion assertion = new StatusCodeAssertion(0, 400);
                    TestRequest request = createTestRequest(method.name(), path, "{}", assertion);
                    TestCase generatedTestCase = createTestCase(method.name() + " " + path
                            + " bad request (missing body property)", request);

                    String description = "The method " + method.name() + " " + path + " requires a body following the "
                            + schemaName + " schema. Intentionally violating it should return status code 400 (Bad request).";
                    return Map.entry(generatedTestCase, description);
                }
            }
        }
        return null;
    }
}

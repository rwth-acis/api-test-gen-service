package i5.las2peer.services.apiTestGenService.generator;

import i5.las2peer.apiTestModel.StatusCodeAssertion;
import i5.las2peer.apiTestModel.TestCase;
import i5.las2peer.apiTestModel.TestRequest;
import io.swagger.models.HttpMethod;
import io.swagger.models.Model;
import io.swagger.models.Operation;
import io.swagger.models.Swagger;
import io.swagger.models.parameters.BodyParameter;
import io.swagger.models.parameters.Parameter;
import io.swagger.models.properties.Property;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.RequestBody;
import org.json.simple.JSONObject;

import java.util.List;
import java.util.Map;

import static i5.las2peer.services.apiTestGenService.generator.GenerationHelper.*;

public class SimplePOSTBodyTestGenerator implements TestCaseGenerator {

    @Override
    public Map.Entry<TestCase, String> generateTestCase(Swagger swagger, HttpMethod method, Operation operation, String path) {
        List<Parameter> pathParams = getOperationPathParams(operation);
        if (pathParams.size() > 0 || !method.equals(HttpMethod.POST) || !operation.getResponses().keySet().contains("201"))
            return null;

        // it is a POST method without path parameters

        if (operationHasBodyParameter(operation)) {
            // has body parameter
            BodyParameter bodyParameter = getOperationBodyParameter(operation);
            if (bodyParameter.getSchema() != null) {
                Model model = getBodyParameterSchema(swagger, bodyParameter);
                if (model.getProperties().keySet().size() > 0) {
                    // definition contains at least one property
                    // create test case with generated body input
                    JSONObject body = new JSONObject();
                    for (String propertyName : model.getProperties().keySet()) {
                        Property property = model.getProperties().get(propertyName);
                        if (property.getType().equals("string")) body.put(propertyName, "text");
                        if (property.getType().equals("integer")) body.put(propertyName, 100);
                        if (property.getType().equals("boolean")) body.put(propertyName, true);
                    }

                    return buildTestCase(path, body.toJSONString());
                }
            }
        }

        return null;
    }

    @Override
    public Map.Entry<TestCase, String> generateTestCaseV3(OpenAPI openAPI, PathItem.HttpMethod method,
                                                          io.swagger.v3.oas.models.Operation operation, String path) {
        List<io.swagger.v3.oas.models.parameters.Parameter> pathParams = getOperationPathParams(operation);
        if (pathParams.size() > 0 || !method.equals(PathItem.HttpMethod.POST) || !operation.getResponses().keySet().contains("201"))
            return null;

        // it is a POST method without path parameters

        if (operation.getRequestBody() != null) {
            // has body parameter
            RequestBody bodyParameter = operation.getRequestBody();
            Content content = bodyParameter.getContent();
            if(content != null) {
                MediaType mediaType = content.get("application/json");
                if (mediaType != null) {
                    Schema schema = mediaType.getSchema();
                    if (schema != null) {
                        String schemaName = getBodyParameterSchemaName(schema);
                        Schema component = openAPI.getComponents().getSchemas().get(schemaName);
                        if(component.getProperties().keySet().size() > 0) {
                            // definition contains at least one property
                            // create test case with generated body input
                            JSONObject body = new JSONObject();
                            for (Object propertyName : component.getProperties().keySet()) {
                                Schema property = (Schema) component.getProperties().get(propertyName);
                                if (property.getType().equals("string")) body.put(propertyName, "text");
                                if (property.getType().equals("integer")) body.put(propertyName, 100);
                                if (property.getType().equals("boolean")) body.put(propertyName, true);
                            }

                            return buildTestCase(path, body.toJSONString());
                        }
                    }
                }
            }
        }

        return null;
    }

    private Map.Entry<TestCase, String> buildTestCase(String path, String body) {
        StatusCodeAssertion assertion = new StatusCodeAssertion(0, 201);
        TestRequest request = createTestRequest("POST", path, body, assertion);

        TestCase generatedTestCase = createTestCase("Test POST " + path, request);

        String description = "A schema for the body of the method POST " + path + " is given in the"
                + " documentation. Based on this, an example body has been generated. Please check"
                + " its correctness.";
        return Map.entry(generatedTestCase, description);
    }
}

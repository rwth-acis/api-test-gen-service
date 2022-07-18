package i5.las2peer.services.apiTestGenService.generator;

import i5.las2peer.apiTestModel.TestCase;
import io.swagger.models.HttpMethod;
import io.swagger.models.Operation;
import io.swagger.models.Swagger;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.PathItem;

import java.util.Map;

public interface TestCaseGenerator {
    Map.Entry<TestCase, String> generateTestCase(Swagger swagger, HttpMethod method, Operation operation, String path);
    Map.Entry<TestCase, String> generateTestCaseV3(OpenAPI openAPI, PathItem.HttpMethod method,
                                                   io.swagger.v3.oas.models.Operation operation, String path);
}

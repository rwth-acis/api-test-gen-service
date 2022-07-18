package i5.las2peer.services.apiTestGenService;

import i5.las2peer.apiTestModel.TestCase;
import i5.las2peer.services.apiTestGenService.generator.*;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;

import java.util.HashMap;
import java.util.Map;

import static i5.las2peer.services.apiTestGenService.generator.GenerationHelper.addTestCaseIfNotNull;

public class TestCaseGenerationV3 {

    /**
     * Generates test cases for given OpenAPI/Swagger operation.
     *
     * @param openAPI   OpenAPI object
     * @param method    HttpMethod
     * @param operation Operation for which test cases should be generated.
     * @param path      Path
     * @return Map containing generated test cases as keys and descriptions as values.
     */
    public static Map<TestCase, String> openAPIOperationToTests(OpenAPI openAPI, PathItem.HttpMethod method, Operation operation, String path) {
        Map<TestCase, String> testCases = new HashMap<>();

        // init generators
        TestCaseGenerator[] generators = new TestCaseGenerator[]{
                new SimpleGETTestGenerator(),
                new MissingBodyPropTestGenerator(),
                new ResourceNotFoundTestGenerator(),
                new SimpleUnauthorizedTestGenerator(),
                new SimplePOSTBodyTestGenerator()};

        // generate test cases
        for (TestCaseGenerator generator : generators) {
            addTestCaseIfNotNull(testCases, generator.generateTestCaseV3(openAPI, method, operation, path));
        }

        return testCases;
    }
}

package i5.las2peer.services.apiTestGenService;

import i5.las2peer.apiTestModel.TestCase;
import i5.las2peer.services.apiTestGenService.generator.*;
import io.swagger.models.HttpMethod;
import io.swagger.models.Operation;
import io.swagger.models.Swagger;

import java.util.*;

import static i5.las2peer.services.apiTestGenService.generator.GenerationHelper.addTestCaseIfNotNull;

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

        // init generators
        TestCaseGenerator[] generators = new TestCaseGenerator[]{
                new SimpleGETTestGenerator(),
                new MissingBodyPropTestGenerator(),
                new ResourceNotFoundTestGenerator(),
                new SimpleUnauthorizedTestGenerator(),
                new SimplePOSTBodyTestGenerator()};

        // generate test cases
        for (TestCaseGenerator generator : generators) {
            addTestCaseIfNotNull(testCases, generator.generateTestCase(swagger, method, operation, path));
        }

        return testCases;
    }
}

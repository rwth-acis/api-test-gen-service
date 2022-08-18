package i5.las2peer.services.apiTestGenService;

import i5.las2peer.api.Service;
import i5.las2peer.apiTestModel.TestCase;
import io.swagger.models.*;
import io.swagger.parser.OpenAPIParser;
import io.swagger.parser.SwaggerParser;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.parser.OpenAPIV3Parser;
import io.swagger.v3.parser.core.models.SwaggerParseResult;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.openapitools.openapidiff.core.OpenApiCompare;
import org.openapitools.openapidiff.core.model.ChangedOpenApi;
import org.openapitools.openapidiff.core.model.ChangedOperation;
import org.openapitools.openapidiff.core.model.Endpoint;

import java.util.HashMap;
import java.util.Map;

/**
 * las2peer service that generates API test cases based on a service's OpenAPI documentation.
 * Provides methods that can be called via RMI by other las2peer services.
 */
public class APITestGenService extends Service {

    /**
     * Generates test cases for the given OpenAPI documentation.
     *
     * @param docs OpenAPI documentation as String
     * @return JSONArray containing the generated test cases and their descriptions converted to String.
     */
    public String openAPIToTests(String docs) {
        Swagger swagger = new SwaggerParser().parse(docs);

        // results are returned as a JSONArray converted to string
        JSONArray arr = new JSONArray();

        // iterate through all paths of the API
        for (String path : swagger.getPaths().keySet()) {
            // generate test cases for the current path
            Map<TestCase, String> pathTestCases = openAPIPathToTests(docs, path);
            // add generated test cases and their descriptions to the JSONArray that will be returned
            for (Map.Entry<TestCase, String> entry : pathTestCases.entrySet()) {
                JSONObject obj = new JSONObject();
                obj.put("testCase", entry.getKey().toJSONObject());
                obj.put("description", entry.getValue());
                arr.add(obj);
            }
        }

        return arr.toJSONString();
    }

    /**
     * Generates test cases for the given OpenAPI documentation.
     *
     * @param docs OpenAPI documentation as String
     * @return JSONArray containing the generated test cases and their descriptions converted to String.
     */
    public String openAPIV3ToTests(String docs) {
        SwaggerParseResult parsed = new OpenAPIV3Parser().readContents(docs);
        OpenAPI openAPI = parsed.getOpenAPI();

        // results are returned as a JSONArray converted to string
        JSONArray arr = new JSONArray();

        // iterate through all paths of the API
        for (String path : openAPI.getPaths().keySet()) {
            // generate test cases for the current path
            Map<TestCase, String> pathTestCases = openAPIV3PathToTests(docs, path);
            // add generated test cases and their descriptions to the JSONArray that will be returned
            for (Map.Entry<TestCase, String> entry : pathTestCases.entrySet()) {
                JSONObject obj = new JSONObject();
                obj.put("testCase", entry.getKey().toJSONObject());
                obj.put("description", entry.getValue());
                arr.add(obj);
            }
        }

        return arr.toJSONString();
    }

    /**
     * Tries to generate a test case for an operation that was added to the OpenAPI doc or that was updated.
     *
     * @param docsOld OpenAPI documentation as String
     * @param docsUpdated OpenAPI documentation as String
     * @return JSONObject containing generated test case and description, if a test case could be generated.
     */
    public String openAPIDiffToTest(String docsOld, String docsUpdated) {
        // v2 docs will automatically be converted to v3
        SwaggerParseResult resultOld = new OpenAPIParser().readContents(docsOld, null, null);
        SwaggerParseResult resultUpdated = new OpenAPIParser().readContents(docsUpdated, null, null);
        OpenAPI openAPIOld = resultOld.getOpenAPI();
        OpenAPI openAPIUpdated = resultUpdated.getOpenAPI();

        // get changes
        ChangedOpenApi changes = OpenApiCompare.fromSpecifications(openAPIOld, openAPIUpdated);
        // if there are no changes, no test case should be generated
        if(changes.isUnchanged()) return new JSONObject().toJSONString();

        Map<TestCase, String> generatedTestCase;

        // check if there is a new operation for which a test case can be generated
        generatedTestCase = this.generateTestCaseForNewOperations(changes, openAPIUpdated);

        if(generatedTestCase == null || generatedTestCase.isEmpty()) {
            // check if there is an updated operation for which a test case can be generated
            generatedTestCase = this.generateTestCaseForUpdatedOperations(changes, openAPIUpdated);
        }

        if(generatedTestCase == null) return new JSONObject().toJSONString();

        JSONObject obj = new JSONObject();
        Map.Entry<TestCase, String> entry = generatedTestCase.entrySet().stream().findAny().get();
        obj.put("testCase", entry.getKey().toJSONObject());
        obj.put("description", entry.getValue());
        return obj.toJSONString();
    }

    /**
     * Tries to generate a test case for one of the new operations.
     *
     * @param changes OpenAPI doc changes
     * @param openAPIUpdated Updated version of OpenAPI doc
     * @return Generated test case and description, if a test case could be generated.
     */
    private Map<TestCase, String> generateTestCaseForNewOperations(ChangedOpenApi changes, OpenAPI openAPIUpdated) {
        for(Endpoint newEndpoint : changes.getNewEndpoints()) {
            // try to generate test case
            Map<TestCase, String> generatedTestCase = TestCaseGenerationV3.openAPIOperationToTests(openAPIUpdated,
                    newEndpoint.getMethod(), newEndpoint.getOperation(), newEndpoint.getPathUrl());
            if(!generatedTestCase.isEmpty()) return generatedTestCase;
        }
        return null;
    }

    /**
     * Tries to generate a test case for one of the changed operations.
     *
     * @param changes OpenAPI doc changes
     * @param openAPIUpdated Updated version of OpenAPI doc
     * @return Generated test case and description, if a test case could be generated.
     */
    private Map<TestCase, String> generateTestCaseForUpdatedOperations(ChangedOpenApi changes, OpenAPI openAPIUpdated) {
        for (ChangedOperation changedOperation : changes.getChangedOperations()) {
            // try to generate test case
            Map<TestCase, String> generatedTestCase = TestCaseGenerationV3.openAPIOperationToTests(openAPIUpdated,
                    changedOperation.getHttpMethod(), changedOperation.getNewOperation(), changedOperation.getPathUrl());
            if(!generatedTestCase.isEmpty()) return generatedTestCase;
        }
        return null;
    }

    /**
     * Generates test cases for the given OpenAPI path.
     *
     * @param docs OpenAPI documentation as String
     * @param path Path for which test cases should be generated.
     * @return Map containing generated test cases and their descriptions.
     */
    public Map<TestCase, String> openAPIPathToTests(String docs, String path) {
        // get path from swagger documentation
        Swagger swagger = new SwaggerParser().parse(docs);
        Path swaggerPath = swagger.getPath(path);

        // store generated test cases and their description in a map
        Map<TestCase, String> testCases = new HashMap<>();

        // iterate through all operations for the given path
        for (Map.Entry<HttpMethod, Operation> entry : swaggerPath.getOperationMap().entrySet()) {
            // generate test cases
            Map<TestCase, String> operationTestCases = TestCaseGeneration.openAPIOperationToTests(swagger, entry.getKey(), entry.getValue(), path);
            // add test cases to map
            operationTestCases.forEach((testCase, description) -> testCases.put(testCase, description));
        }

        return testCases;
    }

    public Map<TestCase, String> openAPIV3PathToTests(String docs, String path) {
        // get path from OpenAPI documentation
        SwaggerParseResult parsed = new OpenAPIV3Parser().readContents(docs);
        OpenAPI openAPI = parsed.getOpenAPI();
        PathItem openAPIPath = openAPI.getPaths().get(path);

        // store generated test cases and their description in a map
        Map<TestCase, String> testCases = new HashMap<>();

        // iterate through all operations for the given path
        for (Map.Entry<PathItem.HttpMethod, io.swagger.v3.oas.models.Operation> entry : openAPIPath.readOperationsMap().entrySet()) {
            // generate test cases
            Map<TestCase, String> operationTestCases = TestCaseGenerationV3.openAPIOperationToTests(openAPI, entry.getKey(), entry.getValue(), path);
            // add test cases to map
            operationTestCases.forEach((testCase, description) -> testCases.put(testCase, description));
        }

        return testCases;
    }
}

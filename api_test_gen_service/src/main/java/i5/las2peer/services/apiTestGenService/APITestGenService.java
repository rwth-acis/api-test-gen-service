package i5.las2peer.services.apiTestGenService;

import i5.las2peer.api.Service;
import i5.las2peer.apiTestModel.TestCase;
import io.swagger.models.*;
import io.swagger.parser.SwaggerParser;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

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
}

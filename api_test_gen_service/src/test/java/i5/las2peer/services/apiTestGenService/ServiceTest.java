package i5.las2peer.services.apiTestGenService;

import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.io.Resources;
import i5.las2peer.api.execution.ServiceInvocationException;
import i5.las2peer.api.security.AgentLockedException;
import i5.las2peer.apiTestModel.StatusCodeAssertion;
import i5.las2peer.apiTestModel.TestCase;
import i5.las2peer.security.AnonymousAgentImpl;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.ParseException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import i5.las2peer.api.p2p.ServiceNameVersion;
import i5.las2peer.p2p.LocalNode;
import i5.las2peer.p2p.LocalNodeManager;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

/**
 * Test class for API test generation.
 */
public class ServiceTest {

    protected static LocalNode node;

    protected static ServiceNameVersion serviceName = new ServiceNameVersion(APITestGenService.class.getName(), "1.0.0");

    /**
     * Called before a test starts.
     * Sets up the node and starts the service.
     *
     * @throws Exception
     */
    @Before
    public void startServer() throws Exception {
        // start node
        node = new LocalNodeManager().newNode();
        node.launch();

        // start service
        node.startService(serviceName, "a pass");
    }

    /**
     * Called after the test has finished. Shuts down the server.
     *
     * @throws Exception
     */
    @After
    public void shutDownServer() throws Exception {
        if (node != null) {
            node.shutDown();
            node = null;
        }
    }

    /**
     * For a GET method without any parameters the response status code should always be 200.
     */
    public void testSimpleGetNoParams(boolean v3) throws ServiceInvocationException, AgentLockedException, IOException {
        String docs = this.readSwaggerDocFromFile((v3 ? "v3" : "v2") + "/simple_get_no_params.json");
        Map<TestCase, String> testCasesMap = (Map<TestCase, String>) node.invoke(AnonymousAgentImpl.getInstance(), serviceName, "openAPI" + (v3 ? "V3" : "") + "PathToTests", new Serializable[]{docs, "/test"});
        List<TestCase> testCases = testCasesMap.keySet().stream().toList();
        assertThat(testCases, hasItem(hasProperty("requests", hasItem(
                allOf(
                        hasProperty("type", is("GET")),
                        hasProperty("url", is("/test")),
                        hasProperty("assertions", hasItem(
                                allOf(
                                        hasProperty("assertionType", is(StatusCodeAssertion.ASSERTION_TYPE_ID)),
                                        hasProperty("comparisonOperator", is(StatusCodeAssertion.COMPARISON_OPERATOR_EQUALS)),
                                        hasProperty("statusCodeValue", is(200))
                                )
                        ))
                )
        ))));
    }

    /**
     * If a schema for the request body is given, a test case violating the schema should be generated.
     */
    public void testRequestBodyWithSchema_400(boolean v3) throws IOException, ServiceInvocationException, AgentLockedException {
        String docs = this.readSwaggerDocFromFile((v3 ? "v3" : "v2") + "/request_body_with_schema.json");
        Map<TestCase, String> testCasesMap = (Map<TestCase, String>) node.invoke(AnonymousAgentImpl.getInstance(), serviceName, "openAPI" + (v3 ? "V3" : "") + "PathToTests", new Serializable[]{docs, "/test"});
        List<TestCase> testCases = testCasesMap.keySet().stream().toList();
        assertThat(testCases, hasItem(hasProperty("requests", hasItem(
                allOf(
                        hasProperty("type", is("POST")),
                        hasProperty("url", is("/test")),
                        hasProperty("body", is("{}")),
                        hasProperty("assertions", hasItem(
                                allOf(
                                        hasProperty("assertionType", is(StatusCodeAssertion.ASSERTION_TYPE_ID)),
                                        hasProperty("comparisonOperator", is(StatusCodeAssertion.COMPARISON_OPERATOR_EQUALS)),
                                        hasProperty("statusCodeValue", is(400))
                                )
                        ))
                )
        ))));
    }

    /**
     * Test for POST request JSON body generation.
     */
    public void testRequestBodyWithSchema_201(boolean v3) throws IOException, ServiceInvocationException, AgentLockedException {
        String docs = this.readSwaggerDocFromFile((v3 ? "v3" : "v2") + "/request_body_with_schema.json");
        Map<TestCase, String> testCasesMap = (Map<TestCase, String>) node.invoke(AnonymousAgentImpl.getInstance(), serviceName, "openAPI" + (v3 ? "V3" : "") + "PathToTests", new Serializable[]{docs, "/test"});
        List<TestCase> testCases = testCasesMap.keySet().stream().toList();
        assertThat(testCases, hasItem(hasProperty("requests", hasItem(
                allOf(
                        hasProperty("type", is("POST")),
                        hasProperty("url", is("/test")),
                        hasProperty("body", stringContainsInOrder("{", "name" ,"}")),
                        hasProperty("assertions", hasItem(
                                allOf(
                                        hasProperty("assertionType", is(StatusCodeAssertion.ASSERTION_TYPE_ID)),
                                        hasProperty("comparisonOperator", is(StatusCodeAssertion.COMPARISON_OPERATOR_EQUALS)),
                                        hasProperty("statusCodeValue", is(201))
                                )
                        ))
                )
        ))));
    }

    /**
     * "Resource not found" test case should be generated if path parameters are available.
     */
    public void testPathParamResourceNotFound(boolean v3) throws IOException, ServiceInvocationException, AgentLockedException {
        String docs = this.readSwaggerDocFromFile((v3 ? "v3" : "v2") + "/path_param_resource_not_found.json");
        Map<TestCase, String> testCasesMap = (Map<TestCase, String>) node.invoke(AnonymousAgentImpl.getInstance(), serviceName, "openAPI"  + (v3 ? "V3" : "") + "PathToTests", new Serializable[]{docs, "/dishes/{id}/ratings"});
        List<TestCase> testCases = testCasesMap.keySet().stream().toList();
        assertThat(testCases, hasItem(hasProperty("requests", hasItem(
                allOf(
                        hasProperty("type", is("GET")),
                        hasProperty("url", is("/dishes/{id}/ratings")),
                        hasProperty("pathParams", hasKey("id")),
                        hasProperty("assertions", hasItem(
                                allOf(
                                        hasProperty("assertionType", is(StatusCodeAssertion.ASSERTION_TYPE_ID)),
                                        hasProperty("comparisonOperator", is(StatusCodeAssertion.COMPARISON_OPERATOR_EQUALS)),
                                        hasProperty("statusCodeValue", is(404))
                                )
                        ))
                )
        ))));
    }

    /**
     * "Unauthorized" test case should be generated if status code 403 is allowed.
     */
    public void testSimpleOperationAgentRequired_403(boolean v3) throws IOException, ServiceInvocationException, AgentLockedException {
        String docs = this.readSwaggerDocFromFile((v3 ? "v3" : "v2") + "/simple_operation_agent_required_403.json");
        Map<TestCase, String> testCasesMap = (Map<TestCase, String>) node.invoke(AnonymousAgentImpl.getInstance(), serviceName, "openAPI" + (v3 ? "V3" : "") + "PathToTests", new Serializable[]{docs, "/test"});
        List<TestCase> testCases = testCasesMap.keySet().stream().toList();
        assertThat(testCases, hasItem(hasProperty("requests", hasItem(
                allOf(
                        hasProperty("type", is("DELETE")),
                        hasProperty("url", is("/test")),
                        hasProperty("agent", is(0)),
                        hasProperty("assertions", hasItem(
                                allOf(
                                        hasProperty("assertionType", is(StatusCodeAssertion.ASSERTION_TYPE_ID)),
                                        hasProperty("comparisonOperator", is(StatusCodeAssertion.COMPARISON_OPERATOR_EQUALS)),
                                        hasProperty("statusCodeValue", is(403))
                                )
                        ))
                )
        ))));
    }

    /**
     * "Unauthorized" test case should be generated if status code 401 is allowed.
     */
    public void testSimpleOperationAgentRequired_401(boolean v3) throws IOException, ServiceInvocationException, AgentLockedException {
        String docs = this.readSwaggerDocFromFile((v3 ? "v3" : "v2") + "/simple_operation_agent_required_401.json");
        Map<TestCase, String> testCasesMap = (Map<TestCase, String>) node.invoke(AnonymousAgentImpl.getInstance(), serviceName, "openAPI" + (v3 ? "V3" : "") + "PathToTests", new Serializable[]{docs, "/test"});
        List<TestCase> testCases = testCasesMap.keySet().stream().toList();
        assertThat(testCases, hasItem(hasProperty("requests", hasItem(
                allOf(
                        hasProperty("type", is("DELETE")),
                        hasProperty("url", is("/test")),
                        hasProperty("agent", is(0)),
                        hasProperty("assertions", hasItem(
                                allOf(
                                        hasProperty("assertionType", is(StatusCodeAssertion.ASSERTION_TYPE_ID)),
                                        hasProperty("comparisonOperator", is(StatusCodeAssertion.COMPARISON_OPERATOR_EQUALS)),
                                        hasProperty("statusCodeValue", is(401))
                                )
                        ))
                )
        ))));
    }

    /**
     * "Unauthorized" test case should be generated if status code 401 is allowed.
     * This test also verifies that the path parameters are initialized.
     */
    public void testOperationAgentRequired_401(boolean v3) throws IOException, ServiceInvocationException, AgentLockedException {
        String docs = this.readSwaggerDocFromFile((v3 ? "v3" : "v2") + "/operation_agent_required_401.json");
        Map<TestCase, String> testCasesMap = (Map<TestCase, String>) node.invoke(AnonymousAgentImpl.getInstance(), serviceName, "openAPI" + (v3 ? "V3" : "") + "PathToTests", new Serializable[]{docs, "/test/{id}"});
        List<TestCase> testCases = testCasesMap.keySet().stream().toList();
        assertThat(testCases, hasItem(hasProperty("requests", hasItem(
                allOf(
                        hasProperty("type", is("GET")),
                        hasProperty("url", is("/test/{id}")),
                        hasProperty("pathParams", hasKey("id")),
                        hasProperty("body", is("")),
                        hasProperty("agent", is(0)),
                        hasProperty("assertions", hasItem(
                                allOf(
                                        hasProperty("assertionType", is(StatusCodeAssertion.ASSERTION_TYPE_ID)),
                                        hasProperty("comparisonOperator", is(StatusCodeAssertion.COMPARISON_OPERATOR_EQUALS)),
                                        hasProperty("statusCodeValue", is(401))
                                )
                        ))
                )
        ))));
    }

    /**
     * Test for method "openAPIToTests".
     */
    public void testOpenAPIToTests(boolean v3) throws IOException, ServiceInvocationException, AgentLockedException, ParseException {
        String docs = this.readSwaggerDocFromFile((v3 ? "v3" : "v2") + "/simple_get_no_params.json");
        String result = (String) node.invoke(AnonymousAgentImpl.getInstance(), serviceName, "openAPI" + (v3 ? "V3" : "") + "ToTests", new Serializable[] {docs});
        JSONArray testsArray = (JSONArray) JSONValue.parseWithException(result);
        // at least one test case should be generated
        assertTrue(testsArray.size() > 0);
        // check result structure (of first item)
        assertThat(testsArray.get(0), isA(JSONObject.class));
        JSONObject obj = (JSONObject) testsArray.get(0);
        assertThat((HashMap<String, Object>) obj, allOf(
                        hasKey("testCase"),
                        hasKey("description")
                )
        );
    }

    /**
     * Test for method "openAPIDiffToTest" with an added endpoint.
     */
    public void testAddedEndpoint(boolean v3) throws ServiceInvocationException, AgentLockedException, IOException {
        String docEmpty = this.readSwaggerDocFromFile((v3 ? "v3" : "v2") + "/empty.json");
        String doc = this.readSwaggerDocFromFile((v3 ? "v3" : "v2") + "/simple_get_no_params.json");
        String result = (String) node.invoke(AnonymousAgentImpl.getInstance(), serviceName, "openAPIDiffToTest", new Serializable[]{docEmpty, doc});
        JSONObject resultJSON = (JSONObject) JSONValue.parse(result);
        assertTrue(resultJSON.containsKey("testCase"));
    }

    /**
     * Test for method "openAPIDiffToTest" with no OpenAPI doc differences.
     */
    public void testUnchanged(boolean v3) throws IOException, ServiceInvocationException, AgentLockedException {
        String doc = this.readSwaggerDocFromFile((v3 ? "v3" : "v2") + "/simple_get_no_params.json");
        String result = (String) node.invoke(AnonymousAgentImpl.getInstance(), serviceName, "openAPIDiffToTest", new Serializable[]{doc, doc});
        JSONObject resultJSON = (JSONObject) JSONValue.parse(result);
        // if the OpenAPI doc is unchanged, no test case should be generated
        assertFalse(resultJSON.containsKey("testCase"));
    }

    protected String readSwaggerDocFromFile(String fileName) throws IOException {
        URL url = Resources.getResource(fileName);
        return Resources.toString(url, StandardCharsets.UTF_8);
    }

}

package i5.las2peer.services.apiTestGenService;

import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import com.google.common.io.Resources;
import i5.las2peer.api.execution.ServiceInvocationException;
import i5.las2peer.api.security.AgentLockedException;
import i5.las2peer.apiTestModel.StatusCodeAssertion;
import i5.las2peer.apiTestModel.TestCase;
import i5.las2peer.security.AnonymousAgentImpl;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import i5.las2peer.api.p2p.ServiceNameVersion;
import i5.las2peer.p2p.LocalNode;
import i5.las2peer.p2p.LocalNodeManager;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * Test class for API test generation.
 */
public class ServiceTest {

    private static LocalNode node;

    private static ServiceNameVersion serviceName = new ServiceNameVersion(APITestGenService.class.getName(), "1.0.0");

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
    @Test
    public void testSimpleGetNoParams() throws ServiceInvocationException, AgentLockedException, IOException {
        String docs = this.readSwaggerDocFromFile("simple_get_no_params.json");
        Map<TestCase, String> testCasesMap = (Map<TestCase, String>) node.invoke(AnonymousAgentImpl.getInstance(), serviceName, "openAPIPathToTests", new Serializable[]{docs, "/test"});
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
    @Test
    public void testRequestBodyWithSchema() throws IOException, ServiceInvocationException, AgentLockedException {
        String docs = this.readSwaggerDocFromFile("request_body_with_schema.json");
        Map<TestCase, String> testCasesMap = (Map<TestCase, String>) node.invoke(AnonymousAgentImpl.getInstance(), serviceName, "openAPIPathToTests", new Serializable[]{docs, "/test"});
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
	 * "Resource not found" test case should be generated if path parameters are available.
	 */
    @Test
    public void testPathParamResourceNotFound() throws IOException, ServiceInvocationException, AgentLockedException {
        String docs = this.readSwaggerDocFromFile("path_param_resource_not_found.json");
        Map<TestCase, String> testCasesMap = (Map<TestCase, String>) node.invoke(AnonymousAgentImpl.getInstance(), serviceName, "openAPIPathToTests", new Serializable[]{docs, "/dishes/{id}/ratings"});
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

    private String readSwaggerDocFromFile(String fileName) throws IOException {
        URL url = Resources.getResource(fileName);
        return Resources.toString(url, StandardCharsets.UTF_8);
    }

}

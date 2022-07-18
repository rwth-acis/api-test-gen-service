package i5.las2peer.services.apiTestGenService;

import i5.las2peer.api.execution.ServiceInvocationException;
import i5.las2peer.api.security.AgentLockedException;
import i5.las2peer.apiTestModel.StatusCodeAssertion;
import i5.las2peer.apiTestModel.TestCase;
import i5.las2peer.security.AnonymousAgentImpl;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.ParseException;
import org.junit.Test;

import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertTrue;

public class ServiceTestV2 extends ServiceTest {

    /**
     * For a GET method without any parameters the response status code should always be 200.
     */
    @Test
    public void testSimpleGetNoParams() throws ServiceInvocationException, AgentLockedException, IOException {
        super.testSimpleGetNoParams(false);
    }

    /**
     * If a schema for the request body is given, a test case violating the schema should be generated.
     */
    @Test
    public void testRequestBodyWithSchema_400() throws IOException, ServiceInvocationException, AgentLockedException {
        super.testRequestBodyWithSchema_400(false);
    }

    /**
     * Test for POST request JSON body generation.
     */
    @Test
    public void testRequestBodyWithSchema_201() throws IOException, ServiceInvocationException, AgentLockedException {
        super.testRequestBodyWithSchema_201(false);
    }

    /**
     * "Resource not found" test case should be generated if path parameters are available.
     */
    @Test
    public void testPathParamResourceNotFound() throws IOException, ServiceInvocationException, AgentLockedException {
        super.testPathParamResourceNotFound(false);
    }

    /**
     * "Unauthorized" test case should be generated if status code 403 is allowed.
     */
    @Test
    public void testSimpleOperationAgentRequired_403() throws IOException, ServiceInvocationException, AgentLockedException {
        super.testSimpleOperationAgentRequired_403(false);
    }

    /**
     * "Unauthorized" test case should be generated if status code 401 is allowed.
     */
    @Test
    public void testSimpleOperationAgentRequired_401() throws IOException, ServiceInvocationException, AgentLockedException {
        super.testSimpleOperationAgentRequired_401(false);
    }

    /**
     * "Unauthorized" test case should be generated if status code 401 is allowed.
     * This test also verifies that the path parameters are initialized.
     */
    @Test
    public void testOperationAgentRequired_401() throws IOException, ServiceInvocationException, AgentLockedException {
        super.testOperationAgentRequired_401(false);
    }

    /**
     * Test for method "openAPIToTests".
     */
    @Test
    public void testOpenAPIToTests() throws IOException, ServiceInvocationException, AgentLockedException, ParseException {
        super.testOpenAPIToTests(false);
    }

}

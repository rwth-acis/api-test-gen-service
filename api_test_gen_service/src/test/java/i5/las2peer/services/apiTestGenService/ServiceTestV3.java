package i5.las2peer.services.apiTestGenService;

import i5.las2peer.api.execution.ServiceInvocationException;
import i5.las2peer.api.security.AgentLockedException;
import org.json.simple.parser.ParseException;
import org.junit.Test;

import java.io.IOException;


public class ServiceTestV3 extends ServiceTest {

    /**
     * For a GET method without any parameters the response status code should always be 200.
     */
    @Test
    public void testSimpleGetNoParamsV3() throws ServiceInvocationException, AgentLockedException, IOException {
        super.testSimpleGetNoParams(true);
    }

    /**
     * If a schema for the request body is given, a test case violating the schema should be generated.
     */
    @Test
    public void testRequestBodyWithSchema_400V3() throws IOException, ServiceInvocationException, AgentLockedException {
        super.testRequestBodyWithSchema_400(true);
    }

    /**
     * Test for POST request JSON body generation.
     */
    @Test
    public void testRequestBodyWithSchema_201V3() throws IOException, ServiceInvocationException, AgentLockedException {
        super.testRequestBodyWithSchema_201(true);
    }

    /**
     * "Resource not found" test case should be generated if path parameters are available.
     */
    @Test
    public void testPathParamResourceNotFoundV3() throws IOException, ServiceInvocationException, AgentLockedException {
        super.testPathParamResourceNotFound(true);
    }

    /**
     * "Unauthorized" test case should be generated if status code 403 is allowed.
     */
    @Test
    public void testSimpleOperationAgentRequired_403V3() throws IOException, ServiceInvocationException, AgentLockedException {
        super.testSimpleOperationAgentRequired_403(true);
    }

    /**
     * "Unauthorized" test case should be generated if status code 401 is allowed.
     */
    @Test
    public void testSimpleOperationAgentRequired_401V3() throws IOException, ServiceInvocationException, AgentLockedException {
        super.testSimpleOperationAgentRequired_401(true);
    }

    /**
     * "Unauthorized" test case should be generated if status code 401 is allowed.
     * This test also verifies that the path parameters are initialized.
     */
    @Test
    public void testOperationAgentRequired_401V3() throws IOException, ServiceInvocationException, AgentLockedException {
        super.testOperationAgentRequired_401(true);
    }

    /**
     * Test for method "openAPIToTests".
     */
    @Test
    public void testOpenAPIToTestsV3() throws IOException, ServiceInvocationException, AgentLockedException, ParseException {
        super.testOpenAPIToTests(true);
    }

}

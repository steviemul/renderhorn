package occ.ssr.rest;

import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import occ.ssr.Registry;

/**
 * The Class OperationsEndpoint.
 */
@Path("/ops")
public class OperationsEndpoint {

  private static Log mLogger = LogFactory.getLog(OperationsEndpoint.class);
  private static final String OP_CLEAR = "clear";
  
  /**
   * Process.
   *
   * @param op the op
   * @return the response
   */
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public Response process(@QueryParam("op") final String op) {
    
    Map<String, String> result = new HashMap<>();
    
    try {
      if (OP_CLEAR.equals(op)) {
        Registry.getRenderingEngine().clearCache();
        result.put("result", "success");
      }
    }
    catch (Exception e) {
      mLogger.error("Failed to reload engine.", e);
      result.put("result", "failure");
      result.put("message", e.getMessage());
    }
    
    Response response = Response.ok()
        .type(MediaType.APPLICATION_JSON)
        .entity(result)
        .build();
    
    return response;
  }
}

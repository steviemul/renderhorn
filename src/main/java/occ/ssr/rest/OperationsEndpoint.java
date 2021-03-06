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
  private static final String OP_RELOAD = "reload";
  
  /**
   * Process.
   *
   * @param op the op
   * @return the response
   */
  @SuppressWarnings({ "unchecked", "rawtypes" })
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public Response process(@QueryParam("op") final String op) {
    
    Object result = "";
    
    try {
      switch (op) {
      case OP_CLEAR:
        Registry.getRenderingEngine().clearCache();
        break;
      case OP_RELOAD:
        Registry.getRenderingEngine().reload();
        break;
      }
      
      Map response = new HashMap<>();
      response.put("result", "success");
      
      result = response;
    }
    catch (Exception e) {
      mLogger.error("Failed to call operation.", e);
      
      Map response = new HashMap<>();
      
      response.put("result", "failure");
      response.put("message", e.getMessage());
      
      result = response;
    }
    
    Response response = Response.ok()
        .type(MediaType.APPLICATION_JSON)
        .entity(result)
        .build();
    
    return response;
  }
}

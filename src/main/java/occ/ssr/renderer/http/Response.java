package occ.ssr.renderer.http;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import io.undertow.server.HttpServerExchange;
import io.undertow.util.Methods;
import occ.ssr.Settings;

/**
 * The Class RenderResponse.
 */
public class Response {
  
  private static Log mLogger = LogFactory.getLog(Response.class);
  
  private final HttpServerExchange mExchange;
  private final Settings mSettings;
  
  /**
   * Instantiates a new render response.
   *
   * @param pExchange the exchange
   */
  public Response(HttpServerExchange pExchange, Settings pSettings) {
    mExchange = pExchange;
    mSettings = pSettings;
  }
  
  /**
   * Push resource.
   *
   * @param pResourcePath the resource path
   */
  public void pushResource(String pResourcePath) {
    
    if (mSettings.isHttp2PushEnabled()) {
      mExchange.getConnection().pushResource(pResourcePath, Methods.GET, mExchange.getRequestHeaders());
      
      mLogger.debug("Attempted to h2 push " + pResourcePath);
    }
  }
  
}

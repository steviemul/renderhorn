package occ.ssr.renderer.http;

import io.undertow.server.HttpServerExchange;
import io.undertow.util.Methods;
import occ.ssr.Settings;

/**
 * The Class RenderResponse.
 */
public class Response {

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
    }
  }
  
}

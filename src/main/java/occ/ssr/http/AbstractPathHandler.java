package occ.ssr.http;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import occ.ssr.Settings;

/**
 * The Class AbstractPathHandler.
 */
public abstract class AbstractPathHandler implements HttpHandler {
 
  private Map<String, HttpHandler> mHandlers = new HashMap<>();
  public static final String FORWARD_SLASH = "/";
  
  /**
   * Instantiates a new static handler.
   *
   * @param pSettings the settings
   */
  public AbstractPathHandler(Settings pSettings) {
    initHandlers(pSettings);
  }
  
  /**
   * Accepts.
   *
   * @param pResourcePath the resource path
   * @return true, if successful
   */
  public boolean accepts(String pResourcePath) {
    return (getHandler(pResourcePath) != null);
  }
  
  /* (non-Javadoc)
   * @see io.undertow.server.HttpHandler#handleRequest(io.undertow.server.HttpServerExchange)
   */
  @Override
  public void handleRequest(HttpServerExchange exchange) throws Exception {
    
    HttpHandler handler = getHandler(exchange.getRequestPath());
    
    if (handler != null) {
      handler.handleRequest(exchange);
    }
  }
  
  protected abstract void initHandlers(Settings pSettings);
  
  /**
   * Adds the handler.
   *
   * @param pRoot the root
   * @param pHandler the handler
   */
  protected void addHandler(String pRoot, HttpHandler pHandler) {
    mHandlers.put(pRoot, pHandler);
  }
  
  /**
   * Gets the handler.
   *
   * @param pResourcePath the resource path
   * @return the handler
   */
  private HttpHandler getHandler(String pResourcePath) {
    
    HttpHandler handler = null;
    
    if (!StringUtils.isEmpty(pResourcePath)) {
      
      if (pResourcePath.startsWith(FORWARD_SLASH)) {
        pResourcePath = pResourcePath.substring(1);
        
        String[] parts = pResourcePath.split(FORWARD_SLASH);
        
        if (!ArrayUtils.isEmpty(parts)) {
          if (mHandlers.containsKey(parts[0])) {
            handler = mHandlers.get(parts[0]);
          }
        }
      }
    }

    return handler;
  }
}

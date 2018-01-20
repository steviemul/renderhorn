package occ.ssr.http;

import java.io.File;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import io.undertow.Handlers;
import io.undertow.predicate.Predicates;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.server.handlers.resource.PathResourceManager;
import io.undertow.server.handlers.resource.ResourceHandler;
import occ.ssr.Settings;

/**
 * The Class StaticHandler.
 */
public class StaticHandler implements HttpHandler {

  private final Settings mSettings;
  private Map<String, HttpHandler> mHandlers;
  private static final String FORWAWRD_SLASH = "/";
  
  /**
   * Instantiates a new static handler.
   *
   * @param pSettings the settings
   */
  public StaticHandler(Settings pSettings) {
    mSettings = pSettings;
    
    initHandlers();
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
  
  /**
   * Gets the handler.
   *
   * @param pResourcePath the resource path
   * @return the handler
   */
  private HttpHandler getHandler(String pResourcePath) {
    
    HttpHandler handler = null;
    
    if (!StringUtils.isEmpty(pResourcePath)) {
      
      if (pResourcePath.startsWith(FORWAWRD_SLASH)) {
        pResourcePath = pResourcePath.substring(1);
        
        String[] parts = pResourcePath.split(FORWAWRD_SLASH);
        
        if (!ArrayUtils.isEmpty(parts)) {
          if (mHandlers.containsKey(parts[0])) {
            handler = mHandlers.get(parts[0]);
          }
        }
      }
    }

    return handler;
  }
  
  
  /**
   * Inits the handlers.
   */
  private void initHandlers() {
    mHandlers = new HashMap<>();
    
    List<String> staticDirs = mSettings.getStaticDirs();
    
    for (String staticDir : staticDirs) {
      buildHandler(staticDir);
    }
  }
  
  /**
   * Builds the handler.
   *
   * @param pStaticDir the static dir
   */
  private void buildHandler(String pStaticDir) {
    
    ResourceHandler resourceHandler = Handlers.resource(new PathResourceManager(Paths.get(pStaticDir), 100));
    
    if (mSettings.getStaticCacheTime() > 0) {
      resourceHandler.setCachable(Predicates.truePredicate());
      resourceHandler.setCacheTime(mSettings.getStaticCacheTime());
    }
    
    File staticDir = new File(pStaticDir);
    
    if (staticDir.exists() && staticDir.isDirectory()) {
      for (String child : staticDir.list()) {
        mHandlers.put(child, resourceHandler);
      }
    }
  }
}

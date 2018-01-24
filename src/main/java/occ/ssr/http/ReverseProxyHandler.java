package occ.ssr.http;

import java.net.URI;
import java.util.Map.Entry;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import io.undertow.server.HttpHandler;
import io.undertow.server.handlers.ResponseCodeHandler;
import io.undertow.server.handlers.proxy.ProxyHandler;
import io.undertow.server.handlers.proxy.SimpleProxyClientProvider;
import occ.ssr.Settings;

/**
 * The Class ReverseProxyHandler.
 */
public class ReverseProxyHandler extends AbstractPathHandler {

  private static Log mLogger = LogFactory.getLog(ReverseProxyHandler.class);
  
  /**
   * Instantiates a new reverse proxy handler.
   *
   * @param pSettings the settings
   */
  public ReverseProxyHandler(Settings pSettings) {
    super(pSettings);
  }

  /* (non-Javadoc)
   * @see occ.ssr.http.AbstractPathHandler#initHandlers(occ.ssr.Settings)
   */
  @Override
  protected void initHandlers(Settings pSettings) {
    
    for (Entry<String, String> proxyEntry : pSettings.getProxyPaths().entrySet()) {
      String root = proxyEntry.getKey();
      
      if (root.startsWith(FORWARD_SLASH)) {
        root = root.substring(1);
      }
      
      String target = proxyEntry.getValue();
      
      try {
        SimpleProxyClientProvider provider = new SimpleProxyClientProvider(new URI(target));
        HttpHandler handler = new ProxyHandler(provider, 30000, ResponseCodeHandler.HANDLE_404);
        
        addHandler(root, handler);
      }
      catch (Exception e) {
        mLogger.error("Unable to create handler for " + proxyEntry, e);
      }
    }
    
  }
 
}

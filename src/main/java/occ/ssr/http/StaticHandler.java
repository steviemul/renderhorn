package occ.ssr.http;

import java.io.File;
import java.nio.file.Paths;
import java.util.List;

import io.undertow.Handlers;
import io.undertow.predicate.Predicates;
import io.undertow.server.handlers.resource.PathResourceManager;
import io.undertow.server.handlers.resource.ResourceHandler;
import occ.ssr.Settings;

/**
 * The Class StaticHandler.
 */
public class StaticHandler extends AbstractPathHandler {  
  
  public StaticHandler(Settings pSettings) {
    super(pSettings);
  }
  
  /**
   * Inits the handlers.
   *
   * @param pSettings the settings
   */
  protected void initHandlers(Settings pSettings) {
    
    List<String> staticDirs = pSettings.getStaticDirs();
    
    for (String staticDir : staticDirs) {
      buildHandler(staticDir, pSettings);
    }
  }
  
  /**
   * Builds the handler.
   *
   * @param pStaticDir the static dir
   */
  private void buildHandler(String pStaticDir, Settings pSettings) {
    
    ResourceHandler resourceHandler = Handlers.resource(new PathResourceManager(Paths.get(pStaticDir), 100));
    
    if (pSettings.getStaticCacheTime() > 0) {
      resourceHandler.setCachable(Predicates.truePredicate());
      resourceHandler.setCacheTime(pSettings.getStaticCacheTime());
    }
    
    File staticDir = new File(pStaticDir);
    
    if (staticDir.exists() && staticDir.isDirectory()) {
      for (String child : staticDir.list()) {
        addHandler(child, resourceHandler);
      }
    }
  }
}

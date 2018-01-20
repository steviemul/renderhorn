package occ.ssr;

import java.io.File;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import occ.ssr.js.RenderingEngine;

/**
 * The Class App.
 */
public class App {
  
  private static Log mLogger = LogFactory.getLog(App.class);
  private static final String SETTINGS_JSON = "settings.json";
  private static final String APP_DIR = "app.dir";
  
  /**
   * The main method.
   *
   * @param args the arguments
   */
  public static void main(String[] args) {
    
    try {
      File appDir = getApplicationDirectory();
      
      mLogger.info("Application directory is " + appDir.getAbsolutePath());

      Settings settings = new Settings(SETTINGS_JSON, appDir);
      
      mLogger.info("Initializing Renderer.");
      
      RenderingEngine engine = new RenderingEngine(settings);
      
      mLogger.info("Starting Server");  
      
      RenderingServer server = new RenderingServer(engine, settings);
      
      server.start();
      
      Registry.init(settings, engine);
    }
    catch (Exception e) {
      mLogger.error("Unable to start server." ,e);
      System.exit(1);
    }
  }
  
  private static File getApplicationDirectory() {
    
    File appDir = null;
    
    if (System.getProperty(APP_DIR) != null) {
      appDir = new File(System.getProperty(APP_DIR));
    }
    else {
      appDir = new File("").getAbsoluteFile();
    }
    
    if (!appDir.exists() || ! appDir.isDirectory()) {
      throw new IllegalArgumentException("Application directory " + appDir + " is not valid.");
    }
    
    return appDir;
  }
}

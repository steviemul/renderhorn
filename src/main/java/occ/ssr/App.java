package occ.ssr;

import java.io.File;
import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;

import occ.ssr.js.RenderingEngine;

/**
 * The Class App.
 * 
 * Entry point for application. This initializes the javacript engine, server etc.
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
      Settings settings = getSettings();
      
      mLogger.info("Application directory is " + settings.getRootDirectory().getAbsolutePath());
      
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
  
  /**
   * Gets the settings.
   *
   * @return the settings
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws JSONException the JSON exception
   */
  private static Settings getSettings() throws IOException, JSONException {
    
    Settings settings = null;
    
    if (System.getProperty(APP_DIR) != null) {
      settings = new Settings(SETTINGS_JSON, new File(System.getProperty(APP_DIR)));
    }
    else {
      settings = new Settings(SETTINGS_JSON);
    }
    
    return settings;
  }
}

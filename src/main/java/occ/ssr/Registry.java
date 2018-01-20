package occ.ssr;

import occ.ssr.js.RenderingEngine;

/**
 * The Class Registry.
 */
public class Registry {
  
  private static Settings mSettings;
  
  /**
   * Gets the settings.
   *
   * @return the settings
   */
  public static Settings getSettings() {
    return mSettings;
  }
  
  private static RenderingEngine mEngine;
  
  /**
   * Gets the rendering engine.
   *
   * @return the rendering engine
   */
  public static RenderingEngine getRenderingEngine() {
    return mEngine;
  }
  
  /**
   * Inits the.
   *
   * @param pSettings the settings
   * @param pRenderingEngine the rendering engine
   */
  static void init( Settings pSettings, RenderingEngine pRenderingEngine) {
    mSettings = pSettings;
    mEngine = pRenderingEngine;
  }
}

package occ.ssr.js;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * The Class Console.
 */
public class Console {
  
  private final Log mLogger;
  
  /**
   * Instantiates a new console.
   *
   * @param pName the name
   */
  public Console(String pName) {
    mLogger = LogFactory.getLog(pName); 
  }
  
  /**
   * Debug.
   *
   * @param pMessage the message
   */
  public void debug(String pMessage) {
    mLogger.debug(pMessage);
  }
  
  /**
   * Info.
   *
   * @param pMessage the message
   */
  public void info(String pMessage) {
    mLogger.info(pMessage);
  }
  
  /**
   * Log.
   *
   * @param pMessage the message
   */
  public void log(String pMessage) {
    mLogger.info(pMessage);
  }
  
  /**
   * Error.
   *
   * @param pMessage the message
   */
  public void error(String pMessage) {
    mLogger.error(pMessage);
  }
  
  /**
   * Trace.
   *
   * @param pMessage the message
   */
  public void trace(String pMessage) {
    mLogger.trace(pMessage);
  }
  
}

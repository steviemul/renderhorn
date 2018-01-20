package occ.ssr.js;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * The Class Console.
 * 
 * This class will be the console object on the javascript engine.
 * This wraps an concrete logger with pass through methods to the relative log level calls.
 */
public class Console {
  
  private final Log mLogger;
  
  private Map<String, Long> mTimers = new HashMap<>();
  
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
   * Warn.
   *
   * @param pMessage the message
   */
  public void warn(String pMessage) {
    mLogger.warn(pMessage);
  }
  
  /**
   * Trace.
   *
   * @param pMessage the message
   */
  public void trace(String pMessage) {
    mLogger.trace(pMessage);
  }
  
  /**
   * Time.
   *
   * @param pTimer the timer
   */
  public void time(String pTimer) {
    Long now = new Date().getTime();
    
    mTimers.put(pTimer, now);
  }
  
  /**
   * Time end.
   *
   * @param pTimer the timer
   */
  public void timeEnd(String pTimer) {
    
    if (mTimers.containsKey(pTimer)) {
      long now = new Date().getTime();
      long start = mTimers.get(pTimer);
          
      info("Timer " + pTimer + " took " + (now - start) + "ms.");
      
      mTimers.remove(pTimer);
    }
  }
}

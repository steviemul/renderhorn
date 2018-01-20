package occ.ssr.js;

import javax.script.Bindings;
import javax.script.Invocable;
import javax.script.ScriptEngine;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * The Class ObjectHelper.
 * 
 * Contains some helper methods for dealing with native javascript engine objects.
 */
public class ObjectHelper {

  private final ScriptEngine mScriptEngine;
  private final Bindings mBindings;
  private static Log mLogger = LogFactory.getLog(ObjectHelper.class);
  
  private static final String JSON_KEY = "JSON";
  private static final String PARSE_METHOD = "parse";
  private static final String STRINGIFY_METHOD = "stringify";
  
  private final Object JSON;
  
  /**
   * Instantiates a new object helper.
   *
   * @param pScriptEngine the script engine
   * @param pBindings the bindings
   */
  public ObjectHelper(ScriptEngine pScriptEngine, Bindings pBindings) {
    mScriptEngine = pScriptEngine;
    mBindings = pBindings;
    
    JSON = mBindings.get(JSON_KEY);
  }
  
  /**
   * Stringify.
   *
   * Calls the native Javascript JSON.stringify.
   * This can be helpful if you get a Javascript object back from an invoked method or function
   * and you need to get a string version of that.
   * 
   * @param pObject the object
   * @return the string
   */
  public String stringify(Object pObject) {
    
    String result = null;
    
    try {
      Invocable invocableEngine = (Invocable) mScriptEngine;
      
      Object[] invokeArgs = {pObject};
      
      result = (String) invocableEngine.invokeMethod(JSON, STRINGIFY_METHOD, invokeArgs);  
    }
    catch (Exception e) {
      mLogger.error("Unable to call JSON.stringify.", e);
    }
    
    return result;
  }
  
  /**
   * This calls the native Javascript JSON.parse.
   * 
   * This is useful if you have a string of json and you want to pass that into 
   * a native javascript function as a parsed json object that it can understand.
   *
   * @param pString the string
   * @return the object
   */
  public Object parse(String pString) {
    
    Object result = null;
    
    try {
      Invocable invocableEngine = (Invocable) mScriptEngine;
      
      Object[] invokeArgs = {pString};
      
      result = invocableEngine.invokeMethod(JSON, PARSE_METHOD, invokeArgs);  
    }
    catch (Exception e) {
      mLogger.error("Unable to call JSON.parse.", e);
    }
    
    return result;
  }
}

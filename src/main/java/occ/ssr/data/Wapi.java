package occ.ssr.data;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import occ.ssr.js.ObjectHelper;

/**
 * The Class Wapi.
 */
public class Wapi {
  
  private static Log mLogger = LogFactory.getLog(Wapi.class);
  private static final String JSON_DIR = "json/";
  private static final String JSON_EXT = ".json";
  
  private final ObjectHelper mObjectHelper;
  
  /**
   * Instantiates a new wapi.
   *
   * @param pObjectHelper the object helper
   */
  public Wapi(ObjectHelper pObjectHelper) {
    mObjectHelper = pObjectHelper;
  }
  
  /**
   * Gets the page.
   *
   * @param pPageId the page id
   * @return the page
   */
  public Object getPage(String pPageId) {
    
    String jsonPath = JSON_DIR + pPageId + JSON_EXT;
    
    String pageJson = getJson(jsonPath);
    
    return mObjectHelper.parse(pageJson);
  }
  
  /**
   * Gets the json.
   *
   * @param pJsonPath the json path
   * @return the json
   */
  public String getJson(String pJsonPath) {
    String json = null;
    
    try (InputStream in = this.getClass().getClassLoader().getResourceAsStream(pJsonPath)) {
      json = IOUtils.toString(in, UTF_8);
    }
    catch (IOException e) {
      mLogger.error("Error loading json", e);
    }
    
    return json;
  }
  
}

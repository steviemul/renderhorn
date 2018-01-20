package occ.ssr;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * The Class Settings.
 */
public class Settings {
  
  //---------------------------------------------------------------------------
  // Constants
  //---------------------------------------------------------------------------
  private static final String NAME = "name";
  private static final String HTTP = "http";
  private static final String HOST = "host";
  private static final String PORT = "port";
  private static final String STATIC_CACHE_TIME = "staticCacheTime";
  private static final String SSL_PORT = "sslPort";
  private static final String HTTP2 = "http2";
  private static final String ENABLED = "enabled";
  private static final String PUSH_ENABLED = "pushEnabled";
  private static final String RENDERER = "renderer";
  private static final String MAIN = "main";
  private static final String STATIC_DIRS = "staticDirs";
  private static final String INIT_SCRIPTS = "initScripts";
  private static final String PACKAGE_LOCATIONS = "packageLocations";
  private static final String INIT_PACKAGES = "initPackages";
  private static final String WATCHING_SCRIPTS = "watchingScripts";
  
  //---------------------------------------------------------------------------
  // Properties
  //---------------------------------------------------------------------------
  private File mRootDirectory;
  
  /**
   * Gets the root directory.
   *
   * @return the root directory
   */
  public File getRootDirectory() {
    return mRootDirectory;
  }
  
  private String mName;
  
  /**
   * Gets the name.
   *
   * @return the name
   */
  public String getName() {
    return mName;
  }
  
  private String mHttpHost;
  
  /**
   * Gets the http host.
   *
   * @return the http host
   */
  public String getHttpHost() {
    return mHttpHost;
  }
  
  private int mHttpPort;
  
  /**
   * Gets the http port.
   *
   * @return the http port
   */
  public int getHttpPort() {
    return mHttpPort;
  }
  
  private int mHttpsPort;
  
  /**
   * Gets the https port.
   *
   * @return the https port
   */
  public int getHttpsPort() {
    return mHttpsPort;
  }
  
  private int mStaticCacheTime;
  
  /**
   * Gets the static cache time.
   *
   * @return the static cache time
   */
  public int getStaticCacheTime() {
    return mStaticCacheTime;
  }
  
  private boolean mHttp2Enabled;
  
  /**
   * Checks if is http 2 enabled.
   *
   * @return true, if is http 2 enabled
   */
  public boolean isHttp2Enabled() {
    return mHttp2Enabled;
  }
  
  private boolean mHttp2PushEnabled;
  
  /**
   * Checks if is http 2 push enabled.
   *
   * @return true, if is http 2 push enabled
   */
  public boolean isHttp2PushEnabled() {
    return mHttp2PushEnabled;
  }
  
  private String mMainScript;
  
  /**
   * Gets the main script.
   *
   * @return the main script
   */
  public String getMainScript() {
    return mMainScript;
  }
  
  private boolean mWatchingScripts;
  
  /**
   * Checks if is watching scripts.
   *
   * @return true, if is watching scripts
   */
  public boolean isWatchingScripts() {
    return mWatchingScripts;
  }
  
  private List<String> mStaticDirs;
  
  /**
   * Gets the static dirs.
   *
   * @return the static dirs
   */
  public List<String> getStaticDirs() {
    return mStaticDirs;
  }
  
  private List<String> mInitScripts;
  
  /**
   * Gets the inits the scripts.
   *
   * @return the inits the scripts
   */
  public List<String> getInitScripts() {
    return mInitScripts;
  }
  
  private List<String> mPackageLocations;
  
  /**
   * Gets the package locations.
   *
   * @return the package locations
   */
  public List<String> getPackageLocations() {
    return mPackageLocations;
  }
  
  private List<String> mInitPackages;
  
  /**
   * Gets the inits the packages.
   *
   * @return the inits the packages
   */
  public List<String> getInitPackages() {
    return mInitPackages;
  }
  
  /**
   * Instantiates a new settings.
   *
   * @param pLocation the location
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws JSONException the JSON exception
   */
  public Settings(String pLocation, File pRootDirectory) throws IOException, JSONException {
    
    mRootDirectory = pRootDirectory;
    
    try (InputStream in = Settings.class.getClassLoader().getResourceAsStream(pLocation)) {
      String contents = IOUtils.toString(in, UTF_8);
      
      JSONObject settings = new JSONObject(contents);
      
      init(settings);
    }
  }
  
  /**
   * Inits the.
   *
   * @param pSettings the settings
   */
  private void init(JSONObject pSettings) throws JSONException {
    
    mName = pSettings.getString(NAME);
    
    JSONObject http = pSettings.getJSONObject(HTTP);
    mHttpHost = http.getString(HOST);
    mHttpPort = http.getInt(PORT);
    mHttpsPort = http.getInt(SSL_PORT);
    mStaticCacheTime = http.getInt(STATIC_CACHE_TIME);
    
    JSONObject http2 = pSettings.getJSONObject(HTTP2);
    mHttp2Enabled = http2.getBoolean(ENABLED);
    mHttp2PushEnabled = http2.getBoolean(PUSH_ENABLED);
    
    JSONObject renderer = pSettings.getJSONObject(RENDERER);
    mMainScript = renderer.getString(MAIN);
    mWatchingScripts = renderer.getBoolean(WATCHING_SCRIPTS);
    
    mStaticDirs = getItemsAsList(renderer.getJSONArray(STATIC_DIRS));
    
    mInitScripts = getSortedItems(renderer.getJSONObject(INIT_SCRIPTS));
    
    mPackageLocations = getItemsAsList(renderer.getJSONArray(PACKAGE_LOCATIONS));
    
    mInitPackages = getSortedItems(renderer.getJSONObject(INIT_PACKAGES));
  }
  
  /**
   * Gets the items as list.
   *
   * @param pArray the array
   * @return the items as list
   * @throws JSONException the JSON exception
   */
  private List<String> getItemsAsList(JSONArray pArray) throws JSONException {
    
    List<String> items = new ArrayList<>();
    
    for (int i=0;i<pArray.length();i++) {
      String path = getAbsolutePath(pArray.getString(i));
      items.add(path);
    }
    
    return items;
  }
  /**
   * Gets the sorted items.
   *
   * @param pObject the object
   * @return the sorted items
   * @throws JSONException the JSON exception
   */
  private List<String> getSortedItems(JSONObject pObject) throws JSONException {
    
    JSONArray indexes = pObject.names();
    List<String> items = new ArrayList<>();
    
    for (int i=0;i<indexes.length();i++) {
      String index = String.valueOf(i+1);
      
      items.add(pObject.getString(index));
    }
    
    return items;
  }
  
  /**
   * Gets the absolute path.
   *
   * @param pRelativePath the relative path
   * @return the absolute path
   */
  private String getAbsolutePath(String pRelativePath) {
    
    File targetDir = new File(mRootDirectory, pRelativePath);
    
    return targetDir.getAbsolutePath();
  }
  
}

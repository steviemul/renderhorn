package occ.ssr.js;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.script.Bindings;
import javax.script.Invocable;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import io.undertow.server.HttpServerExchange;
import occ.ssr.Settings;
import occ.ssr.io.PackageWatcher;
import occ.ssr.js.api.Wapi;
import occ.ssr.renderer.http.Response;

/**
 * The Class Engine.
 */
public class RenderingEngine {
  
  private static final String SCRIPT_ENGINE = "javascript";
  private static final String PACKAGE_JSON = "package.json";
  private static final String NAME = "name";
  private static final String DEPENDENCIES = "dependencies";
  private static final String PEER_DEPENDENCIES = "peerDependencies";
  private static final String DIST = "dist";
  private static final String PACKAGE_EXT = ".js";
  private static final String HEAD = "</head>";
  private static final String WS_SOURCE = "globals/ws.js";
  
  private static Log mLogger = LogFactory.getLog(RenderingEngine.class);
  
  private ScriptEngineManager mManager = new ScriptEngineManager();
  private ScriptEngine mJSEngine;
  private Bindings mBindings;
  
  private final Settings mSettings;
  
  private Map<String, Library> mPackages = new LinkedHashMap<>();
  
  private static final String OCC = "occ";
  private static final String WAPI = "wapi";
  private static final String CONSOLE = "console";
  
  private Object mRenderer = null;
  private static final String RENDER_METHOD = "render";
  private static final String STATE_METHOD = "getState";
  
  private Map<String, String> mRenderedPages = new HashMap<>();
  
  private boolean mStarted = false;
  
  private PackageWatcher mPackageWatcher;
  
  /**
   * Instantiates a new engine.
   *
   * @param pSettings the settings
   */
  public RenderingEngine(Settings pSettings) throws Exception {
    
    mSettings = pSettings;
    mPackageWatcher = new PackageWatcher(pSettings);
    
    initEngine();
    initEnvironment();
    
    mPackageWatcher.startWatching();
    
    mStarted = true;
  }
  
  /**
   * Render.
   *
   * @return the string
   */
  public String render(HttpServerExchange pExchange) {
    
    String result = "";
    
    Date start = new Date();
    
    if (mSettings.isCachingPages() && mRenderedPages.containsKey(pExchange.getRequestPath())) {
      result = mRenderedPages.get(pExchange.getRequestPath());
      
      Date end = new Date();
      
      mLogger.info("Render took " + (end.getTime() - start.getTime()) + "ms (from cache)");
    }
    else {
      try {
        Invocable invocableEngine = (Invocable) mJSEngine;
        
        Response response = new Response(pExchange, mSettings);
        
        Object state = invocableEngine.invokeMethod(mRenderer, STATE_METHOD, new Object[0]);
        
        Object[] invokeArgs = {response, state};
        
        // Multiple threads shouldn't operate on the same bindings object at the same time, in case there are mutations on global/window scoped items.
        // Synchronzing seems better than creating a new bindings object and eval'ing all scripts again on each request.
        synchronized (mRenderer) {
          result = (String) invocableEngine.invokeMethod(mRenderer, RENDER_METHOD, invokeArgs);
          
          if (mSettings.isWatchingPackages()) {
            result = addNotifyingReceiver(result);
          }
        }
        
        Date end = new Date();
        
        mLogger.info("Render took " + (end.getTime() - start.getTime()) + "ms");
        
        if (mSettings.isCachingPages()) {
          mRenderedPages.put(pExchange.getRequestPath(), result);
        }
      }
      catch (NoSuchMethodException | ScriptException e) {
        mLogger.error("Error performing render", e);
      }
    }
    
    
    return result;
  }
  
  /**
   * Reload.
   * @throws Exception 
   */
  public void reload() throws Exception {
    mPackages.clear();
    initEngine(true);
    initEnvironment();
  }
  
  /**
   * Clear cache.
   */
  public void clearCache() throws Exception {
    mRenderedPages.clear();
    
    mLogger.info("Page cache cleared.");
  }
  
  /**
   * Adds the notifying receiver.
   *
   * @param pHtml the html
   * @return the string
   */
  private String addNotifyingReceiver(String pHtml) {
   
    try {
      int insertionPoint = pHtml.indexOf(HEAD);
      
      if (insertionPoint > -1) {
        String ws = "<script>" + getScriptSource(WS_SOURCE) + "</script>";
        
        pHtml = pHtml.substring(0, insertionPoint) + ws + pHtml.substring(insertionPoint);
      }
    }
    catch (Exception e) {
      mLogger.warn("Unable to add script to html", e);
    }
    
    return pHtml;
  }
  
  /**
   * Watch file.
   *
   * @param pFile the file
   */
  private void watchFile(String pFile) {
    
    if (!mStarted) {
      mPackageWatcher.addPath(pFile);
    }
  }
  
  /**
   * Inits the engine.
   */
  private void initEngine() {
    initEngine(false);
  }
  
  /**
   * Inits the engine.
   *
   * @param pReload the reload
   */
  private void initEngine(boolean pReload) {
    
    mJSEngine = mManager.getEngineByName(SCRIPT_ENGINE);
    
    if (pReload) {
      mBindings = mJSEngine.createBindings();
      
      mJSEngine.setBindings(mBindings, ScriptContext.ENGINE_SCOPE);
    }
    else {
      mBindings = mJSEngine.getBindings(ScriptContext.ENGINE_SCOPE);
    }
    
    ObjectHelper objectHelper = new ObjectHelper(mJSEngine, mBindings);
    
    Map<String, Object> context = new HashMap<>();
    
    context.put(WAPI, new Wapi(objectHelper));
    context.put(CONSOLE, new Console(mSettings.getName()));
    
    mBindings.put(OCC, context);
  }
  
  /**
   * Inits the.
   */
  private void initEnvironment() throws Exception {
    
    try {
      mLogger.info("Setting up environment");
      
      loadInitScripts();
      
      mLogger.info("Searching for available packages");
      
      scanLibraries();
      
      mLogger.info("Loading application packages");
      
      for (String initPackage : mSettings.getInitPackages()) {
        loadPackage(initPackage);
      }
      
      mRenderer = loadScript(mSettings.getMainScript());
    }
    catch (Exception e) {
      mLogger.error("Unable to setup environment", e);
      throw e;
    }
  }
  
  /**
   * Loads any initScripts specified.
   */
  private void loadInitScripts() {
    
    try {
      for (String initScript : mSettings.getInitScripts()) {
        loadScript(initScript);
      }
    }
    catch (IOException | ScriptException e) {
      e.printStackTrace();
    }
  }
  
  /**
   * Load package.
   *
   * @param pPackageName the package name
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws ScriptException the script exception
   */
  private void loadPackage(String pPackageName) throws IOException, ScriptException {
    
    Library packageToLoad = mPackages.get(pPackageName);
    
    if (packageToLoad == null || packageToLoad.isLoaded()) {
      return;
    }
    
    // Load dependencies first before we eval the package script.
    if (packageToLoad.getDependencies().size() > 0) {
    
      for (String dependency : packageToLoad.getDependencies()) {
        Library jsPackage = mPackages.get(dependency);
        
        if (jsPackage != null && !jsPackage.isLoaded()) {
          loadPackage(jsPackage.getName());
        }
        else if (jsPackage == null){
          mLogger.warn("Warning - dependency " + dependency + " not found as package. Ensure it's explicitly loaded.");
        }
      }
    }
    
    // Check again incase it was loaded by another dependency.
    if (!packageToLoad.isLoaded()) {
      try (InputStream in = new FileInputStream(packageToLoad.getPath())) {
        
        String source = IOUtils.toString(in, UTF_8);
        
        mJSEngine.eval(source);
        
        packageToLoad.setLoaded(true);
        
        mLogger.info("Successfully loaded package " + packageToLoad);
        
        watchFile(packageToLoad.getPath());
      }
      catch (ScriptException e) {
        mLogger.error("Error loading " + packageToLoad, e);
        throw e;
      }
    }
    
  }
  
  /**
   * Load script.
   *
   * @param pResourcePath the resource path
   * @return the object
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws ScriptException the script exception
   */
  private Object loadScript(String pResourcePath) throws IOException, ScriptException {
    
    Object result = null;
    
    try (InputStream in = this.getClass().getClassLoader().getResourceAsStream(pResourcePath)) {
      if (in == null) {
        throw new FileNotFoundException("Unable to locate resource " + pResourcePath);
      }
      
      String source = IOUtils.toString(in, UTF_8);
      
      result = mJSEngine.eval(source);
      
      mLogger.info("Successfully loaded script " + pResourcePath);
    }
    
    return result;
  }
  
  /**
   * Gets the script source.
   *
   * @param pPath the path
   * @return the script source
   * @throws IOException Signals that an I/O exception has occurred.
   */
  private String getScriptSource(String pPath) throws IOException {
    
    try (InputStream in = this.getClass().getClassLoader().getResourceAsStream(pPath)) {
      if (in == null) {
        throw new FileNotFoundException("Unable to locate resource " + pPath);
      }
      
      String source = IOUtils.toString(in, UTF_8);
      
      return source;
    }
  }
  
  /**
   * Scans specified package locations for potential packages 
   * that may need to be loaded.
   */
  private void scanLibraries() {
    
    for (String packageLocation : mSettings.getPackageLocations()) {
      File packageDirectory = new File(packageLocation);
      
      scanDirectory(packageDirectory);
    }
    
  }
  
  /**
   * Scans the specified directory and subdirectories for
   * packages that may be loaded later. These are identified
   * by the existence of a package.json in the directory.
   *
   * @param pDirectory the directory
   */
  private void scanDirectory(File pDirectory) {
    
    File packageJson = new File(pDirectory, PACKAGE_JSON);
    
    if (packageJson.exists()) {
      try (InputStream in = new FileInputStream(packageJson)) {
        String contents = IOUtils.toString(in, UTF_8);
        
        JSONObject json = new JSONObject(contents);
        
        String packageName = json.getString(NAME);
        
        Library jsPackage = mPackages.containsKey(packageName) ? mPackages.get(packageName) : new Library(packageName);
        
        processDependencies(jsPackage, json.optJSONObject(DEPENDENCIES));
        processDependencies(jsPackage, json.optJSONObject(PEER_DEPENDENCIES));
        
        // We're assuming the script we load will be in a "dist" directory, may need to make this more clever.
        File distDir = new File(pDirectory, DIST);
        
        if (distDir.exists()) {
          File jsFile = new File(distDir, packageName + PACKAGE_EXT);
          
          if (jsFile.exists()) {
            jsPackage.setPath(jsFile.getPath());
            mPackages.put(packageName, jsPackage);
          }
        }
      }
      catch (IOException | JSONException e) {
        mLogger.error("Error loading " + packageJson.getPath() + ".", e);
      }
    }
    else {
      File[] children = pDirectory.listFiles();
      
      if (children != null) {
        for (File child : children) {
          if (child.isDirectory()) {
            scanDirectory(child);
          }
        }
      }
    }
  }
  
  /**
   * Process dependencies.
   *
   * @param pLibrary the library
   * @param pDependencies the dependencies
   * @throws JSONException 
   */
  private void processDependencies(Library pLibrary, JSONObject pDependencies) throws JSONException {
    
    if (pDependencies != null) {
      JSONArray names = pDependencies.names();
      
      if (names != null) {
        for (int i=0;i<names.length();i++) {
          pLibrary.addDependency(names.getString(i));
        }
      }
    }
  }
}

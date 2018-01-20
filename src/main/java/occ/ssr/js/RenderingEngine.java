package occ.ssr.js;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.File;
import java.io.FileInputStream;
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
import occ.ssr.data.Wapi;
import occ.ssr.renderer.http.Response;

/**
 * The Class Engine.
 */
public class RenderingEngine {
  
  private static final String SCRIPT_ENGINE = "javascript";
  private static final String PACKAGE_JSON = "package.json";
  private static final String NAME = "name";
  private static final String DEPENDENCIES = "dependencies";
  private static final String DIST = "dist";
  private static final String PACKAGE_EXT = ".js";
  
  private static Log mLogger = LogFactory.getLog(RenderingEngine.class);
  
  private ScriptEngineManager mManager = new ScriptEngineManager();
  private ScriptEngine mJSEngine;
  private Bindings mBindings;
  
  private final Settings mSettings;
  
  private Map<String, Dependency> mPackages = new LinkedHashMap<>();
  
  private static final String OCC = "occ";
  private static final String WAPI = "wapi";
  private static final String CONSOLE = "console";
  
  private Object mRenderer = null;
  private static final String RENDER_METHOD = "render";
  
  /**
   * Instantiates a new engine.
   *
   * @param pSettings the settings
   */
  public RenderingEngine(Settings pSettings) throws Exception {
    
    mSettings = pSettings;
    
    initEngine();
    initEnvironment();
  }
  
  /**
   * Render.
   *
   * @return the string
   */
  public String render(HttpServerExchange pExchange) {
    
    String result = "";
    
    Date start = new Date();
    
    try {
      Invocable invocableEngine = (Invocable) mJSEngine;
      
      Response response = new Response(pExchange, mSettings);
      
      Object[] invokeArgs = {response};
      
      result = (String) invocableEngine.invokeMethod(mRenderer, RENDER_METHOD, invokeArgs);
      
      Date end = new Date();
      
      mLogger.info("Render took " + (end.getTime() - start.getTime()) + "ms");
    }
    catch (NoSuchMethodException | ScriptException e) {
      mLogger.error("Error performing render", e);
    }
    
    return result;
  }
  
  /**
   * Reload.
   */
  public void reload() throws Exception {
    
    mLogger.info("Reloading Environment");
    
    try {
      initEnvironment();  
    }
    catch (Exception e) {
      mLogger.error("Unable to reload environment.");
      throw e;
    }
  }
  
  /**
   * Inits the engine.
   */
  private void initEngine() {
    
    mJSEngine = mManager.getEngineByName(SCRIPT_ENGINE);
    
    mBindings = mJSEngine.getBindings(ScriptContext.ENGINE_SCOPE);
    
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
      
      loadEnvironment();
      
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
   * Load environment.
   */
  private void loadEnvironment() {
    
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
    
    Dependency packageToLoad = mPackages.get(pPackageName);
    
    if (packageToLoad == null || packageToLoad.isLoaded()) {
      return;
    }
    
    if (packageToLoad.getDependencies().size() > 0) {
    
      for (String dependency : packageToLoad.getDependencies()) {
        Dependency jsPackage = mPackages.get(dependency);
        
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
        
        if (mSettings.isWatchingScripts()) {
          
        }
        
        mLogger.info("Successfully loaded package " + packageToLoad);
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
      String source = IOUtils.toString(in, UTF_8);
      
      result = mJSEngine.eval(source);
      
      mLogger.info("Successfully loaded script " + pResourcePath);
    }
    
    return result;
  }
  
  /**
   * Scan libraries.
   */
  private void scanLibraries() {
    
    for (String packageLocation : mSettings.getPackageLocations()) {
      File packageDirectory = new File(packageLocation);
      
      scanDirectory(packageDirectory);
    }
    
  }
  
  /**
   * Scan directory.
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
        
        Dependency jsPackage = mPackages.containsKey(packageName) ? mPackages.get(packageName) : new Dependency(packageName);
        
        JSONObject dependencies = json.optJSONObject(DEPENDENCIES);
        
        if (dependencies != null) {
          JSONArray names = dependencies.names();
          
          if (names != null) {
            for (int i=0;i<names.length();i++) {
              jsPackage.addDependency(names.getString(i));
            }
          }
        }
        
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
      
      for (File child : children) {
        if (child.isDirectory()) {
          scanDirectory(child);
        }
      }
    }
  }
}

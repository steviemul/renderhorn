package occ.ssr.io;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONObject;

import occ.ssr.Registry;
import occ.ssr.Settings;

/**
 * The Class PackageWatcher.
 */
public class PackageWatcher implements Runnable {

  //--------------------------------------------------------------------------------------
  // Member variables
  //--------------------------------------------------------------------------------------
  private static Log mLogger = LogFactory.getLog(PackageWatcher.class);
  private static final String JS_EXTENSION = ".js";
  private final WatchService mWatcher;
  private Thread mRunner;
  private AtomicBoolean mWatching = new AtomicBoolean(false);
  
  private boolean mWatchingEnabled = false;
  
  /**
   * Instantiates a new package watcher.
   *
   * @param mSettings the m settings
   * @throws Exception the exception
   */
  public PackageWatcher(Settings pSettings) throws Exception {
    
    mWatchingEnabled = pSettings.isWatchingPackages();
    
    try {
      mWatcher = FileSystems.getDefault().newWatchService();
    }
    catch (IOException e) {
      mLogger.error("Unable to start watch service.", e);
      throw e;
    }
  }
  
  /**
   * Adds the path.
   *
   * @param pPath the path
   */
  public void addPath(String pPath) {
    
    if (mWatchingEnabled) {
      Path target = Paths.get(pPath);
      
      try {
        if (target.getParent() != null) {
          target.getParent().register(mWatcher, 
              StandardWatchEventKinds.ENTRY_MODIFY, 
              StandardWatchEventKinds.ENTRY_CREATE, 
              StandardWatchEventKinds.ENTRY_DELETE);
        }
      }
      catch (IOException e) {
        mLogger.warn("Unable to watch for changes on file " + pPath, e);
      }
    }
    
  }
  
  /* (non-Javadoc)
   * @see java.lang.Runnable#run()
   */
  @SuppressWarnings("unchecked")
  public void run() {
    
    mWatching.set(true);
    
    while (mWatching.get()) {
      WatchKey key = null;
          
      try {
        key = mWatcher.take();
      } 
      catch (InterruptedException x) {
        return;
      }
      
      if (key != null) {
        for (WatchEvent<?> event : key.pollEvents()) {
          WatchEvent<Path> ev = (WatchEvent<Path>)event;
          
          String filename = ev.context().toString();
          
          if (filename.endsWith(JS_EXTENSION)) {
            try {
              Registry.getRenderingEngine().reload();
              
              JSONObject message = new JSONObject();
              
              message.put("reload", true);
              
              Registry.getNotifyingServer().broadcastMessage(message.toString());
            }
            catch (Exception e) {
              mLogger.error("Unable to reload engine.", e);
            }
          }
        }
        
        key.reset();
      }
      
    }
    
    mLogger.info("Stopping watching.");
  }
  
  /**
   * Start watching.
   */
  public void startWatching() {
    if (mWatchingEnabled) {
      mRunner = new Thread(this);
      mRunner.setDaemon(true);
      
      mRunner.start();
      
      mLogger.info("Watching for changes");
    }
  }
  
  /**
   * Stop watching.
   */
  public void stopWatching() {
    mWatching.set(false);
  }
}

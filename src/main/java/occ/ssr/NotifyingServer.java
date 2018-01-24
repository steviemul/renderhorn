package occ.ssr;

import static io.undertow.Handlers.path;
import static io.undertow.Handlers.websocket;

import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import io.undertow.Undertow;
import io.undertow.websockets.WebSocketConnectionCallback;
import io.undertow.websockets.core.WebSocketChannel;
import io.undertow.websockets.spi.WebSocketHttpExchange;
import io.undertow.websockets.core.WebSockets;

/**
 * The Class NotifyingServer.
 */
public class NotifyingServer {

  //--------------------------------------------------------------------------------------------
  // Member variables
  //--------------------------------------------------------------------------------------------
  private static Log mLogger = LogFactory.getLog(NotifyingServer.class);
  private final Settings mSettings;
  private final static String PATH = "/ws";
  private Set<WebSocketChannel> mChannels = new LinkedHashSet<>();
  
  /**
   * Instantiates a new notifying server.
   *
   * @param pSettings the settings
   */
  public NotifyingServer(Settings pSettings) {
    mSettings = pSettings;
  }
  
  /**
   * Start server.
   */
  public void start() {
    
    if (mSettings.getNotifyingPort() > 0 && mSettings.isWatchingPackages()) {
      
      Undertow notifyingServer = Undertow.builder()
        .addHttpListener(mSettings.getNotifyingPort(), mSettings.getHttpHost())
        .setHandler(path().addPrefixPath(PATH, websocket(new WebSocketConnectionCallback() {

          @Override
          public void onConnect(WebSocketHttpExchange exchange, WebSocketChannel channel) {
            mChannels.add(channel);
          }
          
        }))).build();
      
      notifyingServer.start();
      
      mLogger.info("Notifying Server listening on port " + mSettings.getNotifyingPort());
    }
    
  }
  
  /**
   * Broadcast message.
   *
   * @param pMessage the message
   */
  public void broadcastMessage(String pMessage) {
    
    for (WebSocketChannel channel : mChannels) {
      WebSockets.sendText(pMessage, channel, null);
    }
  }
}

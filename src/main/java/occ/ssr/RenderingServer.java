package occ.ssr;

import static io.undertow.servlet.Servlets.defaultContainer;
import static io.undertow.servlet.Servlets.deployment;
import static io.undertow.servlet.Servlets.servlet;

import java.io.InputStream;
import java.security.KeyStore;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import io.undertow.Handlers;
import io.undertow.Undertow;
import io.undertow.Undertow.Builder;
import io.undertow.UndertowOptions;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.server.handlers.PathHandler;
import io.undertow.server.handlers.encoding.EncodingHandler;
import io.undertow.servlet.api.DeploymentInfo;
import io.undertow.servlet.api.DeploymentManager;
import io.undertow.util.Headers;
import occ.ssr.http.ReverseProxyHandler;
import occ.ssr.http.StaticHandler;
import occ.ssr.js.RenderingEngine;

/**
 * The Class RenderServlet.
 */
public class RenderingServer {

  private static Log mLogger = LogFactory.getLog(RenderingServer.class);

  private final RenderingEngine mEngine;
  private final Settings mSettings;
  private final static String API_PATH = "/api";

  /**
   * Instantiates a new rendering server.
   *
   * @param pEngine
   *          the engine
   */
  public RenderingServer(RenderingEngine pEngine, Settings pSettings) {
    mEngine = pEngine;
    mSettings = pSettings;
  }

  /**
   * The main method.
   *
   * @param args
   *          the arguments
   */
  public void start() throws Exception {

    // Setup some request handlers.
    StaticHandler resourceHandler = new StaticHandler(mSettings);
    HttpHandler operationsHandler = buildOperationsRestApp();
    ReverseProxyHandler proxyHandler = new ReverseProxyHandler(mSettings);
    
    final PathHandler apiHandler = Handlers.path();

    apiHandler.addPrefixPath(API_PATH, operationsHandler);

    // Create our ssl context for https
    SSLContext sslContext = SSLContext.getInstance("TLS");

    sslContext.init(getKeyManagers(), null, null);

    // This is just plain connection server, this just redirects to https
    Undertow server = Undertow.builder().addHttpListener(mSettings.getHttpPort(), mSettings.getHttpHost())
      .setHandler(new HttpHandler() {

        @Override
        public void handleRequest(final HttpServerExchange exchange) throws Exception {

          String path = exchange.getRequestPath();
          HttpHandler redirect = Handlers
              .redirect("https://" + mSettings.getHttpHost() + ":" + mSettings.getHttpsPort() + path);

          redirect.handleRequest(exchange);
        }
        }).build();

    Builder sslServerBuilder = Undertow.builder();

    // enable http2 if specified.
    if (mSettings.isHttp2Enabled()) {
      sslServerBuilder.setServerOption(UndertowOptions.ENABLE_HTTP2, true);
    }

    HttpHandler mainHandler = new HttpHandler() {
      @Override
      public void handleRequest(final HttpServerExchange exchange) throws Exception {

        String resourcePath = exchange.getRequestPath();

        if (resourceHandler.accepts(resourcePath)) {
          resourceHandler.handleRequest(exchange);
        }
        else if (proxyHandler.accepts(resourcePath)) {
          proxyHandler.handleRequest(exchange);
        }
        else if (resourcePath.startsWith(API_PATH)) {
          apiHandler.handleRequest(exchange);
        } else {
          exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "text/html");
          String response = mEngine.render(exchange);

          exchange.getResponseSender().send(response);
        }
      }
    };

    // gzipping handler
    HttpHandler encodingHandler = new EncodingHandler.Builder().build(null).wrap(mainHandler);

    // This is our main server, this handles all requests.
    Undertow sslServer = sslServerBuilder
        .addHttpsListener(mSettings.getHttpsPort(), mSettings.getHttpHost(), sslContext).setHandler(encodingHandler)
        .build();

    sslServer.start();
    server.start();

    mLogger.info("Started server at http://" + mSettings.getHttpHost() + ":" + mSettings.getHttpPort());
    mLogger.info("Started SSL server at https://" + mSettings.getHttpHost() + ":" + mSettings.getHttpsPort());

    System.out.println("\r\n--------------------------------");
    System.out.println(" Hit ^C to exit.\r\n");
  }

  /**
   * Builds the operations rest app.
   *
   * @return the http handler
   * @throws Exception
   *           the exception
   */
  private HttpHandler buildOperationsRestApp() throws Exception {

    DeploymentInfo servletBuilder = deployment().setClassLoader(RenderingServer.class.getClassLoader())
        .setContextPath(API_PATH).setDeploymentName("api.war")
        .addServlet(servlet("Jersey", org.glassfish.jersey.servlet.ServletContainer.class)
            .addInitParam("javax.ws.rs.Application", "occ.ssr.rest.OperationsApplication")
            .addInitParam("jersey.config.server.provider.packages", "com.fasterxml.jackson.jaxrs.json")
            .addMapping("/*"));

    DeploymentManager manager = defaultContainer().addDeployment(servletBuilder);
    manager.deploy();

    HttpHandler servletHandler = manager.start();

    return servletHandler;
  }

  /**
   * Gets the key managers.
   *
   * @return the key managers
   */
  private KeyManager[] getKeyManagers() {
    try (
        InputStream keystore = RenderingServer.class.getClassLoader().getResourceAsStream(mSettings.getSSLKeystore())) {
      KeyStore keyStore = KeyStore.getInstance(mSettings.getSSLKeystoreType());

      String keystorePwd = mSettings.getSSLKeystorePwd();

      keyStore.load(keystore, keystorePwd.toCharArray());

      KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());

      keyManagerFactory.init(keyStore, keystorePwd.toCharArray());

      return keyManagerFactory.getKeyManagers();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}

package occ.ssr.rest;

import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.core.Application;

/**
 * The Class OperationsApplication.
 */
public class OperationsApplication extends Application {

  /** The classes. */
  private Set<Class<?>> classes = new HashSet<>();
  
  /**
   * Instantiates a new operations application.
   */
  public OperationsApplication() {
    classes.add(OperationsEndpoint.class);
  }
  
  /* (non-Javadoc)
   * @see javax.ws.rs.core.Application#getClasses()
   */
  @Override
  public Set<Class<?>> getClasses() {
    return classes;
  }
}

package occ.ssr.js;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * The Class Library.
 */
public class Library {
  
  //-------------------------------------------------------------------
  // Member variables
  //-------------------------------------------------------------------
  private String mName;
  private boolean mLoaded;
  private String mPath;
  private Set<String> mDependencies = new LinkedHashSet<>();
  
  /**
   * Instantiates a new library.
   *
   * @param pName the name
   */
  public Library(String pName) {
    mName = pName;
  }
  
  /**
   * Gets the name.
   *
   * @return the name
   */
  public String getName() {
    return mName;
  }
  
  /**
   * Checks if is loaded.
   *
   * @return true, if is loaded
   */
  public boolean isLoaded() {
    return mLoaded;
  }
  
  /**
   * Sets the loaded.
   *
   * @param pLoaded the new loaded
   */
  public void setLoaded(boolean pLoaded) {
    mLoaded = pLoaded;
  }
  
  /**
   * Gets the path.
   *
   * @return the path
   */
  public String getPath() {
    return mPath;
  }
  
  /**
   * Sets the path.
   *
   * @param pPath the new path
   */
  public void setPath(String pPath) {
    mPath = pPath;
  }

  /**
   * Gets the dependencies.
   *
   * @return the dependencies
   */
  public Set<String> getDependencies() {
    return mDependencies;
  }
  
  /**
   * Adds the dependency.
   *
   * @param pDependency the dependency
   */
  public void addDependency(String pDependency) {
    mDependencies.add(pDependency);
  }
  
  /* (non-Javadoc)
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((mDependencies == null) ? 0 : mDependencies.hashCode());
    result = prime * result + ((mName == null) ? 0 : mName.hashCode());
    return result;
  }

  /* (non-Javadoc)
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    Library other = (Library) obj;
    if (mDependencies == null) {
      if (other.mDependencies != null)
        return false;
    } else if (!mDependencies.equals(other.mDependencies))
      return false;
    if (mName == null) {
      if (other.mName != null)
        return false;
    } else if (!mName.equals(other.mName))
      return false;
    return true;
  }

  /* (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return "Dependency [mName=" + mName + ", mPath=" + mPath + "]";
  }
  
  
}

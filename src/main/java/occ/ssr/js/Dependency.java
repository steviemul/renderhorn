package occ.ssr.js;

import java.util.LinkedHashSet;
import java.util.Set;

public class Dependency {
  
  private String mName;
  private boolean mLoaded;
  private String mPath;
  private Set<String> mDependencies = new LinkedHashSet<>();
  
  public Dependency(String pName) {
    mName = pName;
  }
  
  public String getName() {
    return mName;
  }
  
  public boolean isLoaded() {
    return mLoaded;
  }
  
  public void setLoaded(boolean pLoaded) {
    mLoaded = pLoaded;
  }
  
  public String getPath() {
    return mPath;
  }
  
  public void setPath(String pPath) {
    mPath = pPath;
  }

  public Set<String> getDependencies() {
    return mDependencies;
  }
  
  public void addDependency(String pDependency) {
    mDependencies.add(pDependency);
  }
  
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((mDependencies == null) ? 0 : mDependencies.hashCode());
    result = prime * result + ((mName == null) ? 0 : mName.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    Dependency other = (Dependency) obj;
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

  @Override
  public String toString() {
    return "Dependency [mName=" + mName + ", mPath=" + mPath + "]";
  }
  
  
}

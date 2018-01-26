package occ.ssr.js.api;

import java.util.function.Function;

public class Timer {
	
	@SuppressWarnings("rawtypes")
	public void run(Function pCallback, long pDelay) {
		
		Runnable callable = new  Runnable() {

      @SuppressWarnings("unchecked")
      @Override
      public void run() {
        
        try {
          Thread.sleep(pDelay);
        }
        catch (InterruptedException e) {
          
        }
        
        pCallback.apply(null);
      }
		  
		};
		
		Thread runner = new Thread(callable);
		
		runner.start();
	}
}

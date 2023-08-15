package bgu.spl.mics;

import java.util.concurrent.TimeUnit;

/**
 * A Future object represents a promised result - an object that will
 * eventually be resolved to hold a result of some operation. The class allows
 * Retrieving the result once it is available.
 *
 * Only private methods may be added to this class.
 * No public constructor is allowed except for the empty constructor.
 */
public class Future<T> {
	private T resulta;
	/**
	 * This should be the only public constructor in this class.
	 */
	public Future() {
		resulta= null;
	}

	/**
	 * retrieves the result the Future object holds if it has been resolved.
	 * This is a blocking method! It waits for the computation in case it has
	 * not been completed.
	 * <p>
	 * @return return the result of type T if it is available, if not wait until it is available.
	 *
	 *
	 * 	* @pre resulta == null;
	 * 	* @inv  resulta==null && thread is waiting
	 * 	* @post resulta!=null && thread is terminated
	 *      */

	public T get() throws InterruptedException {
		synchronized (this) {
			while (resulta == null)
				wait();
		}
		return resulta;
	}

	/**
	 * Resolves the result of this Future object.
	 * @param  result;
	 * @pre resulta == null;
	 * @post this.resulta==result;
	 */
	public void resolve (T result) {
		synchronized (this) {
			this.resulta = result;
			notifyAll();
		}
	}

	/**
	 * @return true if this object has been resolved, false otherwise
	 * 	* @post true if(this.resulta=!null) && false if (this.resulta==null)
	 */
	public boolean isDone() {
		if (resulta != null)
			return  true;
		return false;
	}

	/**
	 * retrieves the result the Future object holds if it has been resolved,
	 * This method is non-blocking, it has a limited amount of time determined
	 * by {@code timeout}
	 * <p>
	 * @param timout 	the maximal amount of time units to wait for the result.
	 * @param unit		the {@link TimeUnit} time units to wait.
	 * @return return the result of type T if it is available, if not,
	 * 	       wait for {@code ti	meout} TimeUnits {@code unit}. If time has
	 *         elapsed, return null.
	 * * @pre timout>0
	 * * @inv resulta == null && c<timout
	 * * @post resulta</T>==resulta || resulta==null
	 */
	public T get(long timeout, TimeUnit unit) {//to implement with sycn
		try {
			unit.sleep(timeout);
		}
		catch (InterruptedException e){}
		return resulta;
	}
//	private T getresulta(){
//		return resulta;
//	}
}

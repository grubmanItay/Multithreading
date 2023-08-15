package bgu.spl.mics;

import java.util.HashMap;
import java.util.Vector;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * The {@link MessageBusImpl class is the implementation of the MessageBus interface.
 * Write your implementation here!
 * Only private fields and methods can be added to this class.
 */
public class MessageBusImpl implements MessageBus {
	private HashMap<Event,Future> openFutures; //every hashmap should prob be sync..
	private HashMap<MicroService,BlockingQueue<Message>> micros;
	private HashMap<Class<? extends Event>, Vector<MicroService>> events;
	private HashMap<Class<? extends Broadcast>, Vector<MicroService>> broadcasts;
	private HashMap<Class<? extends Event>, Integer> rbcounter;
	//private BlockingQueue<Message> massagesQueue;
	public HashMap getMicros() {
		return micros;
	}
	public HashMap getEvents() {
		return events;
	}
	public HashMap getBroadcasts() {return broadcasts;}
	public HashMap getOpenFutures() {return openFutures;}

	public MessageBusImpl(){
		openFutures=new HashMap<>();
		micros=new HashMap<>();
		events=new HashMap<>();
		broadcasts=new HashMap<>();
		rbcounter=new HashMap<>();
	}

	/**
	 * Subscribes {@code m} to receive {@link Event}s of type {@code type}.
	 * <p>
	 * @param <T>  The type of the result expected by the completed event.
	 * @param type The type to subscribe to,
	 * @param m    The subscribing micro-service.
	 * @post m in events.get(type);
	 */

	@Override
	public <T> void subscribeEvent(Class<? extends Event<T>> type, MicroService m) {
		synchronized (events) {
			if (events.get(type) == null){
				Vector<MicroService> vector=new Vector<MicroService>();
				vector.add(m);
				events.put(type,vector);
			}
			else{
				events.get(type).add(m);
			}
			events.notifyAll();
		}
	}
	/**
	 * Subscribes {@code m} to receive {@link Broadcast}s of type {@code type}.
	 * <p>
	 * @param type     The type to subscribe to.
	 * @param m        The subscribing micro-service.
	 * @post m in broadcasts.get(type);
	 */
	@Override
	public void subscribeBroadcast(Class<? extends Broadcast> type, MicroService m) {
		synchronized (broadcasts) {
			if (broadcasts.get(type) == null){
				Vector<MicroService> vector=new Vector<MicroService>();
				vector.add(m);
				broadcasts.put(type,vector);
			}
			else{
				broadcasts.get(type).add(m);
			}
			broadcasts.notifyAll();
		}
	}
	/**
	 * Notifies the MessageBus that the event {@code e} is completed and its
	 * result was {@code result}.
	 * When this method is called, the message-bus will resolve the {@link Future}
	 * object associated with {@link Event} {@code e}.
	 * <p>
	 * @param <T>    The type of the result expected by the completed event.
	 * @param e      The completed event.
	 * @param result The resolved result of the completed event.
	 * @pre openfutures.get(e) !=null
	 * @post openfutures.get(e).isDone()
	 */
	@Override
	public <T> void complete(Event<T> e, T result) {
		openFutures.get(e).resolve(result);
		//need to remove closed fut from the openfutures map(?)

	}
	/**
	 * Adds the {@link Broadcast} {@code b} to the message queues of all the
	 * micro-services subscribed to {@code b.getClass()}.
	 * <p>
	 * @param b    The message to added to the queues.
	 * @pre        b!=null
	 * @post   b in every microservice massage que
	 */
	@Override
	public void sendBroadcast(Broadcast b) {
		synchronized (broadcasts) {
		if (broadcasts.get(b.getClass()) != null) {
			Vector<MicroService> v = broadcasts.get(b.getClass());
				for (int i = 0; i < v.size(); i++)
					micros.get(v.get(i)).add(b);
//				v.notifyAll();
			}
		}
	}
	/**
	 * Adds the {@link Event} {@code e} to the message queue of one of the
	 * micro-services subscribed to {@code e.getClass()} in a round-robin
	 * fashion. This method should be non-blocking.
	 * <p>
	 * @param <T>      The type of the result expected by the event and its corresponding future object.
	 * @param e        The event to add to the queue.
	 * @return {@link Future<T>} object to be resolved once the processing is complete,
	 *            null in case no micro-service has subscribed to {@code e.getClass()}.
	 * @pre e !=null
	 * @post this.getEvents().get(type.e) contanins e
	 * @post @pre(this.rbcounter +1)% size =this.rbcounter
	 */
	@Override
	public <T> Future<T> sendEvent(Event<T> e) {
		synchronized (events) {
			int c;
			if (rbcounter.get(e.getClass()) == null) {
				c = 0;
				rbcounter.put(e.getClass(), c);
			} else {
				c = rbcounter.get(e.getClass());
				if (events.get(e.getClass()).size() - 1  > c)
					c++;
				else
					c = 0;
				rbcounter.put(e.getClass(), c);
			}
			Vector vector = events.get(e.getClass());
			if (vector.isEmpty())
				System.out.println("no available conference to publish the result");
//		MicroService ms = vector.get(c);
			else
				micros.get(vector.get(c)).add(e);
		}
		synchronized (openFutures) {
			Future f = new Future<>();
			openFutures.put(e, f);
			return f;
		}
	}

	/**
	 * Allocates a message-queue for the {@link MicroService} {@code m}.
	 * <p>
	 * @param m the micro-service to create a queue for.
	 * @pre m != null
	 * @pre getmicros().get(m) is null
	 * @post getmicros().get(m) is not null
	 */
	@Override
	public void register(MicroService m) {
		synchronized (micros) {
			BlockingQueue<Message> queue = new LinkedBlockingQueue<Message>();
			micros.put(m, queue);
			micros.notifyAll();
		}
	}
	/**
	 * Removes the message queue allocated to {@code m} via the call to
	 * {@link #register(bgu.spl.mics.MicroService)} and cleans all references
	 * related to {@code m} in this message-bus. If {@code m} was not
	 * registered, nothing should happen.
	 * <p>
	 * @param m the micro-service to unregister.
	 * @pre m!=null
	 * @post getmicros().get(m) contains  m
	 */
	@Override
	public void unregister(MicroService m) {
		synchronized (micros){
		if (micros.get(m) != null) {
			micros.remove(m);
		}
//				if (events.containsValue(m)) {
		synchronized (events) {
			for (Class<? extends Event> key : events.keySet()) {
				if (events.get(key).contains(m))
					events.get(key).remove(m);
			}
		}
//				}
//				for ( :broadcasts.keySet()) {
//					if (broadcasts.containsValue(m))
		synchronized (broadcasts) {
			for (Class<? extends Broadcast> key : broadcasts.keySet())
				if (broadcasts.get(key).contains(m))
					broadcasts.get(key).remove(m);
		}
	}
//			this.notifyAll();
}
	/**
	 * Using this method, a <b>registered</b> micro-service can take message
	 * from its allocated queue.
	 * This method is blocking meaning that if no messages
	 * are available in the micro-service queue it
	 * should wait until a message becomes available.
	 * The method should throw the {@link IllegalStateException} in the case
	 * where {@code m} was never registered.
	 * <p>
	 * @param m The micro-service requesting to take a message from its message
	 *          queue.
	 * @return The next message in the {@code m}'s queue (blocking).
	 * @throws InterruptedException if interrupted while waiting for a message
	 *                              to became available.
	 * @pre m!=null
	 * @pre m queue is alocated
	 * @inv m queue is empty iff m is waiting
	 * @post @return== @pre this.getMicros().get(m).get(0)
	 */
	@Override
	public Message awaitMessage(MicroService m) throws InterruptedException {
		if (!micros.containsKey(m))
			throw new InterruptedException("was never registerd");
		BlockingQueue<Message> messagesQueue = micros.get(m);
		Message s = messagesQueue.take();//.take instead of .poll and delete while-wait-notifyall
		//Callback.call(m.getClass());
		//s.getClass().callback();

		return s;
	}



}
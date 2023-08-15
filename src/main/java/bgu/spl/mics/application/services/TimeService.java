package bgu.spl.mics.application.services;

import bgu.spl.mics.MessageBus;
import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.messages.GetStarted;
import bgu.spl.mics.application.messages.KillAllBroadcast;
import bgu.spl.mics.application.messages.TickBroadcast;

/**
 * TimeService is the global system timer There is only one instance of this micro-service.
 * It keeps track of the amount of ticks passed since initialization and notifies
 * all other micro-services about the current time tick using {@link TickBroadcast}.
 * This class may not hold references for objects which it is not responsible for.
 * 
 * You can add private fields and public methods to this class.
 * You MAY change constructor signatures and even add new public constructors.
 */
public class TimeService extends MicroService{
	private int speed;
	private int duration;
	private int ticks = 1;


	public TimeService(int speed, int duration,MessageBus msgBus) {
		super("TimeService", msgBus);
		this.speed = speed;
		this.duration = duration;
	}

	public void start(){
		while (ticks < duration)
		{
			try{
				Thread.sleep(speed);
			}
			catch (InterruptedException e){}

			sendBroadcast(new TickBroadcast(ticks));
			System.out.println(ticks);
			ticks++;
		}
		sendBroadcast(new KillAllBroadcast());
	}

	@Override
	protected void initialize() {
		this.msgBus.register(this);
		subscribeBroadcast(GetStarted.class, ev-> {
			System.out.println("Starting");
			this.start();
		});
//		sendEvent(timeTickEvent);

		subscribeBroadcast(KillAllBroadcast.class, ev->{
			this.terminate();
		});
	}

	public boolean isTerminated() {
		if (this.ticks >= this.duration)
			return true;
		else
			return false;
	}
}

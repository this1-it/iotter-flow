package it.thisone.iotter.eventbus;

import java.io.Serializable;
import java.util.concurrent.ThreadPoolExecutor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.eventbus.AsyncEventBus;
import com.google.common.eventbus.EventBus;

/**
 * https://blog.rasc.ch/?p=1853
 * 
 * http://kaczanowscy.pl/tomek/2014-06/testing-google-eventbus
 * 
 * postprocessor
 * http://pmeade.blogspot.it/2012/06/using-guava-eventbus-with-spring.html
 * 
 * Java event bus library comparison http://codeblock.engio.net/37/
 * 
 * There is a good reason for this additional, and seemingly unnecessary, class.
 * It allows us to follow the "mock only the types you own" and also makes our
 * code more resilient to change (i.e. if we change EventBus to something else,
 * there is a fat chance the only thing we need to update is our thin wrapper).
 * 
 * @author tisone
 * 
 */

//@Service
public class EventBusWrapper  implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;


	private EventBus eventBus;
	

	//private AsyncEventBus asyncEventBus;

	
	private EventBusWrapper(EventBus eventBus) {
		this.eventBus = eventBus;
		//this.asyncEventBus = asyncEventBus;
	}

	public EventBusWrapper() {
		super();
	}

	public static EventBusWrapper localEventBus() {
		return new EventBusWrapper(new EventBus());
	}	
	
//	public static EventBusWrapper localEventBus(ThreadPoolExecutor executor) {
//		return new EventBusWrapper(new EventBus());
//	}

	public void post(Object event) {
		eventBus.post(event);
	}

	public void register(Object object) {
		eventBus.register(object);
	}

	public void unregister(Object object) {
		try {
			eventBus.unregister(object);
		} catch (IllegalArgumentException e) {
		}
	}


//	public void postAsync(Object event) {
//		asyncEventBus.post(event);
//	}
//
//
//	public void registerAsync(Object object) {
//		asyncEventBus.register(object);
//	}
//
//
//	public void unregisterAsync(Object object) {
//		try {
//			asyncEventBus.unregister(object);
//		} catch (IllegalArgumentException e) {
//		}
//	}

	public EventBus getEventBus() {
		return eventBus;
	}
	
}

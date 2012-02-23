package ec.nem.bluenet.net;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;

public abstract class Layer {
	public static final int MESSAGE_SEND = 0;
	public static final int MESSAGE_RECV = 1;
	public static final int MESSAGE_ERROR = 2;
	
	/** Handles input to this layer from an upper layer */
	protected Handler hReceiveFromAbove;
	/** Handles input to this layer from a lower layer */
	protected Handler hReceiveFromBelow;
	/** Handles output to the layer above this one */
	protected Handler hSendAbove;
	/** Handles output to the layer below this one */
	protected Handler hSendBelow;
	
	/** Processes packets as they flow down the stack */
	protected HandlerThread downThread;
	/** Processes packets as they flow up the stack */
	protected HandlerThread upThread;
	
	public Layer() {
		downThread = new HandlerThread("DownThread");
		upThread = new HandlerThread("UpThread");
		
		downThread.start();
		upThread.start();
		
		hReceiveFromAbove = new Handler(downThread.getLooper()) {
			@Override
			public void handleMessage(Message msg) {
				handleMessageFromAbove(msg);
			}
		};
		
		hReceiveFromBelow = new Handler(upThread.getLooper()) {
			@Override
			public void handleMessage(Message msg) {
				handleMessageFromBelow(msg);
			}
		};
	}
	
	/** Gets the handler to which data should be sent by the layer above this one */
	public Handler getAboveHandler() {
		return hReceiveFromAbove;
	}

	/** Gets the handler to which data should be sent by the layer below this one */
	public Handler getBelowHandler() {
		return hReceiveFromBelow;
	}
	
	/** Set the handler to which this layer should send data to the layer above */
	public void setAboveTargetHandler(Handler handler) {
		hSendAbove = handler;
	}
	
	/** Set the handler to which this layer should send data to the layer below */
	public void setBelowTargetHandler(Handler handler) {
		hSendBelow = handler;
	}
	
	/** Passes a message to the layer below this one */
	public void sendMessageBelow(Object o) {
		Message msg = hSendBelow.obtainMessage();
		msg.obj = o;
		hSendBelow.sendMessage(msg);
	}
	
	/** Passes a message to the layer above this one */
	public void sendMessageAbove(Object o) {
		Message msg = hSendAbove.obtainMessage();
		msg.obj = o;
		hSendAbove.sendMessage(msg);
	}
	
	/**
	 * Called by the communication thread when the application is requesting
	 * to turn off the communication service.  Should use this function to clean up
	 * anything being done within the layer (save state, etc), and then call quit() on
	 * upThread and downThread to cease accepting messages.
	 */
	public void stopLayer() {
		upThread.quit();
		downThread.quit();
	}
	
	/**
	 * This method must be implemented by the layer appropriately to handle messages 
	 * moving up the stack.
	 */
	public abstract void handleMessageFromBelow(Message msg);
	
	/**
	 * This method must be implemented by the layer appropriately to handle messages 
	 * moving down the stack.
	 */
	public abstract void handleMessageFromAbove(Message msg);
}

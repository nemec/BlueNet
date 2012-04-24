package ec.nem.bluenet.net;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import ec.nem.bluenet.Message;
import ec.nem.bluenet.MessageListener;
import ec.nem.bluenet.net.Socket.ReceiveHandler;

/**
 * The SocketManager sits above the network stack and interfaces with sockets that
 * belong to individual programs within the application.  It essentially receives all
 * messages and routes them to the correct socket.  Sockets pass their messages through
 * this layer as well.<br><br>
 * 
 * The SocketManager is a singleton, and users should get an instance of it using
 * getInstance().
 * 
 * @author Darren White, Ivan Hernandez
 */
public final class SocketManager {
	private static final String TAG = "SocketManager";
	private static SocketManager mInstance;
	/** Handles input to this layer from a lower layer */
	private Handler hReceiveFromBelow;
	/** Handles output to the layer below this one */
	private Handler hSendBelow;
	
	/** Processes packets as they flow up the stack */
	private HandlerThread upThread;
	
	private Map<Integer, List<MessageListener>> messageListeners;
	private ArrayList<Socket> mSockets;
	
	private SocketManager() {
		messageListeners = new HashMap<Integer, List<MessageListener>>();
		mSockets = new ArrayList<Socket>();
		upThread = new HandlerThread("SocketManager Receive Thread");
		upThread.start();
		
		hReceiveFromBelow = new Handler(upThread.getLooper()) {
			@Override
			public void handleMessage(android.os.Message msg) {
				handleMessageFromBelow(msg);
			}
		};
	}
	
	/*
	 * This is the singleton for the class.
	 */
	public static SocketManager getInstance() {
		if(mInstance == null) {
			mInstance = new SocketManager();
		}
		return mInstance;
	}
	
	public void stopManager() {
		upThread.quit();
	}
	
	public void initializePort(int port){
		if(!messageListeners.containsKey(port)){
			ArrayList<MessageListener> listeners = new ArrayList<MessageListener>();
			messageListeners.put(port, listeners);
		}
	}
	
	public Socket requestSocket(int type) {
		Socket socket = new Socket(type, this);
		mSockets.add(socket);
		return socket;
	}
	
	/**
	 * Removes the given socket from the socket manager.
	 * 
	 * @param s
	 */
	public void removeSocket(Socket s) {
		mSockets.remove(s);
	}
	
	/** Gets the handler to which data should be sent by the layer below this one */
	public Handler getBelowHandler() {
		return hReceiveFromBelow;
	}
	
	/** Set the handler to which this layer should send data to the layer below */
	public void setBelowTargetHandler(Handler handler) {
		hSendBelow = handler;
	}
	
	/** Passes a message to the layer below this one */
	public void sendMessageBelow(Object o) {
		android.os.Message msg = hSendBelow.obtainMessage();
		msg.obj = o;
		hSendBelow.sendMessage(msg);
	}
	
	private Socket getSocketByPort(int port) {
		for(Socket s : mSockets) {
			if(s.getBoundPort() == port) {
				return s;
			}
		}
		return null;
	}
	
	public void handleMessageFromBelow(android.os.Message msg) {
		
		Segment s = (Segment) msg.obj;
		final int type = s.getType();
		if(type == Segment.TYPE_UDP) {
			/// Handles UDP packets
			UDPHeader header = (UDPHeader) s.transportSegment;
			int port = header.getDestinationPort();
			if(messageListeners.containsKey(port)) {
				List<MessageListener> handlers = messageListeners.get(port);
				if((handlers.size()==0)){
					notifySockets(header, port);
				}
				//send it straight to our UI where magic will handle it
				Message message = Message.deserialize(header.getData());
				if (message != null) {
					Log.d(TAG, "Message Received on BluePort:" + message
							+ "\nWe have " + messageListeners.size()
							+ " Listeners\n");
					for (MessageListener l : handlers) {
						l.onMessageReceived(message);
					}
				}else{
					Log.e(TAG, "Message null from header:" + header);
				}
			}
			else {
				notifySockets(header, port);
			}
		}
		else {
			throw new IllegalArgumentException("Cannot use Non-UDP Segment"); 
		}
	}
	

	private void notifySockets(UDPHeader header, int port) {
		Socket socket;
		// Used with Socket.receive()
		Message message = Message.deserialize(header.getData());
		if (message != null) {
			Log.d(TAG, "Message Reveived but not going to the UI apparently:"
					+ message);
			socket = getSocketByPort(port);
			if (socket != null) {
				ReceiveHandler rh = socket.getMessageHandler();
				if (rh != null) {
					android.os.Message m = rh.obtainMessage(Segment.TYPE_UDP,
							header.getData());
					rh.sendMessage(m);
				}
			}
		} else {
			Log.e(TAG, "Message null from header:" + header);
		}
	}

	public boolean addMessageListener(MessageListener l, int port){
		if(messageListeners.containsKey(port)){
			return messageListeners.get(port).add(l);
		}
		return false;
	}

	public boolean removeMessageListener(MessageListener l, int port){
		if(messageListeners.containsKey(port)){
			return messageListeners.get(port).remove(l);
		}
		return false;
	}
}

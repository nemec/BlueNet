package ec.nem.bluenet.net;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
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
	public static final int BLUENET_PORT = 50000;
	private static SocketManager mInstance;
	/** Handles input to this layer from a lower layer */
	private Handler hReceiveFromBelow;
	/** Handles output to the layer below this one */
	private Handler hSendBelow;
	
	/** Processes packets as they flow up the stack */
	private HandlerThread upThread;
	
	private List<MessageListener> messageListeners;
	private ArrayList<Socket> mSockets;
	
	private SocketManager(Context context) {
		messageListeners = new ArrayList<MessageListener>();
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
	public static SocketManager getInstance(Context context) {
		if(mInstance == null) {
			mInstance = new SocketManager(context);
		}
		return mInstance;
	}
	
	public void stopManager() {
		upThread.quit();
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
		Socket socket;
		
		Segment s = (Segment) msg.obj;
		final int type = s.getType();
		if(type == Segment.TYPE_UDP) {
			/// Handles UPD packets \TODO: this can probably be deleted down to the else
			UDPHeader header = (UDPHeader) s.transportSegment;
			int port = header.getDestinationPort();
			if(port == BLUENET_PORT) {
				//send it straight to our UI where magic will handle it
				Message message = Message.deserialize(header.getData());
				Log.d(TAG,"Message Received on BluePort:"+message+"\nWe have " + messageListeners.size() + " Listeners\n");
				for(MessageListener l : messageListeners){
					l.onMessageReceived(message);
				}
			}
			else {
				//Used with Socket.receive()
				Message message = Message.deserialize(header.getData());
				Log.d(TAG,"Message Reveived but not going to the UI apparently:" + message);
				socket = getSocketByPort(port);
				if(socket != null) {
					ReceiveHandler rh = socket.getMessageHandler();
					if(rh != null) {
						android.os.Message m = rh.obtainMessage(Segment.TYPE_UDP, header.getData());
						rh.sendMessage(m);
					}
				}
			}
		}
		else {
			throw new IllegalArgumentException("Cannot use Non-UDP Segment"); 
		}
	}
	
	public boolean addMessageListener(MessageListener l){
		return messageListeners.add(l);
	}

	public boolean removeMessageListener(MessageListener l){
		return messageListeners.remove(l);
	}
}

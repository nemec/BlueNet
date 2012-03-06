package ec.nem.bluenet.net;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import ec.nem.bluenet.Message;
import ec.nem.bluenet.MessageListener;
import ec.nem.bluenet.Node;
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
 * @author Darren White
 */
public final class SocketManager {
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
	private Node mLocalNode;
	
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
	 * \TODO: Why is this a singleton if it's only used in one place?
	 */
	public static SocketManager getInstance(Context context) {
		if(mInstance == null) {
			mInstance = new SocketManager(context);
		}
		return mInstance;
	}
	
	public void setLocalNode(Node n) {
		mLocalNode = n;
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
				Message message = Message.deserialize(header.getData());
				for(MessageListener l : messageListeners){
					l.onMessageReceived(message);
				}
			}
			else {
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
		else if(type == Segment.TYPE_STCP) {
			/// \TODO: Add TCP Header actions
			throw new IllegalArgumentException("Cannot use Segment type STCP"); 
		}
	}
	
	/*
	 * Send a message to a specific destination.
	 */
	public void sendMessage(int port, String text){
		Socket s = getSocketByPort(port);
		if(s != null){
			Message m = new Message("No one.", text, (System.currentTimeMillis() / 1000L));
			s.send(Message.serialize(m));
		}
	}
	
	/*
	 * Send a message to everyone.
	 */
	public void broadcastMessage(String text){
		for(Socket s : mSockets){
			Message m = new Message("No one.", text, (System.currentTimeMillis() / 1000L));
			s.send(Message.serialize(m));
		}
	}
	
	public void addMessageListener(MessageListener l){
		messageListeners.add(l);
	}

	public boolean removeMessageListener(MessageListener l){
		return messageListeners.remove(l);
	}
}

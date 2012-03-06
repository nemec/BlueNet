package ec.nem.bluenet.net;

import java.util.ArrayList;

import ec.nem.bluenet.Node;
import ec.nem.bluenet.net.Socket.ReceiveHandler;


import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;

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
	private static SocketManager mInstance;
	/** Handles input to this layer from a lower layer */
	private Handler hReceiveFromBelow;
	/** Handles output to the layer below this one */
	private Handler hSendBelow;
	
	/** Processes packets as they flow up the stack */
	private HandlerThread upThread;
	
	private ArrayList<Socket> mSockets;
	
	private SocketManager(Context context) {
		mSockets = new ArrayList<Socket>();
		upThread = new HandlerThread("SocketManager Receive Thread");
		upThread.start();
		
		hReceiveFromBelow = new Handler(upThread.getLooper()) {
			@Override
			public void handleMessage(Message msg) {
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
		Message msg = hSendBelow.obtainMessage();
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
	
	public void handleMessageFromBelow(Message msg) {
		Socket socket;
		
		Segment s = (Segment) msg.obj;
		final int type = s.getType();
		if(type == Segment.TYPE_UDP) {
			/// Handles UPD packets \TODO: this can probably be deleted down to the else
			UDPHeader header = (UDPHeader) s.transportSegment;
			int port = header.getDestinationPort();
			if(port == 50000) {
				/// \todo: iterate message listeners and call on message 
//				storeMessage(header.getData());
			}
			else {
				socket = getSocketByPort(port);
				if(socket != null) {
					ReceiveHandler rh = socket.getMessageHandler();
					if(rh != null) {
						Message m = rh.obtainMessage(Segment.TYPE_UDP, header.getData());
						rh.sendMessage(m);
					}
				}
			}
		}
		else if(type == Segment.TYPE_STCP) {
			/// Handles UPD packets 
			/// \TODO: Add TCP Header actions
		}
	}
	
}

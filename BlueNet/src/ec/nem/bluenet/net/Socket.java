package ec.nem.bluenet.net;

import android.os.*;

import ec.nem.bluenet.Node;
import ec.nem.bluenet.net.stcp.Receiver;
import ec.nem.bluenet.net.stcp.STCPHeader;
import ec.nem.bluenet.net.stcp.Sender;

/**
 * This socket interfaces with our network structure 
 * \TODO: This needs to be heavily tested.
 * @author Ivan Hernandez
 *
 */
public class Socket {
	/*
	 * The source port where data is sent from on this machine
	 */
	private int mSourcePort;
	
	/*
	 * This is the destination port on the remote node where data will be sent
	 */
	private int mDestinationPort;
	
	/*
	 * UDP or TCP
	 */
	private int mType;
	
	/*
	 * Storage for the header segment of the packet
	 */
	private Segment mSegment;
	
	/*
	 * The thread managing what this socket sends
	 */
	private HandlerThread mHandlerThread;
	
	/*
	 * The thread that handles message receipt for this socket.
	 */
	private ReceiveHandler mReceiveHandler; 
	
	/*
	 * The socket manager that contains this Socket(since there could be more than one)
	 */
	private SocketManager mSocketManager;
	
	private Sender mSender;
	private Receiver mReceiver;
	
	public Socket(int type, SocketManager sm) {
		mType = type;
		mSourcePort = 0;
		mDestinationPort = 0;
		mSegment = new Segment(type);
		
		mHandlerThread = new HandlerThread("Socket HandlerThread");
		mHandlerThread.start();
		
		mSocketManager = sm;
		
		if(type == Segment.TYPE_STCP) {
			mSender = new Sender();
			mReceiver = new Receiver();
		}
	}
	
	/* 
	 * Returns the message handler associated with this socket (I assume for debugging)
	 */
	public ReceiveHandler getMessageHandler() {
		return mReceiveHandler;
	}
	
	/*
	 * Sets the port from which this socket will send data
	 */
	public boolean bind(int port) {
		mSourcePort = port;
		return true;
	}
	 /* 
	  * Returns the bound port for this socket.
	  */
	public int getBoundPort() {
		return mSourcePort;
	}
	
	/*
	 * Connects to the specified None on the specified port.
	 */
	public boolean connect(Node node, int destinationPort) {
		mDestinationPort = destinationPort;
		mSegment = new Segment(mType);
		switch(mType) {
		case Segment.TYPE_UDP:
			UDPHeader udpHeader = (UDPHeader) mSegment.transportSegment;
			udpHeader.setSourcePort(mSourcePort);
			udpHeader.setDestinationPort(mDestinationPort);
			break;
		case Segment.TYPE_STCP:
			STCPHeader stcpHeader = (STCPHeader) mSegment.transportSegment;
			stcpHeader.setSourcePort(mSourcePort);
			stcpHeader.setDestinationPort(mDestinationPort);
			/// \TODO: Send out connection packet, await response?
			
			break;
		}
		
		mSegment.IPHeader.destinationAddress = node.getIPAddress();
		
		switch (mType) {
		case Segment.TYPE_UDP:
			mSegment.IPHeader.setNextHeader(IPv6Header.NH_UDP);
			break;
		case Segment.TYPE_STCP:
			mSegment.IPHeader.setNextHeader(IPv6Header.NH_SCTP);
			break;
		}
		
		return true;
	}
	
	public void close() {
		mHandlerThread.quit();
		mSocketManager.removeSocket(this);
	}
	
	/**
	 * Sends the given data
	 * 
	 * @param data The data to be sent
	 */
	public void send(byte[] data) {
		switch(mType) {
		case Segment.TYPE_UDP:
			UDPHeader header = (UDPHeader) mSegment.transportSegment;
			header.setData(data);
			break;
		case Segment.TYPE_STCP:
			/// \TODO: TCP segment, need to check if connected, otherwise
			// say no?
			break;
		}
		mSocketManager.sendMessageBelow(mSegment);
	}
	
	/**
	 * Wait for data to arrive on the socket.  This will block.
	 * In the event this process is interrupted, the data returned
	 * will be null.
	 * 
	 * @return the data received
	 */
	public byte[] receive() {
		mReceiveHandler = new ReceiveHandler(mHandlerThread.getLooper());
		
		synchronized(mReceiveHandler) {
			try {
				mReceiveHandler.wait();
			}
			catch(InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		return mReceiveHandler.data;
	}
	
	/**
     * Handler to handle incoming messages to the socket.
     */
    public class ReceiveHandler extends Handler {
    	public byte[] data = null;
    	
    	public ReceiveHandler(Looper l) {
    		super(l);
    	}
    	
    	public synchronized void handleMessage(android.os.Message msg) {
    		switch(msg.what) {
    		case Segment.TYPE_UDP:
    			data = (byte[]) msg.obj;
    			break;
    		case Segment.TYPE_STCP:
    			data = (byte[]) msg.obj;
    			break;
    		default:
    			data = null;
    		}
    		
    		// Wake up threads waiting for us to have data:
    		notifyAll();  
    	}
    }
}
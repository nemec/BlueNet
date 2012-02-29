package ec.nem.bluenet.net;

import android.os.*;

import ec.nem.bluenet.Node;
import ec.nem.bluenet.net.stcp.Receiver;
import ec.nem.bluenet.net.stcp.STCPHeader;
import ec.nem.bluenet.net.stcp.Sender;


public class Socket {
	private int mSourcePort;
	private int mDestinationPort;
	private int mType;
	private Segment mSegment;
	
	private HandlerThread mHandlerThread;
	private ReceiveHandler mReceiveHandler;
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
	
	public ReceiveHandler getMessageHandler() {
		return mReceiveHandler;
	}
	
	public boolean bind(int port) {
		mSourcePort = port;
		/// \TODO:  Any checks to perform here?
		return true;
	}
	
	public int getBoundPort() {
		return mSourcePort;
	}
	
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
	 * @param data
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

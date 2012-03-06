package ec.nem.bluenet.net;

import ec.nem.bluenet.net.stcp.STCPHeader;

public class Segment {
	public static final int TYPE_UDP = 0;
	public static final int TYPE_STCP = 1;
	public static final int TYPE_ROUTING = 2;
	
	public byte[] nextHopMACAddress = new byte[6];
	public IPv6Header IPHeader = new IPv6Header();
	public TransportSegment transportSegment;
	
	private int mType = TYPE_UDP;
	
	public Segment(int type) {
		switch(type) {
		case TYPE_UDP:
			transportSegment = new UDPHeader();
			break;
		case TYPE_STCP:
			transportSegment = new STCPHeader();
			break;
		case TYPE_ROUTING:
			transportSegment = new DataSegment();
			break;
		default:
			transportSegment = new UDPHeader();
		}
		
		mType = type;
	}

	/**
	 * Gets the transport header type of this socket.
	 * Either Socket.TYPE_TCP or Socket.TYPE_UDP
	 * 
	 * @return the mType
	 */
	public int getType() {
		return mType;
	}
}
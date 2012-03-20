package ec.nem.bluenet.net;

import java.text.MessageFormat;

public class Segment {
	public static final int TYPE_UDP = 0;
//	public static final int TYPE_STCP = 1; 
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
	 * Socket.TYPE_UDP
	 * 
	 * @return the mType
	 */
	public int getType() {
		return mType;
	}
	
	/**
	 * Turn MAC address byte array into human readable string.
	 * @param mac byte[] representing MAC address
	 * @return MAC address as String
	 */
	public static String getMacAddressAsString(byte[] mac){
		StringBuilder sb = new StringBuilder(18);
	    for (byte b : mac) {
	        if (sb.length() > 0)
	            sb.append(':');
	        sb.append(String.format("%02x", b));
	    }
	    return sb.toString();
	}

	@Override
	public String toString(){
		return  MessageFormat.format(
				"Segment::Going to:{0} With Data:{1}",
				getMacAddressAsString(nextHopMACAddress),
				transportSegment); 
	}
}

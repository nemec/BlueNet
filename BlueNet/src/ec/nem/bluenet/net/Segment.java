package ec.nem.bluenet.net;

import java.io.ByteArrayOutputStream;
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
				" Segment::{0} With data:{1}",
				IPHeader,
				transportSegment); 
	}

	public static Segment deserialize(byte[] data) {
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		//we only support udp
		Segment s = new Segment(Segment.TYPE_UDP);
		// pull out IP header
		
		os.write(data, 0, 8);
		s.IPHeader.headerFields = os.toByteArray();

		// pull out source IP
		os.reset();
		os.write(data, 8, 16);
		s.IPHeader.sourceAddress = os.toByteArray();

		// pull out destination IP
		os.reset();
		os.write(data, 24, 16);
		s.IPHeader.destinationAddress = os.toByteArray();

		// rest of the data
		os.reset();
		os.write(data, 40, data.length - 40);
		s.transportSegment.setRawBytes(os.toByteArray());
		return s;
	}
}

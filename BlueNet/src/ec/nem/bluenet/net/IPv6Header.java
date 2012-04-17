package ec.nem.bluenet.net;

import java.text.MessageFormat;

import ec.nem.bluenet.utils.Utils;

public class IPv6Header {
	public static final int NH_UDP = 1;
//	public static final int NH_SCTP = 2; 
	public static final int NH_ROUTING = 3;
	
	public byte[] headerFields = new byte[8];
	
	public byte[] sourceAddress = new byte[16];
	public byte[] destinationAddress = new byte[16];
	
	/** Gets the IPv6 version number (Should be 6) */
	public int getVersion() {
		return (headerFields[0] & 0xF0) >>> 4;
	}
	
	/** Gets the 8 bit traffic class */
	public int getTrafficClass() {
		int firstHalf = 0xF & headerFields[0];
		int secondHalf = 0xF & (headerFields[1] >> 4);
		
		return (firstHalf << 4) | secondHalf;
	}
	
	/** Gets the 20 bit flow label */
	public int getFlowLabel() {
		int firstByte = 0x0F & headerFields[1];
		int secondByte = 0xFF & headerFields[2];
		int thirdByte = 0xFF & headerFields[3];
		
		return (firstByte << 16) | (secondByte << 8) | thirdByte;
	}
	
	/** Gets the 16bit payload length */
	public int getPayloadLength() {
		int firstByte = 0xFF & headerFields[4];
		int secondByte = 0xFF & headerFields[5];
		
		return (firstByte << 8) | secondByte;
	}
	
	/** Gets the 8bit next header field */
	public int getNextHeader() {
		return 0xFF & ((int) headerFields[6]);
	}
	
	/** Gets the 8bit hop limit field */
	public int getHopLimit() {
		return 0xFF & ((int) headerFields[7]);
	}
	
	/** Sets the version number (4bits).  Note: Should be set to 6 */
	public void setVersion(int version) {
		headerFields[0] &= 0x0F;
		headerFields[0] |= (version << 4);
	}
	
	/** Sets the traffic class (8bits) */
	public void setTrafficClass(int trafficClass) {
		headerFields[0] &= 0xF0;
		headerFields[1] &= 0x0F;
		
		headerFields[0] |= (0xF & (trafficClass >> 4));
		headerFields[1] |= (trafficClass << 4);
	}

	/** Sets the flow label (20bits) */
	public void setFlowLabel(int flowLabel) {
		headerFields[1] &= 0xF0;
		headerFields[2] &= 0x0;
		headerFields[3] &= 0x0;
		
		headerFields[1] |= (flowLabel >> 16);
		headerFields[2] |= (flowLabel >>> 8);
		headerFields[3] |= flowLabel;
	}

	/** Sets the payload length (16bits) */
	public void setPayloadLength(int payloadLength) {
		headerFields[4] &= 0x0;
		headerFields[5] &= 0x0;
		
		headerFields[4] |= (payloadLength >> 8);
		headerFields[5] |= payloadLength;
	}

	/** Sets the next header (8bits) */
	public void setNextHeader(int nextHeader) {
		headerFields[6] &= 0x0;
		headerFields[6] |= nextHeader;
	}

	/** Sets the hop limit (8bits) */
	public void setHopLimit(int hopLimit) {
		headerFields[7] &= 0x0;
		headerFields[7] |= hopLimit;
	}

	@Override
	public String toString(){
		return  MessageFormat.format(
				" Segment::To:{0} From:{1} Type:{2} ",
				Utils.getMacAddressAsString(destinationAddress),
				Utils.getMacAddressAsString(sourceAddress),
				getNextHeader(),
				getHopLimit()); 
	}
	
}

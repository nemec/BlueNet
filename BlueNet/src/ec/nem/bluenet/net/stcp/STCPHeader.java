package ec.nem.bluenet.net.stcp;

import ec.nem.bluenet.net.TransportSegment;

/**
 * Header for data sent using Bluemesh's Simple TCP protocol.
 * 
 * @author Darren White
 */
public class STCPHeader extends TransportSegment {
	private static final int MIN_HEADER_LENGTH = 8;
	
	private byte[] sourcePort = new byte[2];
	private byte[] destinationPort = new byte[2];
	private byte[] control = new byte[1];
	private byte[] info = new byte[3];
	private byte[] data = null;
	
	public STCPHeader() {
		setSourcePort(0);
		setDestinationPort(0);
		setProtocolBits(0);
		setSynchronizeBit(0);
		setOKBit(0);
		setFinalizeBit(0);
		setInfoBits(0);
	}
	
	/**
	 * The port number of the sender.
	 * 
	 * @param port the source port number
	 */
	public void setSourcePort(int port) {
		sourcePort[0] &= 0x0;
		sourcePort[1] &= 0x0;
		
		sourcePort[0] |= (port >> 8);
		sourcePort[1] |= port;
	}
	
	/**
	 * Sets the port this packet is addressed to.
	 * 
	 * @param port the destination port number
	 */
	public void setDestinationPort(int port) {
		destinationPort[0] &= 0x0;
		destinationPort[1] &= 0x0;
		
		destinationPort[0] |= (port >> 8);
		destinationPort[1] |= port;
	}
	
	/**
	 * Sets the protocol number, which is a 5 bit field.
	 * 
	 * @param value
	 */
	public void setProtocolBits(int value) {
		control[0] &= 0x07;
		control[0] |= ((value << 3) & 0x000000F8);
	}
	
	/**
	 * Sets the SYN bit to either 1 or 0.
	 * 
	 * @param value
	 */
	public void setSynchronizeBit(int value) {
		if(value > 0) {
			control[0] |= 0x04;
		}
		else {
			control[0] &= 0xFB;
		}
	}
	
	/**
	 * Sets the OK bit to 1 or 0.
	 * 
	 * @param value
	 */
	public void setOKBit(int value) {
		if(value > 0) {
			control[0] |= 0x02;
		}
		else {
			control[0] &= 0xFD;
		}
	}
	
	/**
	 * Sets the FIN bit to 1 or 0.
	 * 
	 * @param value
	 */
	public void setFinalizeBit(int value) {
		if(value > 0) {
			control[0] |= 0x01;
		}
		else {
			control[0] &= 0xFE;
		}
	}
	
	/**
	 * Sets the info bits, which can be a sequence number,
	 * ack number, window size, etc. (24 bits only)
	 * 
	 * @param value
	 */
	public void setInfoBits(int value) {
		info[0] &= 0x0;
		info[1] &= 0x0;
		info[2] &= 0x0;
		
		info[0] |= (value >> 16);
		info[1] |= (value >> 8);
		info[2] |= value;
	}
	
	public void setData(byte[] data) {
		this.data = data;
	}
	
	/**
	 * Gets the port number of the sender. Zero if not used.
	 * 
	 * @return the source port
	 */
	public int getSourcePort() {
		int firstByte = 0xFF & sourcePort[0];
		int secondByte = 0xFF & sourcePort[1];
		
		return (firstByte << 8) | secondByte;
	}
	
	/**
	 * Gets the port this packet is addressed to.
	 * 
	 * @return the destination port
	 */
	public int getDestinationPort() {
		int firstByte = 0xFF & destinationPort[0];
		int secondByte = 0xFF & destinationPort[1];
		
		return (firstByte << 8) | secondByte;
	}
	
	/**
	 * Gets the 5 bit protocol identifier.  This field is reserved in case
	 * someone wants to implement different protocols with this header.
	 * 
	 * @return the protocol identifier
	 */
	public int getProtocolBits() {
		return (control[0] >> 3) & 0x1F;
	}
	
	/**
	 * Gets the SYN bit, used for connection setup
	 * 
	 * @return 1 or 0
	 */
	public int getSynchronizeBit() {
		return (control[0] >> 2) & 0x01;
	}
	
	/**
	 * Gets the OK bit, used for connection setup/teardown
	 * @return
	 */
	public int getOKBit() {
		return (control[0] >> 1) & 0x01;
	}
	
	/**
	 * Gets the FIN bit, used for connection teardown
	 * @return
	 */
	public int getFinalizeBit() {
		return control[0] & 0x01;
	}
	
	/**
	 * Gets the 24 bit information field, which can be an ACK number,
	 * a sequence number, window size, etc.
	 * 
	 * @return an integer value
	 */
	public int getInfoBits() {
		int firstByte = 0xFF & info[0];
		int secondByte = 0xFF & info[1];
		int thirdByte = 0xFF & info[2];
		
		return (firstByte << 16) | (secondByte << 8) | thirdByte;
	}
	
	public byte[] getData() {
		return this.data;
	}

	public byte[] getRawBytes() {
		int index = 0;
		byte[] rawBytes;
		if(data != null) {
			rawBytes = new byte[MIN_HEADER_LENGTH + data.length]; 
		}
		else {
			rawBytes = new byte[MIN_HEADER_LENGTH];
		}
		 
		index = copyToBuffer(rawBytes, sourcePort, index);
		index = copyToBuffer(rawBytes, destinationPort, index);
		index = copyToBuffer(rawBytes, control, index);
		index = copyToBuffer(rawBytes, info, index);
		if(data != null)
			index = copyToBuffer(rawBytes, data, index);
			
		return rawBytes;
	}

	public void setRawBytes(byte[] rawBytes) {
		// Sum of source + destination + control + info + data
		int rawLength = rawBytes.length;
		
		int index = 0;
		index = copyFromBuffer(rawBytes, sourcePort, index);
		index = copyFromBuffer(rawBytes, destinationPort, index);
		index = copyFromBuffer(rawBytes, control, index);
		index = copyFromBuffer(rawBytes, info, index);
		
		// If there is data, allocate the data field:
		if((rawLength - MIN_HEADER_LENGTH) > 0) {
			data = new byte[rawLength - MIN_HEADER_LENGTH];
			index = copyFromBuffer(rawBytes, data, index);
		}
	}
}

package ec.nem.bluenet.net;

public class UDPHeader extends TransportSegment {
	private static final int MIN_HEADER_LENGTH = 8;
	
	private byte[] sourcePort = new byte[2];
	private byte[] destinationPort = new byte[2];
	private byte[] length = new byte[2];
	private byte[] checksum = new byte[2];
	private byte[] data = null;
	
	public UDPHeader() {
		setSourcePort(0);
		setDestinationPort(0);
		setLength(0);
	}
	
	/**
	 * Computed as the 16-bit one's complement of the one's complement sum of a pseudo 
	 * header of information from the IP header, the UDP header, and the data, padded as 
	 * needed with zero bytes at the end to make a multiple of two bytes. 
	 * If the checksum is cleared to zero, then checksuming is disabled. 
	 * If the computed checksum is zero, then this field must be set to 0xFFFF.
	 * 
	 * @return the checksum
	 */
	public byte[] getChecksum() {
		return checksum;
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
	 * Gets the length in bytes of the UDP header and the encapsulated data.
	 * The minimum value for this field is 8.
	 * 
	 * @return the length
	 */
	public int getLength() {
		int firstByte = 0xFF & length[0];
		int secondByte = 0xFF & length[1];
		
		return (firstByte << 8) | secondByte;
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
	
	public byte[] getData() {
		return this.data;
	}
	
	/**
	 * Computed as the 16-bit one's complement of the one's complement sum of a pseudo 
	 * header of information from the IP header, the UDP header, and the data, padded as 
	 * needed with zero bytes at the end to make a multiple of two bytes. 
	 * If the checksum is cleared to zero, then checksuming is disabled. 
	 * If the computed checksum is zero, then this field must be set to 0xFFFF.
	 * 
	 */
	private void calculateChecksum() {
		checksum[0] = 0;
		checksum[1] = 0;
		
		// TODO:  Actually calculate the checksum?
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
	 * The length in bytes of the UDP header and the encapsulated data.
	 * The minimum value for this field is 8.
	 * 
	 * @param length the specified length
	 */
	private void setLength(int length) {
		this.length[0] &= 0x0;
		this.length[1] &= 0x0;
		
		this.length[0] |= (length >> 8);
		this.length[1] |= length;
	}
	
	/**
	 * The port number of the sender. Cleared to zero if not used.
	 * 
	 * @param port the source port number
	 */
	public void setSourcePort(int port) {
		sourcePort[0] &= 0x0;
		sourcePort[1] &= 0x0;
		
		sourcePort[0] |= (port >> 8);
		sourcePort[1] |= port;
	}
	
	public void setData(byte[] data) {
		this.data = data;
		setLength(data.length + 8);
		calculateChecksum();
	}
	
	public byte[] getRawBytes() {
		int index = 0;
		byte[] rawBuffer = new byte[getLength()];
		index = copyToBuffer(rawBuffer, sourcePort, index);
		index = copyToBuffer(rawBuffer, destinationPort, index);
		index = copyToBuffer(rawBuffer, length, index);
		index = copyToBuffer(rawBuffer, checksum, index);
		if(data != null)
			index = copyToBuffer(rawBuffer, data, index);
			
		return rawBuffer;
	}

	public void setRawBytes(byte[] rawBuffer) {
		// Sum of source + destination + length + checksum + data
		int rawLength = rawBuffer.length;
		
		int index = 0;
		index = copyFromBuffer(rawBuffer, sourcePort, index);
		index = copyFromBuffer(rawBuffer, destinationPort, index);
		index = copyFromBuffer(rawBuffer, length, index);
		index = copyFromBuffer(rawBuffer, checksum, index);
		
		// If there is data, allocate the data field:
		if((rawLength - MIN_HEADER_LENGTH) > 0) {
			data = new byte[rawLength - MIN_HEADER_LENGTH];
			index = copyFromBuffer(rawBuffer, data, index);
		}
	}
	
	@Override
	public String toString(){
		return "Source Port:"+ sourcePort.toString() + "\nDestinationPort:"+ destinationPort +"\nData:" + data.toString();
	}
}

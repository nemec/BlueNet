package ec.nem.bluenet.net;

public abstract class TransportSegment {
	/** 
	 * Sets fields in the transport header by parsing
	 * the raw byte stream, along with the data portion.
	 */
	public abstract void setRawBytes(byte[] rawBytes);
	
	/**
	 * Returns the raw byte array of the transport header plus data.
	 * 
	 * @return the raw byte array
	 */
	public abstract byte[] getRawBytes();
	
	/**
	 * Copies a byte array field into the raw buffer starting at the
	 * given index.  Returns the index of the byte after the copied data.
	 * 
	 * @param rawByteBuffer The buffer to copy into
	 * @param field The field to copy from
	 * @param index The index you want the data in the raw buffer to start at
	 * @return The index of the next byte after the copied data
	 */
	protected int copyToBuffer(byte[] rawByteBuffer, byte[] field, int index) {
		int lastIndex = index + field.length;
		
		for(int i = 0; index < lastIndex; i++, index++) {
			rawByteBuffer[index] = field[i];
		}
		
		return index;
	}
	
	/**
	 * Copies data to individual field from a raw buffer.  Copies start
	 * from the given index in the rawByteBuffer.
	 * 
	 * @param rawByteBuffer The raw bytes of the header
	 * @param field The field to copy into from the header
	 * @param index The index from which to start
	 * @return The index of the next byte after the copied data
	 */
	protected int copyFromBuffer(byte[] rawByteBuffer, byte[] field, int index) {
		int lastIndex = index + field.length;
		
		for(int i = 0; index < lastIndex; i++, index++) {
			field[i] = rawByteBuffer[index];
		}
		
		return index;
	}
	
}

package ec.nem.bluenet.net.stcp;

public class SynchronizationHeader {
	public STCPHeader header;
	/** Length of the data after the header, ie. file size, or message size */
	public long messageSize;
	
	public SynchronizationHeader() {
		header = new STCPHeader();
		messageSize = 0;
	}
}

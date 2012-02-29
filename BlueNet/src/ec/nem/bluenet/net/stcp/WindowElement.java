package ec.nem.bluenet.net.stcp;

public class WindowElement {
	private static int DEFAULT_SIZE = 1500;
	
	public int sequenceNumber;
	/** Size in bytes of the data portion of the packet */
	public int size;
	
	public double timeTransmitted;
	
	public boolean hasBeenRetransmitted;
	public boolean hasBeenAcknowledged;
	
	/** The packet's header plus the actual data */
	public byte[] data;
	
	public WindowElement() {
		reset();
		data = new byte[DEFAULT_SIZE];
	}
	
	public void copy(WindowElement other) {
		sequenceNumber = other.sequenceNumber;
		size = other.size;
		timeTransmitted = other.timeTransmitted;
		hasBeenRetransmitted = other.hasBeenRetransmitted;
		hasBeenAcknowledged = other.hasBeenAcknowledged;
		
		final int length = other.data.length;
		data = new byte[length];
		for(int i = 0; i < length; ++i) {
			data[i] = other.data[i];
		}
	}
	
	public void reset() {
		sequenceNumber = -1;
		size = -1;
		timeTransmitted = -1;
		hasBeenRetransmitted = false;
		hasBeenAcknowledged = false;
	}
}

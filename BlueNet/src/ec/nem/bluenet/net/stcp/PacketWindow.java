package ec.nem.bluenet.net.stcp;

/**
 * A circular buffer implementation of a selective repeat
 * buffer window for the STCP.
 * 
 * @author Darren White
 *
 */
public class PacketWindow {
	private WindowElement[] mBuffer;
	
	/** Packet with the lowest sequence number */
	private int mHead;
	/** Packet with the highest sequence number */
	private int mTail;
	/** The capacity of the window */
	private int mWindowSize;
	/** Number of packets ready to go in the window */
	private int mNumberOfPackets;
	
	public PacketWindow(int size) {
		mBuffer = new WindowElement[size];
		mHead = 0;
		mTail = 0;
		mNumberOfPackets = 0;
		mWindowSize = size;
	}
	
	/**
	 * Gets the number of packets currently in the window
	 * 
	 * @return the number of packets
	 */
	public int getSize() {
		return mNumberOfPackets;
	}
	
	/**
	 * Gets the size of the window.
	 * 
	 * @return the size of the window
	 */
	public int getWindowSize() {
		return mWindowSize;
	}
	
	/**
	 * Searches the window for the packet with the given
	 * sequence number and returns it's index, or -1.
	 * 
	 * @param sequenceNumber The sequence number to find
	 * @return the index, or -1 if not found
	 */
	public int getIndexBySequenceNumber(int sequenceNumber) {
		int diff = 0;
		if(!isEmpty()) {
			WindowElement e = mBuffer[mHead];
			if(sequenceNumber >= e.sequenceNumber) {
				diff = sequenceNumber - e.sequenceNumber;
				if(diff < mWindowSize)
					return (mHead + diff) % mWindowSize;
			}
		}
		// Sequence number wasn't within the window
		return -1;
	}
	
	/**
	 * Enqueues a packet at the given offset from the head
	 * packet.
	 * 
	 * @param e The window element to enqueue
	 * @param offset The offset from the head
	 * @return the index the newly inserted packet
	 */
	public int enqueueAt(WindowElement e, int offset) {
		int index = (mHead + offset) % mWindowSize; 
		if(!isFull()) {
			mBuffer[index].copy(e);
			mNumberOfPackets++;
			if(index >= mTail)
				mTail = (index + 1) % mWindowSize;
		}
		return index;
	}
	
	/**
	 * Add data to the window, returns the data's index or -1 if full
	 * 
	 * @param e
	 * @return
	 */
	public int enqueue(WindowElement packet) {
		int index = -1;
		if(!isFull()) {
			mBuffer[mTail].copy(packet);
			index = mTail;
			mNumberOfPackets++;
			mTail = (mTail + 1) % mWindowSize;
		}
		return index;
	}
	
	/**
	 * Remove the head packet from the window.  The removed packet
	 * is copied into e, if e is not null.
	 * 
	 * @param e 
	 * @return the index of the new head packet, or -1 if the window is empty.
	 */
	public int dequeue(WindowElement packet) {
		if(!isEmpty()) {
			if(packet != null) {
				packet.copy(mBuffer[mHead]);
			}
			mNumberOfPackets--;
			mBuffer[mHead].reset();
			mHead = (mHead + 1) % mWindowSize;
			return mHead;
		}
		return -1;
	}
	
	/**
	 * Returns data at index if index is within the window, null otherwise
	 * 
	 * @param index The index at which to peek
	 * @return the window element at index, or null if it doesn't exist
	 */
	WindowElement peek(int index) {
		// If index between head and tail, return it, otherwise,
		// return null.
		if(!isEmpty()) {
			if(mHead < mTail) {
				if(index >= mHead && index < mTail)
					return mBuffer[index];
			}
			else if(index >= mHead || index < mTail) {  // Wrap around case
				return mBuffer[index]; 
			}
		}
		return null;
	}

	public boolean isFull() {
		return (mNumberOfPackets == mWindowSize);
	}
	
	public boolean isEmpty() {
		return (mNumberOfPackets == 0);
	}
}

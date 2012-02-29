package ec.nem.bluenet.net.stcp;

import ec.nem.bluenet.net.Segment;
import ec.nem.bluenet.net.TransportLayer;

public class Sender {
	private boolean isConnected;
	private PacketWindow mWindow = null;
	
	public Sender() {
		isConnected = false;
		mWindow = new PacketWindow(TransportLayer.DEFAULT_WINDOW_SIZE);
	}
	
	public Sender(int windowSize) {
		isConnected = false;
		mWindow = new PacketWindow(windowSize);
	}
	
	public void handleMessage() {
		
	}
	
	public void handleACK() {
		
	}
	
	public synchronized boolean connect() {
		while(!isConnected) {
			
		}
		return true;
	}
	
	public synchronized void disconnect() {
		
	}
	
	public Segment makeDataPacket(Segment s, int sequenceNumber) {
		STCPHeader header = (STCPHeader) s.transportSegment;
		header.setProtocolBits(0);
		header.setSynchronizeBit(0);
		header.setOKBit(0);
		header.setFinalizeBit(0);
		header.setInfoBits(sequenceNumber);
		return s;
	}
	
	public Segment makeSYNPacket(Segment s) {
		STCPHeader header = (STCPHeader) s.transportSegment;
		header.setProtocolBits(0);
		header.setSynchronizeBit(1);
		header.setOKBit(0);
		header.setFinalizeBit(0);
		header.setInfoBits(mWindow.getWindowSize());
		return s;
	}
	
	public Segment makeFINPacket(Segment s) {
		STCPHeader header = (STCPHeader) s.transportSegment;
		header.setProtocolBits(0);
		header.setSynchronizeBit(0);
		header.setOKBit(0);
		header.setFinalizeBit(1);
		header.setInfoBits(0);
		return s;
	}
	
	public boolean checkPacket(Segment s) {
		STCPHeader header = (STCPHeader) s.transportSegment;
		
		if(header.getInfoBits() < 0
				|| header.getInfoBits() > TransportLayer.MAX_SEQUENCE_NUMBER - 1) {
			return false;  // Negative values are meaningless
		}
		if(header.getProtocolBits() != 0) {
			return false;  // No switching protocols mid-transfer!
		}
		if(header.getSynchronizeBit() == 1) {
			if(header.getOKBit() == 0 || header.getFinalizeBit() == 1)
				return false;  // This response makes no sense to the sender
		}
		if(header.getFinalizeBit() == 1) {
			if(header.getOKBit() == 0)
				return false;  // Receiver should always set ok...
		}
		return true; // Packet header info is valid
	}
}

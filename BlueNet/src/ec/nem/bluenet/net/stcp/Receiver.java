package ec.nem.bluenet.net.stcp;

import ec.nem.bluenet.net.Layer;
import ec.nem.bluenet.net.TransportLayer;

public class Receiver {
	private boolean isConnected;
	private PacketWindow mWindow = null;
	private Layer mLayer = null;
	
	public Receiver() {
		
	}
	
	public Receiver(Layer layer) {
		mLayer = layer;
		isConnected = false;
		mWindow = new PacketWindow(TransportLayer.DEFAULT_WINDOW_SIZE);
	}
	
	public Receiver(Layer layer, int windowSize) {
		mLayer = layer;
		isConnected = false;
		mWindow = new PacketWindow(windowSize);
	}
	
	public void handleMessage() {
		
	}
	
	public void deliver(WindowElement e) {
		/// \TODO: Fix
		mLayer.sendMessageAbove(e.data);
	}
	
	public void sendACKPacket() {
		
	}
	
	public void sendSYNACKPacket() {
		
	}
	
	public void sendFINACKPacket() {
		
	}
	
	public boolean checkPacket() {
		return true;
	}
}

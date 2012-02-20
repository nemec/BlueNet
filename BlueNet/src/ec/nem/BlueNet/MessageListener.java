package ec.nem.BlueNet;

public interface MessageListener {

	/**
	 * Called when a new message is received from any
	 * node in the network.
	 */
	public void onMessageReceived();
	
}

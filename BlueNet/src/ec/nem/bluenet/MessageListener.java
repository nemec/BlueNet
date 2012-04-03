package ec.nem.bluenet;

public interface MessageListener {

	/**
	 * Called when a new message is received from any
	 * node in the network.
	 */
	public void onMessageReceived(Message message);
	
}

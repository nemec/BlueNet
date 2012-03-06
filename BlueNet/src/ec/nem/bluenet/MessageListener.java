package ec.nem.bluenet;

public interface MessageListener {

	/**
	 * Called when a new message is received from any
	 * node in the network.
	 * \TODO: Figure out message type that we will be using
	 */
	public void onMessageReceived(Message message);
	
}

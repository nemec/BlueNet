package ec.nem.bluenet;

/// \TODO: Delete this file not needed really.

public interface NodeListener {
	/**
	 * Called when a new Node enters the network. 
	 */
	public void onNodeEnter();
	
	/**
	 * Called when a Node leaves the network.
	 */
	public void onNodeExit();
}

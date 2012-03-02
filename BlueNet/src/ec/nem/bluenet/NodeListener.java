package ec.nem.bluenet;

/// \TODO: Delete this file not needed really.

public interface NodeListener {
	/**
	 * Called when a new Node enters the network.
	 * \TODO: Figure out Node type
	 */
	public void onNodeEnter(Object node);
	
	/**
	 * Called when a Node leaves the network.
	 * \TODO: Figure out Node type
	 */
	public void onNodeExit(Object node);
}

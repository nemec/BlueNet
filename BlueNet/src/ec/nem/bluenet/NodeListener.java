package ec.nem.bluenet;

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

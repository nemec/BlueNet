package ec.nem.bluenet;

public interface NodeListener {
	/**
	 * Called when a new Node enters the network.
	 * Argument is the remote address of the node.
	 */
	public void onNodeEnter(String remoteAddress);
	
	/**
	 * Called when a Node leaves the network.
	 * Argument is the remote address of the node.
	 */
	public void onNodeExit(String remoteAddress);
}

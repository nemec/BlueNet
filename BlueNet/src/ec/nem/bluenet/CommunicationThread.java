package ec.nem.bluenet;

import ec.nem.bluenet.BluetoothNodeService.LocalBinder;
import ec.nem.bluenet.net.Layer;
import ec.nem.bluenet.net.LinkLayer;
import ec.nem.bluenet.net.NetworkLayer;
import ec.nem.bluenet.net.SocketManager;
import ec.nem.bluenet.net.TransportLayer;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import java.util.*;

/*
 * This sets up the communication between all the layers and the application. 
 */

public class CommunicationThread extends Thread {
	private final String TAG = "CommunicationThread";
	
	private boolean running;
	private Context context;
	
	private SocketManager mSocketManager;
	private Layer mTransportLayer;
	private NetworkLayer mNetworkLayer;
	private LinkLayer mLinkLayer;

	private List<NodeListener> nodeListeners;
	private long timeout;  // Milliseconds to wait without being notified until quit.
	
	public CommunicationThread(Context context, long timeout) {
		Log.d(TAG, "Initializing Communication Thread");
		setPriority(Thread.MIN_PRIORITY);
		
		this.context = context;
		this.timeout = timeout;
		
		nodeListeners = new ArrayList<NodeListener>();
		
		mSocketManager = SocketManager.getInstance(context);
		
		mTransportLayer = new TransportLayer();
		mLinkLayer = new LinkLayer(this);
		mNetworkLayer = new NetworkLayer(this);
		
		Log.d(TAG, "Hooking up handlers");
		mSocketManager.setBelowTargetHandler(mTransportLayer.getAboveHandler());
		
		mTransportLayer.setAboveTargetHandler(mSocketManager.getBelowHandler());
		mTransportLayer.setBelowTargetHandler(mNetworkLayer.getAboveHandler());
		
		mNetworkLayer.setAboveTargetHandler(mTransportLayer.getBelowHandler());
		mNetworkLayer.setBelowTargetHandler(mLinkLayer.getAboveHandler());
		
		mLinkLayer.setAboveTargetHandler(mNetworkLayer.getBelowHandler());
		Log.d(TAG, "Communication Thread Initialized");
	}
	
	/**
	 * Connects to all nodes in the network.
	 */
	public void connectToAll(){
		mNetworkLayer.connectToAll();
	}
	
	/**
	 * Connect to a specific node in the network.
	 * @param n The node to connect to.
	 */
	public void connectTo(Node n){
		mNetworkLayer.connectTo(n);
	}
	
	/**
	 * Begin the Link Layer and then wait for a timeout.
	 * If the thread is woken up before the timeout ends,
	 * it sleeps again for another `timeout` milliseconds.
	 */
	@Override
	public void run() {
		running = true;
		
		mLinkLayer.run();
		connectToAll();
		
		long time;
		do{
			time = System.currentTimeMillis();
			synchronized(this) {
				try {
					// Wait for timeout (pass in 0 for no timeout)
					wait(timeout);
					Log.d(TAG, String.format(
							"Current wait time: %dms\nTimeout: %dms",
							System.currentTimeMillis() - time,
							timeout));
				}
				catch(InterruptedException e) {
					e.printStackTrace();
				}
			}
		} while(running && System.currentTimeMillis() - time < timeout);
		
		mSocketManager.stopManager();
		mTransportLayer.stopLayer();
		mNetworkLayer.stopLayer();
		mLinkLayer.stopLayer();
		
		Log.w(TAG, "Service timed out... quitting.");
		Intent intent = new Intent(context, BluetoothNodeService.class);
		Log.w(TAG, String.format("Service stopped: %b", context.stopService(intent)));
	}
	
	/**
	 * Drops this node off the network and closes communication
	 */
	public void stopThread() {
		quit();
		synchronized(this) {
			notifyAll();
		}
	}
	
	public boolean quit(){
		return mNetworkLayer.quit();
	}
	
	public boolean isRunning() {
		return running;
	}
	
	/*
	 * Retrieve all nodes in the routing graph.
	 */
	public List<Node> getAvailableNodes() {
		return mNetworkLayer.getAvailableNodes();
	}
	
	public List<Node> getPairedNodes() {
		return mLinkLayer.getPairedNodes();
	}
	
	public void testPairedNodes() {
		mNetworkLayer.connectToAll();
	}
	
	public Node getLocalNode() {
		return mLinkLayer.getLocalNode();
	}
	
	public void setApplicationLayerHandler(Handler h) {
		mTransportLayer.setAboveTargetHandler(h);
	}
	
	public void addNodeListener(NodeListener l){
		nodeListeners.add(l);
	}

	public boolean removeNodeListener(NodeListener l){
		return nodeListeners.remove(l);
	}
	
	public List<NodeListener> getNodeListeners(){
		return nodeListeners;
	}

	/*
	 * Register a message listener. 
	 */
	public boolean addMessageListener(MessageListener l){
		return mSocketManager.addMessageListener(l);
	}

	/*
	 * Remove a message listener from being activated.
	 */
	public boolean removeMessageListener(MessageListener l){
		return mSocketManager.removeMessageListener(l);
	}
}

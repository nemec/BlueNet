package ec.nem.bluenet;

import java.text.ParseException;
import java.util.List;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;
import ec.nem.bluenet.net.Segment;
import ec.nem.bluenet.net.Socket;
import ec.nem.bluenet.net.SocketManager;

public class BluetoothNodeService extends Service {
	private static final String TAG = "BluetoothNodeService";
	
	/** Username that will show up on messages sent on this service */
	public static String username = "No one.";
	
	/** The port which this game will use*/
	public static final int BLUENET_PORT = 50000;
		
	/** Thread that owns the networking stack */
	private static CommunicationThread mCommThread;
	
	/** Timeout to determine how many seconds to wait before the service crashes. Set to 0 for no timeout*/
	private int commThreadTimeout = 1000 * 60 * 10;

	/** Exposes the service to clients. */
	private final IBinder binder = new LocalBinder();
	
	/** The socket representing our Bluetooth socket. */
	private static Socket socket;

	/** Provides access to the local bluetooth adapter*/
	BluetoothAdapter adapter;
	
	@Override
	public void onCreate() {
		super.onCreate();
		if(mCommThread==null){
			mCommThread = new CommunicationThread(this.getApplicationContext(),	commThreadTimeout);
			Toast.makeText(this, "Service started...", Toast.LENGTH_LONG).show();
		}
		else{
			Log.d(TAG, "Tried to start comm thread again oops");
		}
		if(socket==null){
			SocketManager sm = SocketManager.getInstance();
			socket = sm.requestSocket(Segment.TYPE_UDP);
			socket.bind(BluetoothNodeService.BLUENET_PORT);
		}
		else{
			Log.d(TAG, "Tried to rebind to our own socket again...");
		}
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.d(TAG, "Received start id " + startId + ": " + intent);

		Log.d(TAG, "Thread state when calling startService: " + mCommThread.getState().name());
		if(mCommThread.getState() == Thread.State.NEW) {
			mCommThread.setDaemon(true);
			mCommThread.start();
		}
		else if(mCommThread.getState() == Thread.State.TERMINATED) {
			mCommThread = new CommunicationThread(this.getApplicationContext(), commThreadTimeout);
			mCommThread.setDaemon(true);
			mCommThread.start();
		}
		return START_STICKY;
	}

	@Override
	public void onDestroy() {
		Log.d(TAG, "onDestroy()");
		stop();
		// Tell the user we stopped.
		Toast.makeText(this, R.string.comm_service_stopped, Toast.LENGTH_SHORT).show();
	}

	/*
	 * Kills the Communication thread for routing
	 */
    protected void stop() {
    	Log.d(TAG, "Communication thread is stopping...");
    	if(mCommThread.isRunning()) {
    		mCommThread.stopThread();
    		try {
    			mCommThread.join();
			}
    		catch(InterruptedException e) {
    			Log.e(TAG, e.getMessage());
			}
    	}
    }

    /*
     * Returns the Node for the current device
     */
    public Node getLocalNode() {
    	return mCommThread.getLocalNode();
    }
    
    /*
     * Returns a list of all devices that are on the network.
     */
    public List<Node> getAvailableNodes() {
    	return mCommThread.getAvailableNodes();
    }
    
	protected boolean supportsBluetooth(){
		return adapter != null;
	}
	
	public int getNetworkSize(){
		return mCommThread.getAvailableNodes().size();
	}
	
	public boolean connectTo(String address){
		resetTimeout();
		try {
			Node n = new Node(address);
			mCommThread.connectTo(n);
		} catch (ParseException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	public void broadcastMessage(String text){
		resetTimeout();
		for (Node n : mCommThread.getAvailableNodes()) {
			sendMessage(n, text);
		}
	}
	
	public void broadcastMessage(Object o){
		resetTimeout();
		for (Node n : mCommThread.getAvailableNodes()) {
			sendMessage(n, o);
		}
	}
	
	public void sendMessage(Node destinationNode, String text) {
		resetTimeout();
		// Don't send message to self
		if (destinationNode != getLocalNode()) {
			Message m = new Message(username, getLocalNode().getAddress(),
					text, (System.currentTimeMillis() / 1000L));
			socket.connect(destinationNode, BLUENET_PORT);
			socket.send(Message.serialize(m));
		}
	}

	public void sendMessage(Node destinationNode, Object o) {
		resetTimeout();
		// Don't send message to self
		if (destinationNode != getLocalNode()) {
			Message m = new Message(username, getLocalNode().getAddress(),
					o, (System.currentTimeMillis() / 1000L));
			socket.connect(destinationNode, BLUENET_PORT);
			socket.send(Message.serialize(m));
		}
	}
	
	public void addNodeListener(NodeListener l){
		resetTimeout();
		mCommThread.addNodeListener(l);
	}

	public boolean removeNodeListener(NodeListener l){
		resetTimeout();
		return mCommThread.removeNodeListener(l);
	}

	public void addMessageListener(MessageListener l){
		resetTimeout();
		/// This would break if we don't have the SocketManager in existence
		SocketManager.getInstance().addMessageListener(l);
	}

	public boolean removeMessageListener(MessageListener l){
		resetTimeout();
		return mCommThread.removeMessageListener(l);
	}
	
	/**
	 * Reset the communication thread's timeout to keep the service alive.
	 * When a user calls any of the service's public methods, this method
	 * should be called to make sure the service stays alive.
	 */
	private void resetTimeout(){
		synchronized(mCommThread){
			mCommThread.notify();
		}
	}

	public class LocalBinder extends Binder{
		public BluetoothNodeService getService(){
			return BluetoothNodeService.this;
		}
	}

	@Override
	public IBinder onBind(Intent arg0) {
		return binder;
	}
}

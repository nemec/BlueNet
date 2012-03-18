package ec.nem.bluenet;

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

	/** Thread that owns the networking stack */
	private CommunicationThread mCommThread;

	/** Exposes the service to clients. */
	private final IBinder binder = new LocalBinder();
	
	private Socket socket;

	BluetoothAdapter adapter;
	@Override
	public void onCreate() {
		super.onCreate();
		Toast.makeText(this, "Service started...", Toast.LENGTH_LONG).show();

		mCommThread = new CommunicationThread(this.getApplicationContext());
		
		SocketManager sm = SocketManager.getInstance(this);
        socket = sm.requestSocket(Segment.TYPE_UDP);
        socket.bind(SocketManager.BLUENET_PORT);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.d(TAG, "Received start id " + startId + ": " + intent);

		if(mCommThread.getState() == Thread.State.NEW) {
			mCommThread.start();
		}
		else if(mCommThread.getState() == Thread.State.TERMINATED) {
			mCommThread = new CommunicationThread(this.getApplicationContext());
			mCommThread.start();
		}
		return START_STICKY;
	}

	@Override
	public void onDestroy() {
		Log.d(TAG, "onDestroy()");
		// Cancel the persistent notification.
//		mNM.cancel(R.string.comm_service_started);
		stopCommThread();
		// Tell the user we stopped.
		Toast.makeText(this, R.string.comm_service_stopped, Toast.LENGTH_SHORT).show();
	}

	/*
	 * Kills the Communication thread for routing
	 * \TODO: place leaving network code here for leaving network
	 */
    public void stopCommThread() {
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
    
	public boolean supportsBluetooth(){
		return adapter != null;
	}
	
	public int getNetworkSize(){
		return mCommThread.getAvailableNodes().size();
	}
	
	public void broadcastMessage(String text){
		for(Node n: mCommThread.getAvailableNodes()){
			sendMessage(n, text);
		}
	}
	
	public void sendMessage(Node destinationNode, String text){
		/// \TODO: remove this if we want to send messages to ourselves too (it'd just go to the UI theoretically)
		if(destinationNode!=getLocalNode()){
		Message m = new Message("No one.", text, (System.currentTimeMillis() / 1000L));
		socket.connect(destinationNode, 50000);
		socket.send(Message.serialize(m));
		}
	}
	
	public void addNodeListener(NodeListener l){
		mCommThread.addNodeListener(l);
	}

	public boolean removeNodeListener(NodeListener l){
		return mCommThread.removeNodeListener(l);
	}

	public void addMessageListener(MessageListener l){
		/// This would break if we don't have the SocketManager in existence
		SocketManager.getInstance(this.getApplicationContext()).addMessageListener(l);
	}

	public boolean removeMessageListener(MessageListener l){
		return false;//messageListeners.remove(l);
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

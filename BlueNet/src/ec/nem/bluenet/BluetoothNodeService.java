package ec.nem.bluenet;

import java.util.ArrayList;
import java.util.List;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.widget.Toast;

public class BluetoothNodeService extends Service {
	
	private final IBinder binder = new LocalBinder();

	BluetoothAdapter adapter;
	List<NodeListener> nodeListeners;
	List<MessageListener> messageListeners;
	
	@Override
	public void onCreate() {
		super.onCreate();
		nodeListeners = new ArrayList<NodeListener>();
		messageListeners = new ArrayList<MessageListener>();
	    Toast.makeText(this, "Service started...", Toast.LENGTH_LONG).show();
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		return START_NOT_STICKY;
	}

	
	public boolean supportsBluetooth(){
		return adapter != null;
	}
	
	public int getNetworkSize(){
		// \TODO: Figure out how to pull network size
		return 1;
	}
	
	public void addNodeListener(NodeListener l){
		nodeListeners.add(l);
	}
	
	public boolean removeNodeListener(NodeListener l){
		return nodeListeners.remove(l);
	}
	
	public void addMessageListener(MessageListener l){
		messageListeners.add(l);
	}
	
	public boolean removeMessageListener(MessageListener l){
		return messageListeners.remove(l);
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

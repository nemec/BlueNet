package ec.nem.bluenet.test;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import ec.nem.bluenet.BluetoothNodeService;
import ec.nem.bluenet.BluetoothNodeService.LocalBinder;
import ec.nem.bluenet.Message;
import ec.nem.bluenet.MessageListener;
import ec.nem.bluenet.NodeListener;
import ec.nem.bluenet.net.SocketManager;

public class DemoActivity extends Activity implements MessageListener, NodeListener {

	BluetoothNodeService connectionService;
	boolean boundToService = false;
	ArrayAdapter<String> logAdapter;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.demo);
        
        logAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);
        
        Intent intent = new Intent(this, BluetoothNodeService.class);
    	bindService(intent, connection, Context.BIND_AUTO_CREATE);
    }
	
	private ServiceConnection connection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            LocalBinder binder = (LocalBinder) service;
            connectionService = binder.getService();
            connectionService.addMessageListener(DemoActivity.this);
            connectionService.addNodeListener(DemoActivity.this);
            boundToService = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            boundToService = false;
        }
    };

	@Override
	public void onNodeEnter(String node) {
		final String txt = node;
		runOnUiThread(new Runnable() {
			
			@Override
			public void run() {
				logAdapter.add("Node " + txt + " has joined the chat.");
			}
		});
	}

	@Override
	public void onNodeExit(String node) {
		final String txt = node;
		runOnUiThread(new Runnable() {
			
			@Override
			public void run() {
				logAdapter.add("Node " + txt + " has left the chat.");
			}
		});
	}

	@Override
	public void onMessageReceived(Message message) {
		final String from = message.getTransmitterAddress();
		final String text = message.getText();
		
		runOnUiThread(new Runnable() {
			
			@Override
			public void run() {
				logAdapter.add(from + ": " + text);
			}
		});
	}
	
	public void sendMessage(View v){
		TextView entry = (TextView)findViewById(R.id.im_text);
		String message = entry.getText().toString();
		if(message.length() > 0){
			connectionService.broadcastMessage(message);
			entry.setText("");
		}
	}
}

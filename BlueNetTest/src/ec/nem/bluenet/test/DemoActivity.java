package ec.nem.bluenet.test;

import java.util.ArrayList;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import ec.nem.bluenet.BluetoothNodeService;
import ec.nem.bluenet.BluetoothNodeService.LocalBinder;
import ec.nem.bluenet.Message;
import ec.nem.bluenet.MessageListener;
import ec.nem.bluenet.NodeListener;

public class DemoActivity extends Activity implements MessageListener, NodeListener {

	public static final String MESSAGES_KEY = "messages";
	BluetoothNodeService connectionService;
	boolean boundToService = false;
	ArrayAdapter<String> logAdapter;
	Handler uiHandler;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.demo);
        
        logAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);
        ListView l = (ListView)findViewById(R.id.chat_log);
        l.setAdapter(logAdapter);
        
        if(savedInstanceState != null){
        	ArrayList<String> messages = savedInstanceState.getStringArrayList(MESSAGES_KEY);
        	for(String s: messages){
        		logAdapter.add(s);
        	}
        }
        
        uiHandler = new Handler();
        
        Intent intent = new Intent(this, BluetoothNodeService.class);
    	bindService(intent, connection, Context.BIND_AUTO_CREATE);
    }
	
	@Override
	public void onDestroy(){
		super.onDestroy();
		// MAKE SURE YOU REMOVE YOURSELF FROM THE LISTENER LIST
		// OTHERWISE YOU WILL LEAK MEMORY
		if(connectionService != null){
			connectionService.removeMessageListener(this);
			connectionService.removeNodeListener(this);
		}
		unbindService(connection);
	}
	
	@Override
	public void onSaveInstanceState(Bundle b){
		ArrayList<String> backup = new ArrayList<String>();
		for(int x = 0; x < logAdapter.getCount(); x++){
			backup.add(logAdapter.getItem(x));
		}
		b.putStringArrayList(MESSAGES_KEY, backup);
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
		uiHandler.post(new Runnable() {
			@Override
			public void run() {
				logAdapter.add("Node " + txt + " has Connected to us.");
				logAdapter.notifyDataSetChanged();
			}
		});
	}

	@Override
	public void onNodeExit(String node) {
		final String txt = node;
		uiHandler.post(new Runnable() {
			@Override
			public void run() {
				logAdapter.add("Node " + txt + " has disconnected from us.");
				logAdapter.notifyDataSetChanged();
			}
		});
	}

	@Override
	public void onMessageReceived(Message message) {
		final String from = message.getTransmitterName();
		final String text = message.getText();
		
		uiHandler.post(new Runnable() {
			@Override
			public void run() {
				logAdapter.add(from + ": " + text);
				logAdapter.notifyDataSetChanged();
			}
		});
	}
	
	public void sendMessage(View v){
		TextView entry = (TextView)findViewById(R.id.im_text);
		String message = entry.getText().toString();
		if(message.length() > 0){
			connectionService.broadcastMessage(message);
			entry.setText("");
			
			logAdapter.add("Me: " + message);
			logAdapter.notifyDataSetChanged();
		}
	}
}

package ec.nem.BlueNet.test;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.widget.TextView;
import ec.nem.BlueNet.BluetoothNodeService;
import ec.nem.BlueNet.BluetoothNodeService.LocalBinder;
import ec.nem.BlueNet.R;

public class BlueNetLibApp extends Activity {
	
	private BluetoothNodeService connectionService;
	private boolean bound = false;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        Intent intent = new Intent(this, BluetoothNodeService.class);
    	startService(intent);
    }
    
    @Override
    protected void onDestroy(){
    	super.onDestroy();
    	unbindService(connection);
    }
    
    @Override
    protected void onStart(){
    	super.onStart();
    	Intent intent = new Intent(this, BluetoothNodeService.class);
    	bindService(intent, connection, Context.BIND_AUTO_CREATE);
    }
    
    @Override
    protected void onStop(){
    	super.onStop();
    	if(bound){
    		unbindService(connection);
    	}
    }
    
    public boolean isBound(){
    	return bound;
    }
    
    private ServiceConnection connection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            LocalBinder binder = (LocalBinder) service;
            connectionService = binder.getService();
            TextView v = (TextView)findViewById(R.id.text);
            v.setText("Bound!");
            bound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
        	TextView v = (TextView)findViewById(R.id.text);
            v.setText("No longer bound...");
            bound = false;
        }
    };
}

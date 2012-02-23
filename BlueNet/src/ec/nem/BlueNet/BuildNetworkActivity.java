package ec.nem.BlueNet;

import java.util.Set;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;

public class BuildNetworkActivity extends Activity {

	private static final int REQUEST_ENABLE_BT = 2039234;
	private BluetoothAdapter adapter;
	
	@Override
	public void onCreate(Bundle savedInstance){
		adapter = BluetoothAdapter.getDefaultAdapter();
		if (adapter != null && !adapter.isEnabled()) {
		    Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
		    startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
		}
		//ListAdapter arrayAdapter = ((ListView)findViewById(R.id.pairedDevices)).getAdapter();
		Set<BluetoothDevice> pairedDevices = adapter.getBondedDevices();
		if (pairedDevices.size() > 0) {
		    for (BluetoothDevice device : pairedDevices) {
		        //arrayAdapter.add(device.getName() + "\n" + device.getAddress());
		    }
		}

	}
}

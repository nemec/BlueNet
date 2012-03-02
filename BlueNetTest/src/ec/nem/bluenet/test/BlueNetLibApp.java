package ec.nem.bluenet.test;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import ec.nem.bluenet.BluetoothNodeService;
import ec.nem.bluenet.BuildNetworkActivity;
import ec.nem.bluenet.R;
import ec.nem.bluenet.BluetoothNodeService.LocalBinder;

public class BlueNetLibApp extends Activity {
	
	private static final int RESULT_BUILD_NETWORK = 3478344;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
    }
    
    public void onBuildNetworkClicked(View v){
    	Intent intent = new Intent(this, BuildNetworkActivity.class);
    	startActivityForResult(intent, RESULT_BUILD_NETWORK);
    }
    
    @Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data){
		super.onActivityResult(requestCode, resultCode, data);
		if(requestCode == RESULT_BUILD_NETWORK){
			if(resultCode == RESULT_OK){
				// The network is built. You would start the actual game here.
				TextView v = (TextView)findViewById(R.id.text);
				v.setText("Network has been built.");
			}
			else if(resultCode == RESULT_CANCELED){
				// Could not connect.
			}
		}
	}
}

package ec.nem.bluenet.test;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import ec.nem.bluenet.BuildNetworkActivity;
import ec.nem.bluenet.R;

public class BlueNetLibApp extends Activity {
	
	private static final int RESULT_BUILD_NETWORK = 3478344;
	private static final int PORT_NUMBER = 55;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
    }
    
    public void onBuildNetworkClicked(View v){
    	Intent intent = new Intent(this, BuildNetworkActivity.class);
    	intent.putExtra(BuildNetworkActivity.EXTRA_PORT, PORT_NUMBER);
    	startActivityForResult(intent, RESULT_BUILD_NETWORK);
    }
    
    @Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data){
		super.onActivityResult(requestCode, resultCode, data);
		if(requestCode == RESULT_BUILD_NETWORK){
			if(resultCode == RESULT_OK){
				Intent intent = new Intent(this, DemoActivity.class);
				startActivity(intent);
			}
			else if(resultCode == RESULT_CANCELED){
				// Could not connect.
			}
		}
	}
}

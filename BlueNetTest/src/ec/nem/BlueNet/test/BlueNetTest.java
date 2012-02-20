package ec.nem.BlueNet.test;

import ec.nem.BlueNet.BluetoothConnector;
import android.test.AndroidTestCase;

public class BlueNetTest extends AndroidTestCase {

	private BluetoothConnector connector;
	
	@Override
	protected void setUp(){
		connector = new BluetoothConnector();
	}
	
	public void testConnector(){
		assertEquals("test", connector.getName());
	}
}

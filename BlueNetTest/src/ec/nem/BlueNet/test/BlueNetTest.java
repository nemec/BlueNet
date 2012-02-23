package ec.nem.bluenet.test;

import android.test.ActivityInstrumentationTestCase2;

public class BlueNetTest extends ActivityInstrumentationTestCase2<BlueNetLibApp> {

	private BlueNetLibApp activity;
	
	public BlueNetTest(){
		super(BlueNetLibApp.class);
	}
	
	@Override
	protected void setUp(){
		activity = this.getActivity();
	}
	
	public void testServiceBound() throws InterruptedException{
		Thread.sleep(5100);
		assertTrue(activity.isBound());
	}
}

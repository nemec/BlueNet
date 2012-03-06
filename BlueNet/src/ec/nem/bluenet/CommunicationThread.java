package ec.nem.bluenet;

import ec.nem.bluenet.net.Layer;
import ec.nem.bluenet.net.LinkLayer;
import ec.nem.bluenet.net.NetworkLayer;
import ec.nem.bluenet.net.SocketManager;
import ec.nem.bluenet.net.TransportLayer;

import android.content.Context;
import android.os.Handler;
import android.util.Log;
import java.util.*;

/*
 * This sets up the communication between all the layers and the application. 
 */

public class CommunicationThread extends Thread {
	private final String TAG = "CommunicationThread";
	
	private boolean running;
	
	private SocketManager mSocketManager;
	private Layer mTransportLayer;
	private NetworkLayer mNetworkLayer;
	private LinkLayer mLinkLayer;
	
	public CommunicationThread(Context context) {
		Log.d(TAG, "Initializing Communication Thread");
		setPriority(Thread.MIN_PRIORITY);
		
		mSocketManager = SocketManager.getInstance(context);
		
		mTransportLayer = new TransportLayer();
		mLinkLayer = new LinkLayer(this);
		mNetworkLayer = new NetworkLayer(this);
		
		Log.d(TAG, "Hooking up handlers");
		mSocketManager.setBelowTargetHandler(mTransportLayer.getAboveHandler());
		
		mTransportLayer.setAboveTargetHandler(mSocketManager.getBelowHandler());
		mTransportLayer.setBelowTargetHandler(mNetworkLayer.getAboveHandler());
		
		mNetworkLayer.setAboveTargetHandler(mTransportLayer.getBelowHandler());
		mNetworkLayer.setBelowTargetHandler(mLinkLayer.getAboveHandler());
		
		mLinkLayer.setAboveTargetHandler(mNetworkLayer.getBelowHandler());
		Log.d(TAG, "Communication Thread Initialized");
	}
	
	@Override
	public void run() {
		running = true;
		
		mLinkLayer.run();
		mNetworkLayer.run();
		
		while(running) {
			synchronized(this) {
				try {
					// Now just wait until stopThread() is called.
					wait();
				}
				catch(InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
		
		mSocketManager.stopManager();
		mTransportLayer.stopLayer();
		mNetworkLayer.stopLayer();
		mLinkLayer.stopLayer();
	}
	
	public void stopThread() {
		running = false;
		synchronized(this) {
			notifyAll();
		}
	}
	
	public boolean isRunning() {
		return running;
	}
	
	public List<Node> getAvailableNodes() {
		return mNetworkLayer.getAvailableNodes();
	}
	
	public List<Node> getPairedNodes() {
		return mLinkLayer.getPairedNodes();
	}
	
	public void testPairedNodes() {
		mNetworkLayer.run();
	}
	
	public Node getLocalNode() {
		return mLinkLayer.getLocalNode();
	}
	
	public void setApplicationLayerHandler(Handler h) {
		mTransportLayer.setAboveTargetHandler(h);
	}
	
//	public void setProgressHandler(ProgressHandler progress) {
//    	mProgressHandler = progress;
//    }
//	
//	public void showProgress(boolean visible) {
//		if(mProgressHandler != null) {
//			if(visible)
//				mProgressHandler.obtainMessage(ProgressHandler.SHOW).sendToTarget();
//			else
//				mProgressHandler.obtainMessage(ProgressHandler.HIDE).sendToTarget();
//		}
//	}
//	
//	public void showProgressError(int errorCode) {
//		if(mProgressHandler != null) {
//			mProgressHandler.obtainMessage(errorCode).sendToTarget();
//		}
//	}
}

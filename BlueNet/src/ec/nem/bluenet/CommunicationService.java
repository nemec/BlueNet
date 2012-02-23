package ec.nem.bluenet;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;
import java.util.*;

//import ec.nem.bluenet.BaseActivity.ProgressHandler;


/**
 * This service encapsulates the routing layer of the mesh.<br><br>
 * 
 * The intent is for this to be an implementation of an Android local service.  Once started,
 * the service will run until a client calls Context.stopService() on the service, or the 
 * service itself calls stopSelf().  Calling Context.startService() the first time will call
 * the onCreate() method.  Subsequent calls to startService() will not create a new instance
 * of the service (if the service is already running), rather it will just call onStartCommand().
 */
public class CommunicationService extends Service {
	private static final String TAG = "CommunicationService";
	
	/** Used to notify the user via the notification bar */
	private NotificationManager mNM;
	
	/** Thread that owns the networking stack */
	private CommunicationThread mCommThread;
	
	/** Exposes the service to clients. */
    private final IBinder mBinder = new LocalBinder();

    /**
     * Used for clients to access the service's methods.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with
     * IPC (i.e. create an AIDL interface).  The bound Activity can just call the service's
     * public methods directly.  Alternatively, we could provide methods within the binder itself.
     */
    public class LocalBinder extends Binder {
    	CommunicationService getService() {
            return CommunicationService.this;
        }
    }

    @Override
    public void onCreate() {
    	Log.d(TAG, "onCreate()");
        mNM = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        // Display a notification about us starting.
        // We put an icon in the status bar.
        showNotification();
        mCommThread = new CommunicationThread(this.getApplicationContext());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "Received start id " + startId + ": " + intent);
        
        if(mCommThread.getState() == Thread.State.NEW) {
        	mCommThread.start();
        }
        else if(mCommThread.getState() == Thread.State.TERMINATED) {
        	mCommThread = new CommunicationThread(this.getApplicationContext());
        	mCommThread.start();
        }
        
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
    	Log.d(TAG, "onDestroy()");
        // Cancel the persistent notification.
        mNM.cancel(R.string.comm_service_started);
        stopCommThread();
        // Tell the user we stopped.
        Toast.makeText(this, R.string.comm_service_stopped, Toast.LENGTH_SHORT).show();
    }

    @Override
    public IBinder onBind(Intent intent) {
    	Log.d(TAG, "onBind()");
        return mBinder;
    }

    /**
     * Show a notification while this service is running.
     */
    private void showNotification() {
//        // We'll use the same text for the ticker and the expanded notification
//        CharSequence text = getText(R.string.comm_service_started);
//
//        // Set the icon, scrolling text and timestamp
////        Notification notification = new Notification(R.drawable.stat_service_running, text,
////                System.currentTimeMillis());
//
//        // The PendingIntent to launch our activity if the user selects this notification
//        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
//                new Intent(this, MainActivity.class), 0);
//
//        // Set the info for the views that show in the notification panel.
//        notification.setLatestEventInfo(this, getText(R.string.comm_service_label),
//                       text, contentIntent);
//
//        // Send the notification.
//        // We use a layout id because it is a unique number.  We use it later to cancel.
//        mNM.notify(R.string.comm_service_started, notification);
    }
    
    public CommunicationThread getCommunicationThread() {
    	return mCommThread;
    }
    
    public void stopCommThread() {
    	Log.d(TAG, "Communication thread is stopping...");
    	if(mCommThread.isRunning()) {
    		mCommThread.stopThread();
    		try {
    			mCommThread.join();
			}
    		catch(InterruptedException e) {
    			Log.e(TAG, e.getMessage());
			}
    	}
    }
    
    public Node getLocalNode() {
    	return mCommThread.getLocalNode();
    }
    
    public List<Node> getAvailableNodes() {
    	return mCommThread.getAvailableNodes();
    }
}


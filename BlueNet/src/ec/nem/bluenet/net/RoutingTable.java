package ec.nem.bluenet.net;

import java.util.*;

import android.util.Log;

import ec.nem.bluenet.*;

public class RoutingTable {
	public static final String TAG = "RoutingTable";
	List<Route> mEntries = new LinkedList<Route>();
	
	public synchronized void add(Route route) {
		mEntries.add(route);
	}
	
	public synchronized void remove(Route route) {
		mEntries.remove(route);
	}
	
	public synchronized Node getNextHop(byte[] ipAddress) {
		int maxPrefixLength = -1;
		Route winningRoute = null;
		
		for (Route r : mEntries) {
			if (r.matchesAddress(ipAddress)) {
				if (r.getPrefixLength() > maxPrefixLength) {
					winningRoute = r;
					maxPrefixLength = r.getPrefixLength();
				} else {
					Log.d(TAG,
							"PrefixLength is bad... WTF length:"
									+ r.getPrefixLength());
				}
			}
		}
		
		/* protect against NullPointerException, in the case that there is no next hop */
		if (winningRoute == null) {
			Log.e(TAG, "Next hop is not contained in routing table.");
			return null;
		}
		
		return winningRoute.getNextHop();
	}
}

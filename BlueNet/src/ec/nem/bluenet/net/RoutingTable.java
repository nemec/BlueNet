package ec.nem.bluenet.net;

import java.util.*;

import ec.nem.bluenet.*;

public class RoutingTable {
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
		
		for (Route r: mEntries) {
			if (r.matchesAddress(ipAddress) && r.getPrefixLength() > maxPrefixLength) {
				winningRoute = r;
				maxPrefixLength = r.getPrefixLength();
			}
		}
		
		/* protect against NullPointerException, in the case that there is no next hop */
		if (winningRoute == null) {
			return null;
		}
		
		return winningRoute.getNextHop();
	}
}

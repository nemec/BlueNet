package ec.nem.bluenet;

import java.text.ParseException;
import java.util.HashMap;

import ec.nem.bluenet.utils.Utils;

public class NodeFactory {
	public static NodeFactory factory = new NodeFactory();
	
	private HashMap<String, Node> mByMac = new HashMap<String, Node>();
	
	private NodeFactory() {
	}
	
	public synchronized Node fromMacAddress(String address) throws ParseException {
		if (mByMac.containsKey(address)) {
			return mByMac.get(address);
		}
		
		Node n = new Node(address);
		mByMac.put(address, n);
		
		return n;
	}
	
	public synchronized Node fromMacAddress(byte[] bytes) {
		try {
			return fromMacAddress(Utils.getMacAddressAsString(bytes));
		} catch (ParseException e) {
			return null;
		}
	}
}

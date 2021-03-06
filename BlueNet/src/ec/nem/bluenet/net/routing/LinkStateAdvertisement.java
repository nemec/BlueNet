package ec.nem.bluenet.net.routing;

import android.util.Log;

import ec.nem.bluenet.*;

import java.io.*;
import java.text.ParseException;
import java.util.*;

public class LinkStateAdvertisement implements Serializable {
	private static final long serialVersionUID = 1;
	private static String TAG = "LinkStateAdvertisement";
	
	public Node source;
	public int sequence = initialSequence;
	public ArrayList<Node> others = new ArrayList<Node>();
	
	public static final int initialSequence = 1;
	
	private void writeObject(ObjectOutputStream oos) throws IOException {
		oos.writeObject(source.getAddress());
		oos.writeObject(sequence);
		
		oos.writeObject(others.size());
		for (Node n: others) {
			oos.writeObject(n.getAddress());
		}
	}
	
	private void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException {
		others = new ArrayList<Node>();
		
		String source = (String) ois.readObject();
		try {
			this.source = NodeFactory.factory.fromMacAddress(source);
		} catch (ParseException e) {
			e.printStackTrace();
			this.source = null;
		}
				
		sequence = (Integer) ois.readObject();
		
		Integer numNodes = (Integer) ois.readObject();
		for (int i = 0; i < numNodes; ++i) {
			String address = (String) ois.readObject();
			
			try {
				Node n = NodeFactory.factory.fromMacAddress(address);
				others.add(n);
			} catch (ParseException e) {
				Log.d(TAG, "Failed to get node from factory properly");
			}
		}
	}
}

package ec.nem.bluenet.net;


import java.text.MessageFormat;
import java.text.ParseException;
import java.util.*;

import ec.nem.bluenet.CommunicationThread;
import ec.nem.bluenet.Node;
import ec.nem.bluenet.NodeFactory;
//import ec.nem.bluenet.BaseActivity.ProgressHandler;
import ec.nem.bluenet.net.routing.*;
import ec.nem.bluenet.utils.Utils;


import android.os.Message;
import android.util.Log;

/**
 * Moves datagrams between hosts.  This is where the IP and routing logic resides.
 * 
 * @author Darren White drastically cleaned up by Ivan Hernandez
 */

public class NetworkLayer extends Layer {
	private static final String TAG = "NetworkLayer";
	CommunicationThread mCommThread;
	RoutingTable mRoutingTable = new RoutingTable();
	RoutingProtocol mRoutingProtocol;
	
	public NetworkLayer(CommunicationThread t) {
		super();
		mCommThread = t;
		mRoutingProtocol = new RoutingProtocol(mCommThread.getLocalNode(), this);
	}
	
	/**
	 * This connects to all the paired devices
	 */
	public void connectToAll() {
		for (Node n: mCommThread.getPairedNodes()) {
			mRoutingProtocol.connectTo(n);
		}
	}
	
	/**
	 * This connects to the specified node
	 */
	public void connectTo(Node n) {
		mRoutingProtocol.connectTo(n);
	}
	
	/**
	 * This removes from the network the specified node
	 */
	public void removeNode(Node n) {
		mRoutingProtocol.removeNode(n);
	}
	
	@Override
	public void handleMessageFromAbove(Message msg) {
		Segment s = (Segment) msg.obj;
		
		byte[] destination = s.IPHeader.destinationAddress;
		Node nextHop = mRoutingTable.getNextHop(destination);
		if(nextHop != null) {
			s.nextHopMACAddress = nextHop.getAddressBytes();
			Log.d(TAG, "Sending to:" + s );
			sendMessageBelow(s);
		}
		else {
			///TODO: handle route non-existence 
//			mCommThread.showProgressError(ProgressHandler.ROUTE_FAILURE);
			Log.e(TAG, "Route doesn't exist" + s );
		}
	}

	@Override
	public void handleMessageFromBelow(Message msg) {
		Segment s = (Segment) msg.obj;
		
		if (s.IPHeader.getNextHeader() == IPv6Header.NH_ROUTING) {
			Log.d(TAG, "Forwarding Routing Message.");
			dispatchRoutingMessage(s);
		} else {
			byte[] destination = s.IPHeader.destinationAddress;
			Node myNode = mCommThread.getLocalNode();
			
			if (!Arrays.equals(destination, myNode.getIPAddress())) {
				// handle packets that should be transported through this node 
				Node nextHop = mRoutingTable.getNextHop(destination);

				if(nextHop == null){
					Log.w(TAG,
						MessageFormat.format(
								"Could not forward message to {0}.",
								Utils.getMacAddressAsString(destination)));
				}
				else{
					s.nextHopMACAddress = nextHop.getAddressBytes();
					Log.d(TAG, "Forwarding message to:" + nextHop);
					sendMessageBelow(s);
				}
			}
			else {
				// otherwise send to Transport Layer
				Log.d(TAG, "Sending up:" + s );
				sendMessageAbove(s);
			}
		}
	}

	@Override
	public void stopLayer() {
		super.stopLayer();
	}
	
	/**
	 * Obtains all nodes that routing knows about
	 * @return list of all Routing table key nodes
	 */
	public List<Node> getAvailableNodes() {
		return mRoutingProtocol.getAvailableNodes();
	}
	
	/**
	 * Tells this node to drop off the network.
	 * @return whether quitting was successful
	 */
	public boolean quit(){
		return mRoutingProtocol.quit();
	}
	
	public void sendRoutingMessage(Node n, RoutingMessage msg) {
		Segment segment = new Segment(Segment.TYPE_ROUTING);
		DataSegment dataSegment = (DataSegment) segment.transportSegment;
		dataSegment.setRawBytes(RoutingMessage.serializeMessage(msg));
		IPv6Header ipHeader = segment.IPHeader;
		
		ipHeader.sourceAddress = mCommThread.getLocalNode().getIPAddress();
		ipHeader.destinationAddress = n.getIPAddress();
		ipHeader.setNextHeader(IPv6Header.NH_ROUTING);
		
		segment.nextHopMACAddress = n.getAddressBytes();
		
		sendMessageBelow(segment);
	}
	
	private void dispatchRoutingMessage(Segment s) {
		DataSegment ds = new DataSegment();
		ds.setRawBytes(s.transportSegment.getRawBytes());
		RoutingMessage rm = RoutingMessage.deserializeMessage(ds.getRawBytes());
		if(rm != null){
			/* Since the Node class is probably serialized without its addressBytes */ 
		 	/* TODO: this violates encapsulation A LOT, since it depends on how RoutingMessage is 
		 	* serialized and depends on Node's serialization mechanism */ 
		 	if(rm.type == RoutingMessage.Type.Hello || 
		 			rm.type == RoutingMessage.Type.HelloAck || 
		 			rm.type == RoutingMessage.Type.Quit) { 
		 		Node n = (Node) rm.obj; 
		 		try { 
		 			Node newNode = NodeFactory.factory.fromMacAddress(n.getAddress());
		 			rm.obj = newNode; 
		 		}catch(ParseException e) {
		 			e.printStackTrace(); 
		 		} 
		 	}
			mRoutingProtocol.receiveMessage(rm);
			
			/* Get the routing table that might have been calculated */
			Map<Node, RoutingProtocol.GraphNode> protocolTable = mRoutingProtocol.getRoutingTable();
			if (protocolTable != null) {
				RoutingTable routingTable = new RoutingTable();
	
				for (Node n : protocolTable.keySet()) {
					// TODO If we want to change the distance in the graph this is where we do it.
					routingTable.add(new Route(n.getIPAddress(), (short) 128, protocolTable.get(n).nextHop));
				}
	
				mRoutingTable = routingTable;
			}
		}
 	}
}

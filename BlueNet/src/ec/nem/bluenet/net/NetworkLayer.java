package ec.nem.bluenet.net;


import java.text.ParseException;
import java.util.*;

import ec.nem.bluenet.CommunicationThread;
import ec.nem.bluenet.Node;
import ec.nem.bluenet.NodeFactory;
//import ec.nem.bluenet.BaseActivity.ProgressHandler;
import ec.nem.bluenet.net.routing.*;


import android.os.Message;

/**
 * Moves datagrams between hosts.  This is where the IP and routing logic resides.
 * 
 * @author Darren White drastically cleaned up by Ivan Hernandez
 */

public class NetworkLayer extends Layer {
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
	public void run() {
		for (Node n: mCommThread.getPairedNodes()) {
			mRoutingProtocol.connectTo(n);
		}
	}
	
	/**
	 * This connects to the specified node
	 */
	public void run(Node n) {
		mRoutingProtocol.connectTo(n);
	}
	
	@Override
	public void handleMessageFromAbove(Message msg) {
		Segment s = (Segment) msg.obj;
		
		// TODO: This doesn't handle the case where there isn't a route
		byte[] destination = s.IPHeader.destinationAddress;
		Node nextHop = mRoutingTable.getNextHop(destination);
		if(nextHop != null) {
			s.nextHopMACAddress = nextHop.getAddressBytes();
			sendMessageBelow(s);
		}
		else {
			///\TODO: handle route non-existence 
//			mCommThread.showProgressError(ProgressHandler.ROUTE_FAILURE);
		}
	}

	@Override
	public void handleMessageFromBelow(Message msg) {
		Segment s = (Segment) msg.obj;
		
		if (s.IPHeader.getNextHeader() == IPv6Header.NH_ROUTING) {
			dispatchRoutingMessage(s);
		} else {
			byte[] destination = s.IPHeader.destinationAddress;
			Node myNode = mCommThread.getLocalNode();
			
			if (!Arrays.equals(destination, myNode.getIPAddress())) {
				// handle packets that should be transported through this node 
				Node nextHop = mRoutingTable.getNextHop(destination);
				s.nextHopMACAddress = nextHop.getAddressBytes();
				sendMessageBelow(s);
			}
			else {
				// otherwise send to UI
				sendMessageAbove(s);
			}
		}
	}

	@Override
	public void stopLayer() {
		super.stopLayer();
	}
	
	public List<Node> getAvailableNodes() {
		return mRoutingProtocol.getAvailableNodes();
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
		/* Since the Node class is probably serialized without its addressBytes */ 
	 	/* TODO: this violates encapsulation A LOT, since it depends on how RoutingMessage is 
	 	* serialized and depends on Node's serialization mechanism */ 
	 	if(rm.type == RoutingMessage.Type.Hello || rm.type == RoutingMessage.Type.HelloAck) { 
	 		Node n = (Node) rm.obj; 
	 		try { 
	 			Node newNode = NodeFactory.factory.fromMacAddress(n.getAddress());
	 			newNode.setDeviceName(n.getDeviceName()); 
	 			newNode.setName(n.getDeviceName()); 
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
				routingTable.add(new Route(n.getIPAddress(), (short) 128,
						protocolTable.get(n).predecessor));
			}

			mRoutingTable = routingTable;
		}
 	}
}

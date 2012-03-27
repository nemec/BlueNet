package ec.nem.bluenet.net.routing;



import java.text.MessageFormat;
import java.util.*;

import android.util.Log;
import ec.nem.bluenet.Node;
import ec.nem.bluenet.net.NetworkLayer;
import ec.nem.bluenet.net.routing.RoutingMessage.Type;

/**
 * A single run of the Routing Protocol
 * 
 * @author mmullins, and Ivan Hernandez
 */
public class RoutingProtocol {
	static String TAG = "RoutingProtocol";
	
	/* static public ConcurrentHashMap<String, RoutingProtocol> devices =
		new ConcurrentHashMap<String, RoutingProtocol>();
	*/
	
	Node mNode;
	NetworkLayer mNetworkLayer;

	public RoutingProtocol(Node node, NetworkLayer networkLayer) {
		mNode = node;
		mNetworkLayer = networkLayer;
	}
	
	enum LinkState {
		None,
		HelloSent,
		HandshakeCompleted,
		FullyConnected
	};
	
	HashMap<Node, LinkState> mLinks = new HashMap<Node, LinkState>();
	HashMap<Node, LinkStateAdvertisement> mGraph = new HashMap<Node, LinkStateAdvertisement>();
	
	public void receiveMessage(RoutingMessage msg) {
		switch (msg.type) {
		case Hello: {
			Node n = (Node) msg.obj;
			Log.d(TAG, MessageFormat.format("Received Hello packet from {0}",
					n.getAddress()));
			if (!mLinks.containsKey(n) || mLinks.get(n) == LinkState.None) {
				Log.d(TAG, MessageFormat.format("Sending Hello packet to {0}",
						n.getAddress()));
				
				RoutingMessage newMsg = new RoutingMessage();
				newMsg.type = Type.Hello;
				newMsg.obj = mNode;
				
				mLinks.put(n, LinkState.HelloSent);
				mNetworkLayer.sendRoutingMessage(n, newMsg);
			} else if (mLinks.get(n) == LinkState.HelloSent) {
				mLinks.put(n, LinkState.FullyConnected);
					
				Log.d(TAG, MessageFormat.format("Sending HelloAck to {0}",
						n.getAddress()));
				
				RoutingMessage newMsg = new RoutingMessage();
				newMsg.type = Type.HelloAck;
				newMsg.obj = mNode;

				mNetworkLayer.sendRoutingMessage(n, newMsg);

				handshakeFinished(n);
			} else {
				Log.e(TAG, MessageFormat.format("Received erroneous Hello from {0}",
						n.getAddress()));
			}
			break;
		}
			
		case HelloAck: {
			Node n = (Node) msg.obj;
			if (mLinks.get(n) == LinkState.HelloSent) {
				mLinks.put(n, LinkState.FullyConnected);
				handshakeFinished(n);
			} else {
				Log.e(TAG, MessageFormat.format("Received erroneous HelloAck from {0}",
						n.getAddress()));
			}
			break;
		}
		
		case LinkStateAdvertisement: {
			LinkStateAdvertisement lsa = (LinkStateAdvertisement) msg.obj;
			handleNewLsa(lsa);
			break;
		}
		
		case Quit: {
			Node n = (Node) msg.obj;
			//if we're connected we want the network to know that we're not anymore.
			if (mLinks.get(n) == LinkState.FullyConnected) {
				mLinks.remove(n);
				removeNode(n);
			} else {
				Log.e(TAG, MessageFormat.format("Received erroneous Quit from {0}",
						n.getAddress()));
			}
			break;
		}
		
		default:
			Log.e(TAG, MessageFormat.format("Received some message I don't understand: {0}",
					msg));
		}
	}
	
	void handleNewLsa(LinkStateAdvertisement lsa) {
		if (!mGraph.containsKey(lsa.source) || mGraph.get(lsa.source).sequence < lsa.sequence) {
			Log.d(TAG, MessageFormat.format(
					"Got an LSA of sequence {0} from {1}",
					lsa.sequence, lsa.source.getAddress()));
			
			RoutingMessage msg = new RoutingMessage();
			msg.type = Type.LinkStateAdvertisement;
			msg.obj = lsa;
			
			LinkStateAdvertisement thisLsa = mGraph.get(mNode);
			for (Node n: thisLsa.others) {
				mNetworkLayer.sendRoutingMessage(n, msg);
			}
			
			mGraph.put(lsa.source, lsa);
			
			recomputeRoutingTable();
		} else {
			Log.d(TAG, MessageFormat.format("Erroneous new LSA: sequence {0} from {1}",
					lsa.sequence, lsa.source.getAddress()));
		}
	}
	
	void handshakeFinished(Node n) {
		Log.d(TAG, MessageFormat.format("Finished handshake with {0}", 
				n.getAddress()));
		
		LinkStateAdvertisement thisLsa;
		if (mGraph.containsKey(mNode)) {
			thisLsa = mGraph.get(mNode);
			thisLsa.sequence++;
		} else {
			thisLsa = new LinkStateAdvertisement();
			thisLsa.source = mNode;
			mGraph.put(mNode, thisLsa);
		}
		
		thisLsa.others.add(n);
		
		// Send the new link state announcement to all connected devices
		for (Node other: thisLsa.others) {
			Log.d(TAG, MessageFormat.format("Sending updated LSA sequence {0} from {1} to {2}",
					thisLsa.sequence, thisLsa.source.getAddress(), other.getAddress()));
			
			RoutingMessage msg = new RoutingMessage();
			msg.type = Type.LinkStateAdvertisement;
			msg.obj = thisLsa;
			mNetworkLayer.sendRoutingMessage(other, msg);
		}
		
		// Send the entire link state database to the new node
		for (Node origin: mGraph.keySet()) {
			// Ignore the LSA received directly from the connecting node
			if (n == origin) continue;
			
			RoutingMessage msg = new RoutingMessage();
			msg.type = Type.LinkStateAdvertisement;
			msg.obj = mGraph.get(origin);
			mNetworkLayer.sendRoutingMessage(n, msg);
		}
	}
		
	void removeNode(Node n) {
		Log.d(TAG, MessageFormat.format("{0} has quit.", 
				n.getAddress()));
		
		LinkStateAdvertisement thisLsa;
		if (mGraph.containsKey(mNode)) {
			thisLsa = mGraph.get(mNode);
			thisLsa.sequence++;
		} else {
			thisLsa = new LinkStateAdvertisement();
			thisLsa.source = mNode;
			mGraph.put(mNode, thisLsa);
		}
		
		thisLsa.others.remove(n);
		
		// Send the new link state announcement to all connected devices
		for (Node other: thisLsa.others) {
			Log.d(TAG, MessageFormat.format("Sending updated LSA sequence {0} from {1} to {2}",
					thisLsa.sequence, thisLsa.source.getAddress(), other.getAddress()));
			
			RoutingMessage msg = new RoutingMessage();
			msg.type = Type.LinkStateAdvertisement;
			msg.obj = thisLsa;
			mNetworkLayer.sendRoutingMessage(other, msg);
		}
		
	}

	public void connectTo(Node n) {
		RoutingMessage newMsg = new RoutingMessage();
		newMsg.type = Type.Hello;
		newMsg.obj = mNode;
		
		mLinks.put(n, LinkState.HelloSent);
		mNetworkLayer.sendRoutingMessage(n, newMsg);
	}
	
	/**
	 * Obtains all nodes that routing knows about
	 * @return list of all Routing table key nodes
	 */
	public List<Node> getAvailableNodes() {
		return new ArrayList<Node>(mGraph.keySet());
	}
	
	/**
	 * Tells this node to drop off the network.
	 * @return whether quitting was successful
	 */
	public boolean quit(){
		RoutingMessage newMsg = new RoutingMessage();
		newMsg.type = Type.Quit;
		newMsg.obj = mNode;
		LinkStateAdvertisement thisLsa;
		if (mGraph.containsKey(mNode)) {
			thisLsa = mGraph.get(mNode);
		} else {
			Log.d(TAG, "Quitting when we're not even there...");
			return false;
		}
		Log.d(TAG, "Quitting!");
		for(Node n: thisLsa.others)
		{
			mNetworkLayer.sendRoutingMessage(n, newMsg);
		}
		return true;
	}
	
	/**
	 * Helper class for Dijkstra's algorithm.
	 * @author mmullins, Ivan Hernandez
	 */
	public class GraphNode implements Comparable<GraphNode> {
		public Node node;
		public int distance;
		public Node predecessor;
	
		public GraphNode(Node n, int d, Node p) {
			node = n;
			distance = d;
			predecessor = p;
		}
		
		/**
		 * Compare based on distance, then by node MAC, then by predecessor MAC
		 * Provides a complete ordering so as not to screw with other data structures
		 */
		public int compareTo(GraphNode o) {
			if (distance == o.distance) {
				int compNode = node.getAddress().compareTo(o.node.getAddress());
				int compPred = predecessor.getAddress().compareTo(o.node.getAddress());
				
				if (compNode != 0) {
					return compNode;
				} else if (compPred != 0) {
					return compPred;
				} else {
					return 0;
				}
			} else if (distance < o.distance) {
				return -1;
			} else if (distance > o.distance) {
				return 1;
			} else {
				Log.e(TAG, "Whoa, GraphNode::compareTo failed massively!");
				return Integer.MAX_VALUE;
			}
		}
	}
	
	Map<Node, GraphNode> mRoutingTable;

	/**
	 * Implements Dijkstra's algorithm to calculate the routing table
	 */
	void recomputeRoutingTable() {
		//LinkStateAdvertisement thisLsa = mGraph.get(mNode);
		Map<Node, GraphNode> finalGraph = new HashMap<Node, GraphNode>();
		PriorityQueue<GraphNode> queue = new PriorityQueue<GraphNode>();
		
		queue.add(new GraphNode(mNode, 0, null));
		
		while (!queue.isEmpty()) {
			GraphNode gn = queue.remove();
			
			// Have we already found a path to this
			if (finalGraph.containsKey(gn.node)) {
				// Is that path shorter?  Then skip the rest!
				if (finalGraph.get(gn.node).distance < gn.distance) {
					continue;
				}
			}

			finalGraph.put(gn.node, gn);
			
			for (Node n: mGraph.get(gn.node).others) {
				/* Only add the node to the queue if we've received an LSA from it
				 * and is well connected. 
				 */
				if (mGraph.containsKey(n) && mGraph.get(n).others.contains(gn.node)) {
					/* Find the next hop for the routing table */
					Node predecessor;
					if (gn.node == mNode) {
						/* If the node that got us here is this node, then we
						 * want to actually set the predecessor */
						predecessor = n;
					} else {
						/* Otherwise, let's take the same predecessor from the node that got us here */
						predecessor = gn.predecessor;
					}
					/**
					 * TODO: when we want to change the connection graph 
					 * 			we need to make gn.distance+1 to + magic formula 
					 * 			taking in battery if we can get to the nodes some 
					 * 			other manner (may be costly)
					 */
					GraphNode ngn = new GraphNode(n, gn.distance + 1, predecessor);
					queue.add(ngn);
				} else {
					Log.d(TAG, MessageFormat.format("Skipping node {0}", n.getAddress()));
				}
			}
		}
		
		Log.d(TAG, "Routing table computation complete!");
		mRoutingTable = finalGraph;
	}
	
	public Map<Node, GraphNode> getRoutingTable() {
		return mRoutingTable;
	}
}

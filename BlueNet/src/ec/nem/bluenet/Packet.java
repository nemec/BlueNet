package ec.nem.bluenet;

/** A message to pass within our framework, with its type */
public class Packet {
	public static final int TYPE_DATA = 0;
	public static final int TYPE_ROUTING_INIT = 1;
	public static final int TYPE_ROUTING_INFO = 2;
	public static final int TYPE_ACK = 3;
	// Other types...
	
	public int type;
	public byte[] data;
	
	public Packet(int type, byte[] data) {
		this.type = type;
		this.data = data.clone();
	}
}

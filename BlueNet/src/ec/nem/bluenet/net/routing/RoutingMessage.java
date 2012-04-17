package ec.nem.bluenet.net.routing;

import java.io.*;

import android.util.Log;

public class RoutingMessage implements Serializable {
	public static final String TAG = "RoutingMessage";
	public static final long serialVersionUID = 1L;
	
	public enum Type {
		Hello,
		HelloAck,
		LinkStateAdvertisement,
		Quit
	};
	
	public Type type;
	public Object obj;
	
	public static byte[] serializeMessage(RoutingMessage msg) {
		try {
			ByteArrayOutputStream os = new ByteArrayOutputStream();
			ObjectOutputStream oos = new ObjectOutputStream(os);
			oos.writeObject(msg);
			return os.toByteArray();
		} catch (IOException e) {
			return null;
		}
	}
	
	public static RoutingMessage deserializeMessage(byte[] input) {
		try {
			ByteArrayInputStream is = new ByteArrayInputStream(input);
			ObjectInputStream ois = new ObjectInputStream(is);
			RoutingMessage msg = (RoutingMessage) ois.readObject();
			return msg;
		} catch (IOException e) {
			Log.e(TAG, e.getMessage());
			return null;
		} catch (ClassNotFoundException e) {
			/* This really shouldn't happen, since we *ARE* the RoutingMessage class! */
			return null;
		}
	}
}

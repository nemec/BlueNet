package ec.nem.bluenet.net;

import android.os.Message;
import android.util.Log;

/**
 * Transports application-layer messages between application endpoints on the network.
 * This is where protocols like TCP and UDP are implemented.<br><br>
 * 
 * A message here is known as a segment.
 * 
 * @author Darren White
 */

public class TransportLayer extends Layer {
	private static final String TAG = "TransportLayer";
	public static final int DEFAULT_WINDOW_SIZE = 64;
	/** This is 2^24, sequence numbers should go from [0, 2^24 - 1] */
	public static final int MAX_SEQUENCE_NUMBER = 16777216;
	
	public TransportLayer() {
		Log.i(TAG, "Transport Layer Created");
	}

	@Override
	public void handleMessageFromAbove(Message msg) {
		// TODO For now, just send it to the next layer:
		sendMessageBelow(msg.obj);
	}

	@Override
	public void handleMessageFromBelow(Message msg) {
		// TODO For now, just send it to the next layer:
		Segment s = (Segment) msg.obj;
		sendMessageAbove(s);
	}

	@Override
	public void stopLayer() {
		super.stopLayer();
	}
	
	private int estimateRTT(int estRTT, int sampleRTT) {
		return (int) ((0.875 * estRTT) + (0.125 * sampleRTT) + 0.5);
	}
	
	private int estimateDeviance(int devRTT, int sampleRTT, int estRTT) {
		return (int) ((0.75 * devRTT) + (0.25 * Math.abs(sampleRTT - estRTT)) + 0.5);
	}
}

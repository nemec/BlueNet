package ec.nem.bluenet;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.text.MessageFormat;
import java.text.ParseException;

import android.util.Log;

import ec.nem.bluenet.utils.Utils;


/**
 * Represents a user on the mesh.
 * 
 * @author Darren White, Ivan Hernandez
 */
public class Node implements Serializable {
	private final static String TAG = "Node";
	private static final long serialVersionUID = 1L;

	/** The device's bluetooth MAC address */
	private String deviceAddress;
	/** The device's bluetooth MAC address (binary) */
	private transient byte[] deviceAddressBytes;
		
	public Node() {
		deviceAddress = "Unknown";
		deviceAddressBytes = new byte[6];
	}
	
	public Node(String address) throws ParseException {
		this();
		setAddress(address);
	}

	public Node(String userName, String deviceName, String deviceAddress) throws ParseException {
		this();
		setAddress(deviceAddress);
	}
	
	public synchronized final String getAddress() {
		return deviceAddress;
	}
	
	public synchronized final byte[] getAddressBytes() {
		return deviceAddressBytes;
	}
	
	public synchronized final byte[] getIPAddress() {
		// turn it into fe80::/10 addresses
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		
		os.write(0xFE);
		os.write(0x80);
		os.write(0x00);
		os.write(0x00);
		os.write(0x00);
		os.write(0x00);
		os.write(0x00);
		os.write(0x00);
		os.write(0x00);
		os.write(0x00);
		try {
			os.write(deviceAddressBytes, 0, 6);
		} catch (Exception e) {
			return null;
		}
		
		return os.toByteArray();
	}
	
	public synchronized final void setAddress(String addr) throws ParseException {
		/*
		 * Parses the Bluetooth address from a string into a byte array.
		 */
		addr = addr.toUpperCase();
		String[] bytes = addr.split(":");
		
		if (bytes.length != 6) {
			throw new ParseException("A Bluetooth address must have 6 bytes, delimited by :", -1);
		}
		
		for (int i = 0; i < 6; ++i) {
			try {
				int theByte = Integer.parseInt(bytes[i], 16);
				
				if (theByte < 0 || theByte > 0xFF) {
					throw new ParseException("The byte must be between 0 and 255 (inclusive)", i);
				}
				
				deviceAddressBytes[i] = (byte) theByte;
			} catch (NumberFormatException ex) {
				// we don't want any of the bytes we've parsed so far, so let's throw out
				// the old array and make a new one
				deviceAddressBytes = new byte[6];
				throw new ParseException("The byte must be a valid hexadecimal", i);
			}
		}
		
		// keep the string around for old-times' sake
		deviceAddress = addr;
	}

	/** 
	 * Serializes the node safely
	 * @param node Node to serialize
	 * @return Bytes representing this object
	 */
	public static byte[] serialize(Node node) {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		try {
			ObjectOutputStream oos = new ObjectOutputStream(bos);
			oos.writeObject(node);
		}
		catch(IOException e) {
			e.printStackTrace();
		}
		
		return bos.toByteArray();
	}
	
	/**
	 * Deserializes a node from its byte representation
	 * @param nodeData Node data to Deserialize
	 * @return The node represented by the byte array
	 */
	public static Node deserialize(byte[] nodeData) {
		Node node = null;
		ObjectInputStream ois;
		try {
			ois = new ObjectInputStream(new ByteArrayInputStream(nodeData));
			Object obj = ois.readObject();
			if(obj instanceof Node) {
				node = (Node) obj;
			}
		}
		catch(IOException e1) {
			e1.printStackTrace();
			Log.e(TAG,"Reading the node broke badly.");
		}
		catch(ClassNotFoundException e2) {
			e2.printStackTrace();
			Log.e(TAG,"Reading the node resulted in not finding the class.");
		}
		
		return node;
	}
	
	@Override
	public String toString(){
		return  MessageFormat.format("Node {0}, IP:{1}",
				this.deviceAddress,
				Utils.getMacAddressAsString(getIPAddress()));
	}
}

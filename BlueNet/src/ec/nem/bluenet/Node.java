package ec.nem.bluenet;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.text.MessageFormat;
import java.text.ParseException;
import java.util.Formatter;

/**
 * Represents a user on the mesh.
 * 
 * @author Darren White, Ivan Hernandez
 */
public class Node implements Serializable {
	private static final long serialVersionUID = 1L;
	
	/** A human readable name for the user */
	private transient String userName;
	/** The device's bluetooth adapter name */
	private String deviceName;
	/** The device's bluetooth MAC address */
	private String deviceAddress;
	/** The device's bluetooth MAC address (binary) */
	private transient byte[] deviceAddressBytes;
	
	public Node() {
		userName = "Unknown";
		deviceName = "Unknown";
		deviceAddress = "Unknown";
		deviceAddressBytes = new byte[6];
	}
	
	public Node(String address) throws ParseException {
		this();
		setAddress(address);
	}

	public Node(String userName, String deviceName, String deviceAddress) {
		this.userName = userName;
		this.deviceName = deviceName;
		this.deviceAddress = deviceAddress;
	}
	
	public synchronized final String getName() {
		return userName;
	}
	
	public synchronized final String getDeviceName() {
		return deviceName;
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
	
	public synchronized final void setName(String name) {
		userName = name;
	}
	
	public synchronized final void setDeviceName(String name) {
		deviceName = name;
	}
	
	public synchronized final void setAddress(String addr) throws ParseException {
		/*
		 * Parses the Bluetooth address from a string into a byte array.
		 */
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
	
	public synchronized final void setAddressBytes(byte[] bytes) {
		// TODO: handle errors if it's not 6 bytes
		deviceAddress = addressFromBytes(bytes);
		deviceAddressBytes = bytes;
	}
	
	public static String addressFromBytes(byte[] bytes) {
		String deviceAddress;
		
		Formatter f = new Formatter();
		deviceAddress = f.format("%02X:%02X:%02X:%02X:%02X:%02X", bytes[0],
				bytes[1], bytes[2], bytes[3], bytes[4], bytes[5]).toString();
		
		return deviceAddress;
	}
	
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
		}
		catch(ClassNotFoundException e2) {
			e2.printStackTrace();
		}
		
		return node;
	}
	
	@Override
	public String toString(){
		return  MessageFormat.format("Node Username:{0}, DeviceName:{1} DeviceAddress{2} IP:{3}",this.userName,this.deviceName,this.deviceAddress,getIPAddress());
	}
}

package ec.nem.bluenet;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Calendar;

public class Message implements Comparable<Message>, Serializable {
	private static final long serialVersionUID = -8651367473468823389L;
	
	private String mTransmitterName = "Unknown";
	private String mTransmitterAddress = "Unknown";
	private String mText = "";
	private long mTime = 0;
	private boolean isDateSeparator = false;
	
	/**
	 * Constructor used only for creating separators in the ListView.
	 * Not the greatest code I've ever written...
	 * 
	 * @param timestamp
	 */
	public Message(long timestamp) {
		isDateSeparator = true;
		mTime = timestamp;
	}
	
	public Message(String txName, String text, long timestamp) {
		mTransmitterName = txName;
		mText = text;
		mTime = timestamp;
	}
	
	public Message(String txName, String txAddr, String text, long timestamp) {
		mTransmitterName = txName;
		mTransmitterAddress = txAddr;
		mText = text;
		mTime = timestamp;
	}
	
	public boolean isSeparator() {
		return isDateSeparator;
	}
	
	public String getTransmitterName() {
		return mTransmitterName;
	}
	
	public String getTransmitterAddress() {
		return mTransmitterAddress;
	}
	
	public String getText() {
		return mText;
	}
	
	public long getTimeInMillis() {
		return mTime;
	}
	
	public String getDateString() {
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(mTime);
		
		String time = "";
		time += (cal.get(Calendar.MONTH) + 1)+ "-" +
			cal.get(Calendar.DAY_OF_MONTH) + "-" +
			cal.get(Calendar.YEAR);
		return time;
	}
	
	public String getTimeString() {
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(mTime);
		
		int nMinutes = cal.get(Calendar.MINUTE);
		String sMinutes = "" + nMinutes;
		if(nMinutes < 10) {
			sMinutes = "0" + nMinutes;
		}
		
		String time = "";
		time += cal.get(Calendar.HOUR) + ":" +
			sMinutes + " ";
		time += (cal.get(Calendar.AM_PM) < 1) ? "AM" : "PM";
		return time;
	}
	
	public void setText(String text) {
		mText = text;
	}
	
	public void setText(byte[] raw) {
		mText = new String(raw);
	}
	
	/**
	 * Used to find out if this message was sent during any day previous
	 * to another message.
	 * 
	 * @param other The Message object to compare to
	 * @return true if this message is technically from at least one day before, false otherwise.
	 */
	public boolean isDayBefore(Message other) {
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(mTime);
		
		Calendar otherCal = Calendar.getInstance();
		otherCal.setTimeInMillis(other.getTimeInMillis());
		
		int value = cal.get(Calendar.YEAR);
		int otherValue = otherCal.get(Calendar.YEAR);
		if(value < otherValue)
			return true;
		else {
			value = cal.get(Calendar.MONTH);
			otherValue = otherCal.get(Calendar.MONTH);
			if(value < otherValue)
				return true;
			else {
				value = cal.get(Calendar.DATE);
				otherValue = otherCal.get(Calendar.DATE);
				if(value < otherValue)
					return true;
			}
		}
		
		return false;
	}

	/**
	 * Currently sorts only on the basis of time sent.  Define your
	 * own comparator if you want to do it differently.
	 */
	public int compareTo(Message another) {
		if(this.mTime > another.mTime) {
			return 1;
		}
		else if(this.mTime < another.mTime) {
			return -1;
		}
		else {
			return 0;
		}
	}
	
	public static byte[] serialize(Message message) {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		try {
			ObjectOutputStream oos = new ObjectOutputStream(bos);
			oos.writeObject(message);
		}
		catch(IOException e) {
			e.printStackTrace();
		}
		
		return bos.toByteArray();
	}
	
	public static Message deserialize(byte[] messageData) {
		Message message = null;
		ObjectInputStream ois;
		try {
			ois = new ObjectInputStream(new ByteArrayInputStream(messageData));
			Object obj = ois.readObject();
			if(obj instanceof Message) {
				message = (Message) obj;
			}
		}
		catch(IOException e1) {
			e1.printStackTrace();
		}
		catch(ClassNotFoundException e2) {
			e2.printStackTrace();
		}
		
		return message;
	}
}
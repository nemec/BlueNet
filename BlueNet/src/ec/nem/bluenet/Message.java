package ec.nem.bluenet;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.text.MessageFormat;
import java.util.Calendar;

import android.util.Log;

public class Message implements Comparable<Message>, Serializable {
	private static final String TAG = "Message";
	private static final long serialVersionUID = -8651367473468823379L;
	
	private String mTransmitterName = "Unknown";
	private String mTransmitterAddress = "Unknown";
	private String mText = "";
	private long mTime = 0;
	private boolean isDateSeparator = false;
	private Object data;
	
	public Message(String txName, String txAddr, String text, Object o, long timestamp){
		mTransmitterName = txName;
		mTransmitterAddress = txAddr;
		mText = text == null ? "" : text;  // Ensure never null
		data = o;
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

	public void setData(Object o){
		data = o;
	}

	public Object getData(){
		return data;
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
			if (obj instanceof Message) {
				message = (Message) obj;
			}
		} catch (IOException e1) {
			e1.printStackTrace();
		} catch (ClassNotFoundException e2) {
			Log.d(TAG,
					"Class "
							+ e2.getClass()
							+ " is not one of your classes. If if you see this message and do not recognize the class, simply ignore this message.");
		} catch (Exception e){
			Log.e(TAG, "Unexpected Exception when deserializing Message class version:"+serialVersionUID,e);
		}

		return message;
	}
	
	@Override
	public String toString(){
		return MessageFormat.format("Name:{0} Address:{1} Time:{3} Text:{2} ", 
					mTransmitterName,
					mTransmitterAddress,
					mText,
					mTime);
	}
}

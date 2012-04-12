package ec.nem.bluenet.utils;

public class Utils {
	

	/**
	 * Turn MAC address byte array into human readable string.
	 * @param mac byte[] representing MAC address
	 * @return MAC address as String
	 */
	public static String getMacAddressAsString(byte[] mac){
		StringBuilder sb = new StringBuilder(18);
	    for (byte b : mac) {
	        if (sb.length() > 0)
	            sb.append(':');
	        sb.append(String.format("%02x", b));
	    }
	    return sb.toString();
	}
	
}

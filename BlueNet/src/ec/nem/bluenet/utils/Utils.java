package ec.nem.bluenet.utils;


public class Utils {
	
//	public static double getBatteryLevel() {
//		Intent batteryIntent = BlueNetApplication.getContext().registerReceiver(null,
//						new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
//		int rawlevel = batteryIntent.getIntExtra("level", -1);
//		double scale = batteryIntent.getIntExtra("scale", -1);
//		double level = -1;
//		if (rawlevel >= 0 && scale > 0) {
//			level = rawlevel / scale;
//		}
//		return level;
//	}

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

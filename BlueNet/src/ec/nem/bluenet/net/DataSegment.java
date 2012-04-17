package ec.nem.bluenet.net;

import java.text.MessageFormat;

public class DataSegment extends TransportSegment {
	private byte[] mData;

	@Override
	public byte[] getRawBytes() {
		return mData;
	}

	@Override
	public void setRawBytes(byte[] rawBytes) {
		mData = rawBytes;
	}
	
	@Override
	public String toString(){
		return  MessageFormat.format( 
				" UDPHeader::{0}",
				mData.toString());
	}
}

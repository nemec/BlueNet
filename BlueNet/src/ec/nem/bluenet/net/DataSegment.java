package ec.nem.bluenet.net;

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
}

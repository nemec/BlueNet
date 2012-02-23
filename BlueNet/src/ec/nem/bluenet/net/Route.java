package ec.nem.bluenet.net;

import ec.nem.bluenet.*;

public class Route {
	final byte[] mAddress;
	final short mPrefixLength;
	final Node mNextHop;

	public Route(byte[] address, short prefixLength, Node nextHop) {
		mAddress = address;
		mPrefixLength = prefixLength;
		mNextHop = nextHop;
	}
	
	public byte[] getAddress() {
		return mAddress;
	}
	
	public short getPrefixLength() {
		return mPrefixLength;
	}
	
	public Node getNextHop() {
		return mNextHop;
	}
	
	public boolean matchesAddress(byte[] otherAddress) {
		for (int i = 0; i < mPrefixLength; ++i) {
			// pull the bit out of both addresses
			int index = i / 8;
			int bitIx = i % 8;
			int bitmask = 0x80 >>> bitIx;
			
			if ( (mAddress[index] & bitmask) != (otherAddress[index] & bitmask) ) {
				// bits don't match => the addresses don't match
				return false;
			}
		}
		
		// if we get this far, then all of the bits matched
		return true;
	}
}

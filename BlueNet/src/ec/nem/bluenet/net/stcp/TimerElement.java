package ec.nem.bluenet.net.stcp;

public class TimerElement implements Comparable<TimerElement> {
	private long mStartTime;
	private long mWaitTime;
	
	/**
	 * Creates a new TimerElement using the current system
	 * time as it's start time.
	 * 
	 * @param waitTime
	 */
	public TimerElement(long waitTime) {
		mStartTime = System.currentTimeMillis();
		mWaitTime = waitTime;
	}
	
	public boolean isExpired() {
		final long currentTime = System.currentTimeMillis();
		if(currentTime - mStartTime > mWaitTime) {
			return true;
		}
		return false;
	}
	
	public long timeLeft() {
		return 0;
	}

	public int compareTo(TimerElement o) {
		final long expiration = mStartTime + mWaitTime;
		final long otherExpiration = o.mStartTime + o.mWaitTime;
		if(expiration < otherExpiration) {
			return -1;
		}
		else if(expiration > otherExpiration) {
			return 1;
		}
		return 0;
	}
}

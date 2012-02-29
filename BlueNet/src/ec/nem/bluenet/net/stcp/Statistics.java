package ec.nem.bluenet.net.stcp;

/**
 * Keeps track of interesting statistics about the Simple Transfer Control Protocol's
 * performance.
 * 
 * @author Darren White
 */
public class Statistics {
	/** Number of timeouts detected */
	public int nTimeouts;
	/** Retransmissions by the sender/receiver */
	public int nReTX;            
	/** Duplicate ACKs at the sender */
	public int nDuplicateACKs;
	/** Duplicate packets at the receiver */
	public int nDuplicatePkts;
	/** Number of RTOs counted */
	public int nRTOs;
	/** Average RTO estimated by sender */
	public int avgRTO;           
	/** Number of RTTs counted */
	public int nRTTs;
	/** Average RTT measured by sender */
	public double avgRTT;
	/** Average throughput in kilobits per second*/
	public double avgThroughput;
	
	public Statistics() {
		reset();
	}
	
	public void reset() {
		nTimeouts = 0;
		nReTX = 0;
		nDuplicateACKs = 0;
		nDuplicatePkts = 0;
		nRTOs = 0;
		avgRTO = 0;
		nRTTs = 0;
		avgRTT = 0;
		avgThroughput = 0;
	}
}

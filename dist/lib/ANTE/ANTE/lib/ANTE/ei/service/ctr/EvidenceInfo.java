package ei.service.ctr;

import jade.content.Concept;

/**
 * This class represents the concept of evidence info. It is used to save information to build charts
 * @author Sérgio
 *
 */
public class EvidenceInfo implements Concept
{

	/**
	 * 
	 */
	private static final long serialVersionUID = 3192032963126262058L;
	
	private double trustValue;
	private String contracID;
	
	/**
	 * Sets the new value of trust
	 * @param updatedRep the updatedRep to set
	 */
	public void setTrustValue(double trustValue) {
		this.trustValue = trustValue;
	}
	
	/**
	 * Returns the new value of trust
	 * @return the updatedRep
	 */
	public double getTrustValue() {
		return trustValue;
	}
	/**
	 * Sets the contract ID
	 * @param contracID the contracID to set
	 */
	public void setContracID(String contracID) {
		this.contracID = contracID;
	}
	/**
	 * Returns the contract ID
	 * @return the contracID
	 */
	public String getContracID() {
		return contracID;
	}
}
package ei.agent.role;

import jade.core.AID;

/**
 * Represents a bank movement between two agents of a given amount
 */
public class BankMovement {
	
	private AID from;
	private AID to;
	private Double amount;
	
	/**
	 * Class constructor
	 * 
	 * @param from		the agent who initiated the movement
	 * @param to		the agent who received the amount
	 * @param amount	the amount exchanged
	 */
	public BankMovement(AID from, AID to, Double amount){
		this.from = from;
		this.to = to;
		this.amount = amount;
	}
	
	/**
	 * Gets the amount for the movement
	 * 
	 * @return	the movement amount
	 */
	public Double getAmount(){
		return amount;
	}
	
	/**
	 * Sets the amount for the movement
	 * 
	 * @param a	the movement amount
	 */
	public void setAmount(Double a){
		amount = a;
	}
	
	/**
	 * Gets the agent who initiated the movement
	 * 
	 * @return	the aid for the agent
	 */
	public AID getFrom(){
		return from;
	}
	
	/**
	 * Sets the agent who initiated the movement
	 * 
	 * @param a	the aid for the agent
	 */
	public void setFrom(AID a){
		from = a;
	}
	
	/**
	 * Gets the agent who received the amount
	 * 
	 * @return	the aid for the agent
	 */
	public AID getTo(){
		return to;
	}
	
	/**
	 * Sets the agent who received the amount
	 * 
	 * @param a the aid for the agent
	 */
	public void setTo(AID a){
		to = a;
	}
	
}

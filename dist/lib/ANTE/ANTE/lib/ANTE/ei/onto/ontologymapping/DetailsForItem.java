package ei.onto.ontologymapping;

import jade.content.Predicate;
import jade.util.leap.List;

/**
 * This class represents the item details who are needed by the ontology service from the agent who requested the negotiation in order
 * to match possible needs in different ontologies. It contains a list of details about the attributes of the need,
 * the price and currency in which is being negotiated and a comment.
 * This list has another five lists inside. One for attributes of type string, one for integers, one for floats,
 * one for booleans, and one for relations.
 */
public class DetailsForItem implements Predicate {

	private static final long serialVersionUID = 1L;

	private List details;
	private String currency;
	private String classType;
	
//	private String price="0";
//	private String price1="0";
//	private String price2="0";	
	/**
	 * Sets the class name of the item
	 * 
	 * @param c	the new comment
	 */
	public void setClassType(String c){
		classType = c;
	}
	
	/**
	 * Gets the class name of the item
	 * 
	 * @return	the comment
	 */
	public String getClassType(){
		return classType;
	}
	
//	/**
//	 * Sets the price
//	 * 
//	 * @param p	the new price
//	 */
//	public void setPrice(String p){
//		price = p;
//	}
//	
//	
//	/**
//	 * Sets the price1 of the domain
//	 * 
//	 * @param p	the new price
//	 */
//	public void setPrice1(String p){
//		price1 = p;
//	}
//	
//	/**
//	 * Sets the price2 of the domain
//	 * 
//	 * @param p	the new price
//	 */
//	public void setPrice2(String p){
//		price2 = p;
//	}
//	
//	/**
//	 * Gets the price
//	 * 
//	 * @return	the price
//	 */
//	public String getPrice(){
//		return price;
//	}
//	
//	/**
//	 * Gets the price1 of the domain
//	 * 
//	 * @return	the price
//	 */
//	public String getPrice1(){
//		return price1;
//	}
//	
//	/**
//	 * Gets the price2 of the domain
//	 * 
//	 * @return	the price
//	 */
//	public String getPrice2(){
//		return price2;
//	}
//	
	/**
	 * Sets the currency
	 * 
	 * @param c	the new currency
	 */
	public void setCurrency(String c){
		currency = c;
	}
	
	/**
	 * Gets the currency
	 * 
	 * @return	the currency
	 */
	public String getCurrency(){
		return currency;
	}
	
	/**
	 * Sets the details list
	 * 
	 * @param l	the new details list
	 */
	public void setDetails(List l){
		details = l;
	}
	
	/**
	 * Gets the details list
	 * 
	 * @return	the details list
	 */
	public List getDetails(){
		return details;
	}
	
}

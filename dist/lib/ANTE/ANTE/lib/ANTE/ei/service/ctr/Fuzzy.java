package ei.service.ctr;

/**
 * 
 * @author Joana - 30/09/2009
 * 		deals with fuzzy values for CFP and evidence attributes (ex., quantity, deliveryTime)
 *
 */
public class Fuzzy {
	public enum quantLabels {low, medium, high};
	public enum deliveryTimeLabels {low, medium, big};
	
	public Fuzzy () {
		
	}
	
	/**
	 * get the fuzzy label of a given quantity
	 * @return label
	 */
	public quantLabels getQuantityLabel (int quant) {

		if (quant >= 0 && quant <405000)
			return quantLabels.low;
		else if (quant >= 405000 && quant <675000)
			return quantLabels.medium;
		else return quantLabels.high;
	}

	
	public deliveryTimeLabels getDeliveryTimeLabel (Long date) {

		if(date == 1000)
			return deliveryTimeLabels.low;
		else if(date == 2000)
			return deliveryTimeLabels.medium;
		else if(date == 3000)
			return deliveryTimeLabels.big;
		

		if(date >= 0 && date < 14.5)
			return deliveryTimeLabels.low;
		else if(date >= 14.5 && date < 23.5)
			return deliveryTimeLabels.medium;
		else
			return deliveryTimeLabels.big;

	}

	
}
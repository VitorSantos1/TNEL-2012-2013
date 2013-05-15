package ei.service.ctr;

import java.io.StringReader; 
import java.util.Vector;

import ei.service.ctr.OutcomeGenerator.Outcome;

import weka.classifiers.trees.Id3;
import weka.core.Instances;


/**
 * 
 * @author Joana Urbano - 09/01/2010
 *
 */
public class Stereotype {
	private Vector<ContractualHistory> evidences;
	public enum classificationMethod {CLASSF, CLUSTER};
	
	String ARFFString; 
	
	enum classNames {TRUE, FALSE, TOTAL};
	enum stereotypeMethod {INCR_FREQ, INTRA_INTER, ID3};
	public stereotypeMethod extractionMethod = stereotypeMethod.ID3;
	EvidenceClass classTotals = new EvidenceClass ();
	EvidenceClass classTrue = new EvidenceClass ();
	EvidenceClass classFalse = new EvidenceClass ();
	
	/**
	 * 
	 * @param evidences
	 * @param method
	 * @param tick
	 */
	Stereotype (Vector<ContractualHistory> evidences, String ARFF) {
		this.ARFFString = ARFF;
		this.evidences = evidences;
	}
	
	
	/**
	 * @return 0.0 if current cfp matches false stereotype, 1.0 otherwise
	 * @throws Exception
	 */
	public double extractStereotype (ei.onto.ctr.ContextualEvidence contract, String supplierName) throws Exception {
		Vector<ContractualHistory> stereotypes = new Vector<ContractualHistory> ();
		
		double returnValue = 1.0;
	
		// ------------------- extraction method = ID3
		if (extractionMethod == stereotypeMethod.ID3) {
			
			// if there is no evidences for this supplier do nothing and return
			if (evidences.isEmpty())
				return 1.0;
			
			
			// start building dataset
			ARFFString = ARFFString + "@data\n";

			// read from supplier evidences
			for (int i=0; i < evidences.size(); i++) {
				ContractualHistory evidence = evidences.elementAt(i);
				ARFFString = ARFFString + evidence.getFabric()+",";
				ARFFString = ARFFString + evidence.getQuantity()+",";
				ARFFString = ARFFString + evidence.getDeliveryTime()+",";
				ARFFString = ARFFString + evidence.getSupplierName()+",";
				ARFFString = ARFFString + evidence.getOutcome()+"\n";
			}
				
			// build ID3 classifier
			Id3 id3 = new Id3 ();
			
			StringReader arffStr = new StringReader (ARFFString);
			// put dataset in instances
//			System.out.println("::"+ARFFString+"\n"+"..."+arffStr.toString());
			Instances instances = new Instances (arffStr);;
			
			// set class attribute as last attribute
			instances.setClassIndex(instances.numAttributes()-1);
			// build tree
			try{
			id3.buildClassifier(instances);
			}catch(Exception e)
			{
				e.printStackTrace();
			}
			
			stereotypes = extractStereotypesFromId3Tree (id3, supplierName);
		
			//System.out.println("--->"+stereotypes.size());
			if(stereotypes.size() == 1)
			{
				if(stereotypes.get(0).getFabric() == null && stereotypes.get(0).getDeliveryTime() == null && stereotypes.get(0).getQuantity() == null)
				{
					if(stereotypes.get(0).getOutcome() == Outcome.Fulfilled)
						return 1.0;
					else if(stereotypes.get(0).getOutcome() == Outcome.DeadlineViolated || stereotypes.get(0).getOutcome() == Outcome.DeadlineViolatedDeadlineViolated || stereotypes.get(0).getOutcome() == Outcome.DeadlineViolatedFulfilled || stereotypes.get(0).getOutcome() == Outcome.DeadlineViolatedViolated)
						return 0.5;
					else if(stereotypes.get(0).getOutcome() == Outcome.Violated || stereotypes.get(0).getOutcome() == Outcome.ViolatedDeadlineViolated || stereotypes.get(0).getOutcome() == Outcome.ViolatedFulfilled || stereotypes.get(0).getOutcome() == Outcome.ViolatedViolated)
						return 0.0;
				}
			}
			//No information
			else if(stereotypes.isEmpty())
			{
				return 1.0;
				
			}
				
						
		} 

	/*	
		// ------------------- extraction method = INCR_FREQ
		else if (extractionMethod == stereotypeMethod.INCR_FREQ) {		
			this.setAttributeCountsPerClass ();
			

			// extract TRUE stereotype
			ContractualHistory stereotype = getStereotypeIncrFreq (Stereotype.classNames.TRUE);
				
			// add TRUE stereotype (if any) to vector of stereotypes
			if (stereotype != null) {
				// add supplier name to stereotype
				stereotype.setSupplierName(supplierName);
				stereotypes.add(stereotype);
			}					
			
			// extract FALSE stereotype
			stereotype = getStereotypeIncrFreq (Stereotype.classNames.FALSE);
				
			// add FALSE stereotype (if any) to vector of stereotypes
			if (stereotype != null) {
				// add supplier name to stereotype
				stereotype.setSupplierName(supplierName);
				stereotypes.add(stereotype);
			}
			
		}
			
			
		// ------------------- extraction method = INTRA_INTER
		else if (extractionMethod == stereotypeMethod.INTRA_INTER ) {
			this.setAttributeCountsPerClass ();
			
			// extract TRUE stereotype
			ContractualHistory stereotype = getStereotypeIntraInter (Stereotype.classNames.TRUE);
				
			// add TRUE stereotype (if any) to vector of stereotypes
			if (stereotype != null) {
				// add supplier name to stereotype
				stereotype.setSupplierName(supplierName);
				stereotypes.add(stereotype);
			}
			
			// extract FALSE stereotype
			stereotype = getStereotypeIntraInter (Stereotype.classNames.FALSE);

			// add FALSE stereotype (if any) to vector of stereotypes
			if (stereotype != null) {
				// add supplier name to stereotype
				stereotype.setSupplierName(supplierName);
				stereotypes.add(stereotype);
			}
				
		}
*/
			
		// compare current CFP to supplier stereotype, and compute contextual fitness value for this supplier		
		
		// Fuzzyfy current cfp attributes
		Fuzzy fuzzy = new Fuzzy ();
		Fuzzy.quantLabels cfpQuantity = fuzzy.getQuantityLabel(contract.getQuantity());
		Fuzzy.deliveryTimeLabels cfpDeliveryTime = fuzzy.getDeliveryTimeLabel(contract.getDeliveryTime());

		//System.out.println ("\nStereotypes:");
		
		
		for (int j=0; j < stereotypes.size(); j++) {
/*
			
			System.out.print (stereotypes.elementAt(j).getSupplierName());
			System.out.print ("; " + stereotypes.elementAt(j).getFabric());
			System.out.print ("; " + stereotypes.elementAt(j).getQuantity());
			System.out.print ("; " + stereotypes.elementAt(j).getDeliveryTime());
			System.out.print ("; " + stereotypes.elementAt(j).getResult());
			System.out.print (" (" + stereotypes.elementAt(j).getNumInstances() + ") ");
			
*/
			
			// if stereotype's fabric is not null and is different from cfp, ignore stereotype
			if (stereotypes.elementAt(j).getFabric() != null)
				if (!stereotypes.elementAt(j).getFabric().equals(contract.getNeedName()))
				{	
					continue;
				}
			
			
			// now test failure on the remaining attributes
			//////////////////////////////////
			//Todos cuja primeira obrigaçao seja Violated, tomam o valor de 0
			if ((cfpQuantity == stereotypes.elementAt(j).getQuantity())
					&& (cfpDeliveryTime == stereotypes.elementAt(j).getDeliveryTime())
					&& (stereotypes.elementAt(j).getResult() == ContractualHistory.resultLabels.Violated || 
						stereotypes.elementAt(j).getResult() == ContractualHistory.resultLabels.ViolatedFulfilled || 
						stereotypes.elementAt(j).getResult() == ContractualHistory.resultLabels.ViolatedDeadlineViolated ||
						stereotypes.elementAt(j).getResult() == ContractualHistory.resultLabels.ViolatedViolated)) {
				returnValue = 0.0;
			}
			else if (((cfpQuantity == stereotypes.elementAt(j).getQuantity())
					&& (stereotypes.elementAt(j).getDeliveryTime() == null)
					|| (cfpDeliveryTime == stereotypes.elementAt(j).getDeliveryTime()
					&& stereotypes.elementAt(j).getQuantity() == null))
					&& (stereotypes.elementAt(j).getResult() == ContractualHistory.resultLabels.Violated || 
						stereotypes.elementAt(j).getResult() == ContractualHistory.resultLabels.ViolatedFulfilled || 
						stereotypes.elementAt(j).getResult() == ContractualHistory.resultLabels.ViolatedDeadlineViolated ||
						stereotypes.elementAt(j).getResult() == ContractualHistory.resultLabels.ViolatedViolated))  {
				returnValue = 0.0;
			}
			else if (stereotypes.elementAt(j).getQuantity() == null && stereotypes.elementAt(j).getDeliveryTime() == null
					&& (stereotypes.elementAt(j).getResult() == ContractualHistory.resultLabels.Violated || 
							stereotypes.elementAt(j).getResult() == ContractualHistory.resultLabels.ViolatedFulfilled || 
							stereotypes.elementAt(j).getResult() == ContractualHistory.resultLabels.ViolatedDeadlineViolated ||
							stereotypes.elementAt(j).getResult() == ContractualHistory.resultLabels.ViolatedViolated)) {
				returnValue = 0.0;
			}
			//////////////////////////////
			//Todos cuja primeira obrigaçao seja DeadlineViolated, tomam o valor de 0.5
			else if ((cfpQuantity == stereotypes.elementAt(j).getQuantity())
					&& (cfpDeliveryTime == stereotypes.elementAt(j).getDeliveryTime())
					&& (stereotypes.elementAt(j).getResult() == ContractualHistory.resultLabels.DeadlineViolated || 
						stereotypes.elementAt(j).getResult() == ContractualHistory.resultLabels.DeadlineViolatedFulfilled || 
						stereotypes.elementAt(j).getResult() == ContractualHistory.resultLabels.DeadlineViolatedDeadlineViolated ||
						stereotypes.elementAt(j).getResult() == ContractualHistory.resultLabels.DeadlineViolatedViolated)){
				returnValue = 0.5;
			}
			else if (((cfpQuantity == stereotypes.elementAt(j).getQuantity())
					&& (stereotypes.elementAt(j).getDeliveryTime() == null)
					|| (cfpDeliveryTime == stereotypes.elementAt(j).getDeliveryTime()
					&& stereotypes.elementAt(j).getQuantity() == null))
					&& (stereotypes.elementAt(j).getResult() == ContractualHistory.resultLabels.DeadlineViolated || 
						stereotypes.elementAt(j).getResult() == ContractualHistory.resultLabels.DeadlineViolatedFulfilled || 
						stereotypes.elementAt(j).getResult() == ContractualHistory.resultLabels.DeadlineViolatedDeadlineViolated ||
						stereotypes.elementAt(j).getResult() == ContractualHistory.resultLabels.DeadlineViolatedViolated)) {
				returnValue = 0.5;
			}
			else if (stereotypes.elementAt(j).getQuantity() == null && stereotypes.elementAt(j).getDeliveryTime() == null
					&& (stereotypes.elementAt(j).getResult() == ContractualHistory.resultLabels.DeadlineViolated || 
							stereotypes.elementAt(j).getResult() == ContractualHistory.resultLabels.DeadlineViolatedFulfilled || 
							stereotypes.elementAt(j).getResult() == ContractualHistory.resultLabels.DeadlineViolatedDeadlineViolated ||
							stereotypes.elementAt(j).getResult() == ContractualHistory.resultLabels.DeadlineViolatedViolated)) {
				returnValue = 0.5;
			}
			
			
		}
		return returnValue;
	}
	
	
	
	public void setAttributeCountsPerClass () {
		ContractualHistory evd;
		Vector<EvidenceClass> evdClasses = new Vector<EvidenceClass> ();
		
	
		// separate supplier evidences into classes TRUE and FALSE
		Vector<ContractualHistory> supplierPosEvidences = new Vector<ContractualHistory> ();
		Vector<ContractualHistory> supplierNegEvidences = new Vector<ContractualHistory> ();

		for (int i=0; i < evidences.size(); i++) {
			evd = evidences.elementAt(i);					
			if (evd.getOutcome().equals(OutcomeGenerator.Outcome.Fulfilled)) {
				supplierPosEvidences.add(evd); 
			}
			else {
				supplierNegEvidences.add(evd);
			}	
		}
	
		
		// set class TOTALS
		classTotals.setNumClassInstances(evidences.size());
		//System.out.println ("\ntotalInstances: " + totalInstances);
		classTotals.setClassName(classNames.TOTAL);
		
		// set class TRUE
		classTrue.classEvidences = supplierPosEvidences;
		classTrue.setNumClassInstances (classTrue.classEvidences.size());
		classTrue.classOutcome = true;
		classTrue.setClassName(classNames.TRUE);
		evdClasses.add(classTrue);

		// set class FALSE
		classFalse.classEvidences = supplierNegEvidences;
		classFalse.setNumClassInstances (classFalse.classEvidences.size());
		classFalse.classOutcome = false;
		classFalse.setClassName(classNames.FALSE);
		evdClasses.add(classFalse);

		
		// attribute counts for true and false classes, and totals
		EvidenceClass evdClass = new EvidenceClass ();
		for (int c=0; c < evdClasses.size(); c++) {
			evdClass = evdClasses.elementAt(c);
			
			// count number of each attribute values
			for (int i=0; i < evdClass.classEvidences.size(); i++) {
				evd = evdClass.classEvidences.elementAt(i);
				if (evd.getFabric().equals("Good.GOOD_VALUES[0]")) {
					evdClass.numClassFabricCotton++;
					classTotals.incrNumClassFabricCotton();
				}
				else if (evd.getFabric().equals("LL")) {
					evdClass.numClassFabricChiffon++;
					classTotals.incrNumClassFabricChiffon();
				}
				else {
					evdClass.numClassFabricVoile++;
					classTotals.incrNumClassFabricVoile();
				}
			
				switch (evd.getQuantity()) {
					case low: 
						evdClass.numClassQuantLow++;
						classTotals.incrNumClassQuantLow();
						break;
					case medium: 
						evdClass.numClassQuantMedium++;
						classTotals.incrNumClassQuantMedium();
						break;
					case high: 
						evdClass.numClassQuantHigh++;
						classTotals.incrNumClassQuantHigh();
						break;
				}
				switch (evd.getDeliveryTime()) {
					case low: 
						evdClass.numClassDtimeLow++;
						classTotals.incrNumClassDtimeLow();
						break;
					case medium: 
						evdClass.numClassDtimeMedium++;
						classTotals.incrNumClassDtimetMedium();
						break;
					case big: 
						evdClass.numClassDtimeBig++;
						classTotals.incrNumClassDtimeBig();
						break;
				}
			}									
		} // for each class of evidences
	}
	
	

	/**
	 * 
	 * JU: 24/03/2010
	 * @param clName
	 * @return
	 */
	/*ContractualHistory getStereotypeIncrFreq (Stereotype.classNames clName) {

		// System.out.println ("Totals Fabric: " + classTotals.getNumClassFabricCotton() + ";" + classTotals.getNumClassFabricChiffon() + ";" + classTotals.numClassFabricVoile);
		// System.out.println ("Totals Quant: " + totalQuantLow + ";" + totalQuantMedium + ";" + totalQuantHigh);
		// System.out.println ("Totals Dtime: " + totalDtimeLow + ";" + totalDtimeMedium + ";" + totalDtimeBig);
		// System.out.println ("\n");

		
		double stereoThreshold = 0.25;
		
		ContractualHistory stereotype = new ContractualHistory ();
		EvidenceClass evdClass = new EvidenceClass ();
		
		// whether current class is class TRUE or class FALSE
		if (clName == Stereotype.classNames.TRUE) {
			evdClass = classTrue;
		}
		else if (clName == Stereotype.classNames.FALSE) {
			evdClass = classFalse;
		}
		
		int classInstances = evdClass.getNumClassInstances();
		int totalInstances = classTotals.getNumClassInstances();
		
		
		// System.out.println ("classInstances: " + classInstances);
		if (classInstances == 0)
			return null;
		
		
		// if there is just one class
		if (classInstances == classTotals.getNumClassInstances()) {
			// quantity=low
			if ((Math.pow(evdClass.getNumClassQuantLow()/(double)classInstances, 2) >= (1-stereoThreshold))) {
				stereotype.setQuantity(Fuzzy.quantLabels.low);
				//System.out.println ("QuantLow is significative in cluster");
			}
		
			// quantity=medium
			if ((Math.pow(evdClass.getNumClassQuantMedium ()/(double)classInstances, 2) >= (1-stereoThreshold))) {
				stereotype.setQuantity(Fuzzy.quantLabels.medium);
				//System.out.println ("QuantMedium is significative in cluster");
			}
		
			// quantity=high
			if ((Math.pow(evdClass.getNumClassQuantHigh ()/(double)classInstances, 2) >= (1-stereoThreshold))) {
				stereotype.setQuantity(Fuzzy.quantLabels.high);
				//System.out.println ("QuantHigh is significative in cluster");
			}
		
			// dtime=low
			if ((Math.pow(evdClass.getNumClassDtimeLow ()/(double)classInstances, 2) >= (1-stereoThreshold))) {
				stereotype.setDeliveryTime(Fuzzy.deliveryTimeLabels.low);
				//System.out.println ("DtimeLow is significative in cluster");
			}
		
			// dtime=medium
			if ((Math.pow(evdClass.getNumClassDtimetMedium ()/(double)classInstances, 2) >= (1-stereoThreshold))) {
				stereotype.setDeliveryTime(Fuzzy.deliveryTimeLabels.medium);
				//System.out.println ("DtimeMedium is significative in cluster");
			}
		
			// dtime=big
			if ((Math.pow((evdClass.getNumClassDtimeBig ()/(double)classInstances), 2) >= (1-stereoThreshold))) {
				stereotype.setDeliveryTime(Fuzzy.deliveryTimeLabels.big);
				//System.out.println ("DtimeBig is significative in cluster");
			}
		
			// fabric=cotton
			if ((Math.pow((evdClass.getNumClassFabricCotton ()/(double)classInstances), 2) >= (1-stereoThreshold))) {
				stereotype.setFabric("cotton");
				//System.out.println ("FabricCotton is significative in cluster");
			}
		
			// fabric=chiffon
			if ((Math.pow((evdClass.getNumClassFabricChiffon ()/(double)classInstances), 2) >= (1-stereoThreshold))) {
				stereotype.setFabric("chiffon");
				//System.out.println ("FabricChiffon is significative in cluster");
			}

			// fabric=voile
			if ((Math.pow((evdClass.getNumClassFabricVoile ()/(double)classInstances), 2) >= (1-stereoThreshold))) {
				stereotype.setFabric("voile");
				//System.out.println ("FabricVoile is significative in cluster");
			}
		}
		
		// there are both classes true and false
		else {
		
			//System.out.println ("cotton in class: " + Math.pow((evdClass.getNumClassFabricCotton ()/(double)classInstances), 2));
			//System.out.println ("cotton total: " + Math.pow((totalFabricCotton/(double)totalInstances), 2));
			//System.out.println ("cotton difference: " + (Math.pow((evdClass.getNumClassFabricCotton ()/(double)classInstances), 2) - (Math.pow((totalFabricCotton/(double)totalInstances), 2))));
		
			// quantity=low
			if ((Math.pow(evdClass.getNumClassQuantLow()/(double)classInstances, 2) - (Math.pow(classTotals.getNumClassQuantLow()/(double)totalInstances, 2)) >= stereoThreshold)) {
				stereotype.setQuantity(Fuzzy.quantLabels.low);
				//System.out.println ("QuantLow is significative in cluster");
			}
		
			// quantity=medium
			if ((Math.pow(evdClass.getNumClassQuantMedium ()/(double)classInstances, 2) - (Math.pow(classTotals.getNumClassQuantMedium()/(double)totalInstances, 2)) >= stereoThreshold)) {
				stereotype.setQuantity(Fuzzy.quantLabels.medium);
				//System.out.println ("QuantMedium is significative in cluster");
			}
		
			// quantity=high
			if ((Math.pow(evdClass.getNumClassQuantHigh ()/(double)classInstances, 2) - (Math.pow(classTotals.getNumClassQuantHigh()/(double)totalInstances, 2)) >= stereoThreshold)) {
				stereotype.setQuantity(Fuzzy.quantLabels.high);
				//System.out.println ("QuantHigh is significative in cluster");
			}
		
			// dtime=low
			if ((Math.pow(evdClass.getNumClassDtimeLow ()/(double)classInstances, 2) - (Math.pow(classTotals.getNumClassDtimeLow()/(double)totalInstances, 2)) >= stereoThreshold)) {
				stereotype.setDeliveryTime(Fuzzy.deliveryTimeLabels.low);
				//System.out.println ("DtimeLow is significative in cluster");
			}
		
			// dtime=medium
			if ((Math.pow(evdClass.getNumClassDtimetMedium ()/(double)classInstances, 2) - (Math.pow(classTotals.getNumClassDtimetMedium()/(double)totalInstances, 2)) >= stereoThreshold)) {
				stereotype.setDeliveryTime(Fuzzy.deliveryTimeLabels.medium);
				//System.out.println ("DtimeMedium is significative in cluster");
			}
		
			// dtime=big
			if ((Math.pow((evdClass.getNumClassDtimeBig ()/(double)classInstances), 2) - (Math.pow((classTotals.getNumClassDtimeBig()/(double)totalInstances), 2)) >= stereoThreshold)) {
				stereotype.setDeliveryTime(Fuzzy.deliveryTimeLabels.big);
				//System.out.println ("DtimeBig is significative in cluster");
			}
		
			// fabric=cotton
			if ((Math.pow((evdClass.getNumClassFabricCotton ()/(double)classInstances), 2) - (Math.pow((classTotals.getNumClassFabricCotton()/(double)totalInstances), 2)) >= stereoThreshold)) {
				stereotype.setFabric("cotton");
				//System.out.println ("FabricCotton is significative in cluster");
			}
		
			// fabric=chiffon
			if ((Math.pow((evdClass.getNumClassFabricChiffon ()/(double)classInstances), 2) - (Math.pow((classTotals.getNumClassFabricChiffon()/(double)totalInstances), 2)) >= stereoThreshold)) {
				stereotype.setFabric("chiffon");
				//System.out.println ("FabricChiffon is significative in cluster");
			}

			// fabric=voile
			if ((Math.pow((evdClass.getNumClassFabricVoile ()/(double)classInstances), 2) - (Math.pow((classTotals.getNumClassFabricVoile()/(double)totalInstances), 2)) >= stereoThreshold)) {
				stereotype.setFabric("voile");
				//System.out.println ("FabricVoile is significative in cluster");
			}
		
		}
		
		// validate stereotype (fabric=null, quantity=null and dtime=null is not valid!)
		if (stereotype.getFabric() == null && stereotype.getQuantity() == null && stereotype.getDeliveryTime() == null) {
			return null;
		}
		
		
		// add outcome of evidence to stereotype
		if (evdClass.getClassOutcome() == true) {
			stereotype.setResult(ContractualHistory.resultLabels.True);
		}
		else if (evdClass.getClassOutcome() == false) {
			stereotype.setResult(ContractualHistory.resultLabels.False);
		}
		
		//stereotype.setOutcome(evdClass.getClassOutcome());
		
		
		// number of instances (in case there are two stereotypes with contradictory result/outcome)
		stereotype.setNumInstances(classInstances);

		return stereotype;	
	}
	
	
	*/
	/**
	 * JU - 24/03/2010
	 * INTRA_INTER: product of intra and inter class similarities 
	 *		P(Ai=Vij|Ck).P(Ck|Ai=Vij)
	 */
	/*ContractualHistory getStereotypeIntraInter (Stereotype.classNames clName) {
		
		double stereoThreshold = 0.50;
		ContractualHistory stereotype = new ContractualHistory ();
		EvidenceClass evdClass = new EvidenceClass ();
		
		// whether current class is class TRUE or class FALSE
		if (clName == Stereotype.classNames.TRUE) {
			evdClass = classTrue;
		}
		else if (clName == Stereotype.classNames.FALSE) {
			evdClass = classFalse;
		}
		
		int classInstances = evdClass.getNumClassInstances();
			
		// System.out.println ("classInstances: " + classInstances);
		if (classInstances == 0)
			return null;
		
		int totalQuantLow = classTotals.getNumClassQuantLow();
		int totalQuantMedium = classTotals.getNumClassQuantMedium();
		int totalQuantHigh = classTotals.getNumClassQuantHigh();
		int totalDtimeLow = classTotals.getNumClassDtimeLow();
		int totalDtimeMedium = classTotals.getNumClassDtimetMedium();
		int totalDtimeBig = classTotals.getNumClassDtimeBig();
		int totalFabricCotton = classTotals.getNumClassFabricCotton();
		int totalFabricChiffon = classTotals.getNumClassFabricChiffon();
		int totalFabricVoile = classTotals.getNumClassFabricVoile();
		
		double interClassQuantLow = 0;
		double interClassQuantMedium = 0;
		double interClassQuantHigh = 0;
		double interClassDtimeLow = 0;
		double interClassDtimeMedium = 0;
		double interClassDtimeBig = 0;
		double interClassFabricCotton = 0;
		double interClassFabricChiffon = 0;
		double interClassFabricVoile = 0;
		
		
		if (totalQuantLow != 0) interClassQuantLow = evdClass.getNumClassQuantLow()/(double)totalQuantLow;
		if (totalQuantMedium != 0) interClassQuantMedium = evdClass.getNumClassQuantMedium()/(double)totalQuantMedium;
		if (totalQuantHigh != 0) interClassQuantHigh = evdClass.getNumClassQuantHigh()/(double)totalQuantHigh;
		if (totalDtimeLow != 0) interClassDtimeLow = evdClass.getNumClassDtimeLow()/(double)totalDtimeLow;
		if (totalDtimeMedium != 0) interClassDtimeMedium = evdClass.getNumClassDtimetMedium()/(double)totalDtimeMedium;
		if (totalDtimeBig != 0) interClassDtimeBig = evdClass.getNumClassDtimeBig()/(double)totalDtimeBig;
		if (totalFabricCotton != 0) interClassFabricCotton = evdClass.getNumClassFabricCotton()/(double)totalFabricCotton;
		if (totalFabricChiffon != 0) interClassFabricChiffon = evdClass.getNumClassFabricChiffon()/(double)totalFabricChiffon;
		if (totalFabricVoile != 0) interClassFabricVoile = evdClass.getNumClassFabricVoile()/(double)totalFabricVoile;
		
		/*
		System.out.println (c+": (quantLow) " + numClassQuantLow/(double)classInstances * interClassQuantLow);
		System.out.println (c+": (quantMedium) " + numClassQuantMedium/(double)classInstances * interClassQuantMedium);
		System.out.println (c+": (quantHigh) " + numClassQuantHigh/(double)classInstances * interClassQuantHigh);
		System.out.println (c+": (dtimeLow) " + numClassDtimeLow/(double)classInstances * interClassDtimeLow);
		System.out.println (c+": (dtimeMedium) " + numClassDtimeMedium/(double)classInstances * interClassDtimeMedium);
		System.out.println (c+": (dtimeBig) " + numClassDtimeBig/(double)classInstances * interClassDtimeBig);
		System.out.println (c+": (fabricCotton) " + numClassFabricCotton/(double)classInstances * interClassFabricCotton);
		System.out.println (c+": (fabricChiffon) " + numClassFabricChiffon/(double)classInstances * interClassFabricChiffon);
		System.out.println (c+": (fabricVoile) " + numClassFabricVoile/(double)classInstances * interClassFabricVoile);
		*/

		/*
		// quantity=low			
		if (evdClass.getNumClassQuantLow()/(double)classInstances * interClassQuantLow > stereoThreshold) {
			stereotype.setQuantity(Fuzzy.quantLabels.low);
		}
		// quantity=medium
		if (evdClass.getNumClassQuantMedium()/(double)classInstances * interClassQuantMedium > stereoThreshold) {
			stereotype.setQuantity(Fuzzy.quantLabels.medium);
		}
		// quantity=high
		if (evdClass.getNumClassQuantHigh()/(double)classInstances * interClassQuantHigh > stereoThreshold) {
			stereotype.setQuantity(Fuzzy.quantLabels.high);
		}
		// dtime=low
		if (evdClass.getNumClassDtimeLow()/(double)classInstances * interClassDtimeLow > stereoThreshold) {
			stereotype.setDeliveryTime(Fuzzy.deliveryTimeLabels.low);
		}
		// dtime=medium
		if (evdClass.getNumClassDtimetMedium()/(double)classInstances * interClassDtimeMedium > stereoThreshold) {
			stereotype.setDeliveryTime(Fuzzy.deliveryTimeLabels.medium);
		}
		// dtime=big
		if (evdClass.getNumClassDtimeBig()/(double)classInstances * interClassDtimeBig > stereoThreshold) {
			stereotype.setDeliveryTime(Fuzzy.deliveryTimeLabels.big);
		}
		// fabric=cotton
		if (evdClass.getNumClassFabricCotton()/(double)classInstances * interClassFabricCotton > stereoThreshold) {
			stereotype.setFabric("cotton");
		}
		// fabric=chiffon
		if (evdClass.getNumClassFabricChiffon()/(double)classInstances * interClassFabricChiffon > stereoThreshold) {
			stereotype.setFabric("chiffon");
		}
		// fabric=voile
		if (evdClass.getNumClassFabricVoile()/(double)classInstances * interClassFabricVoile > stereoThreshold) {
			stereotype.setFabric("voile");
		}

		
		// validate stereotype (fabric=null, quantity=null and dtime=null is not valid!)
		if (stereotype.getFabric() == null && stereotype.getQuantity() == null && stereotype.getDeliveryTime() == null) {
			return null;
		}

		
		// add outcome of evidence to stereotype
		if (evdClass.getClassOutcome() == true) {
			stereotype.setResult(ContractualHistory.resultLabels.True);
		}
		else if (evdClass.getClassOutcome() == false) {
			stereotype.setResult(ContractualHistory.resultLabels.False);
		}

		//stereotype.setOutcome(evdClass.getClassOutcome());
		
		// number of instances (in case there are two stereotypes with contradictory result/outcome)
		stereotype.setNumInstances(classInstances);

		return stereotype;
	}
	*/
	
	/**
	 * 
	 * Transform a ID3 rule in an individual stereotype
	 * v250310 (JU)  
	 * v080510 (JU): outcome is replaced by result, in order to accommodate "null" results from ID3
	 * @param lines
	 * @return ContractualHistory - stereotype
	 */
	ContractualHistory getStereotypeId3 (String line) {
		//System.out.println("---"+line);
		ContractualHistory sttype = new ContractualHistory ();
		String[] str = line.split(":");
		// prevent cases such as :false
		if (str[0].isEmpty()) {
			return null;
		}
		// isolate the type of stereotype by result (true, false or null)
		if (str[1].contains("Fulfilled")) {
			sttype.setResult(ContractualHistory.resultLabels.Fulfilled);
		}
		else if (str[1].contains("Violated")) {
			sttype.setResult(ContractualHistory.resultLabels.Violated);
		}
		else if (str[1].contains("DeadlineViolated")) {
			sttype.setResult(ContractualHistory.resultLabels.DeadlineViolated);
		}
		else if (str[1].contains("null")) {
			sttype.setResult(ContractualHistory.resultLabels.Null);
		}
		
		// isolate each attribute
		str = line.split(";");
		for (int i=0; i<str.length; i++) {
			// isolate attribute value
			String[] attrStr = str[i].split("= ");
			// fabric = cotton
			if (attrStr[0].contains("good")) {
				String[] strr = attrStr[1].split(":");
				sttype.setFabric(strr[0]);
			}
			
			// quant = low
			else if (attrStr[0].contains("quant") && attrStr[1].contains("low")) {
				sttype.setQuantity(Fuzzy.quantLabels.low);
			}
			// quant = medium
			else if (attrStr[0].contains("quant") && attrStr[1].contains("medium")) {
				sttype.setQuantity(Fuzzy.quantLabels.medium);
			}
			// quant = high
			else if (attrStr[0].contains("quant") && attrStr[1].contains("high")) {
				sttype.setQuantity(Fuzzy.quantLabels.high);
			}
			// dtime = low
			else if (attrStr[0].contains("dtime") && attrStr[1].contains("low")) {
				sttype.setDeliveryTime(Fuzzy.deliveryTimeLabels.low);
			}
			// dtime = medium
			else if (attrStr[0].contains("dtime") && attrStr[1].contains("medium")) {
				sttype.setDeliveryTime(Fuzzy.deliveryTimeLabels.medium);
			}
			// dtime = big
			else if (attrStr[0].contains("dtime") && attrStr[1].contains("big")) {
				sttype.setDeliveryTime(Fuzzy.deliveryTimeLabels.big);
			}
		}

		// validate stereotype (fabric=null, quantity=null and dtime=null is not valid!)
		if (sttype.getFabric() == null && sttype.getQuantity() == null && sttype.getDeliveryTime() == null) {
			return null;
		}

		
		return sttype;
	}
	
	
	/**
	 * Parse a ID3 tree and return the corresponding behaviour stereotypes
	 * @param id3 tree as returned by Id3 classifier
	 * @param supplierName
	 * @return vector with the extracted stereotypes 
	 */
	public Vector<ContractualHistory> extractStereotypesFromId3Tree (Id3 id3, String supplierName) {

		Vector<ContractualHistory> extractedStypes = new Vector<ContractualHistory> ();
		
		// get classification tree from evidences
		String id3Tree = id3.toString();	
		//System.out.println(supplierName+"\\\\"+id3Tree);
		
		// parse ID3 into stereotypes	
		String [] treeLines = id3Tree.split(("\r\n|\r|\n"));
		
		String headLine = "", headLine2 = "";
		for (int i=0; i < treeLines.length; i++) {
			
			if (treeLines[i].isEmpty()) {
				continue;
			}
			
			// if : false, supplier always fails
			if (treeLines[i].startsWith(": Violated")) {
				ContractualHistory stereotype = new ContractualHistory();
				stereotype.setOutcome(Outcome.Violated);
				extractedStypes.add(stereotype);
				return extractedStypes;
			}
			if (treeLines[i].startsWith(": ViolatedFulfilled")) {
				ContractualHistory stereotype = new ContractualHistory();
				stereotype.setOutcome(Outcome.Violated);
				extractedStypes.add(stereotype);
				return extractedStypes;
			}
			if (treeLines[i].startsWith(": ViolatedDeadlineViolated")) {
				ContractualHistory stereotype = new ContractualHistory();
				stereotype.setOutcome(Outcome.Violated);
				extractedStypes.add(stereotype);
				return extractedStypes;
			}
			if (treeLines[i].startsWith(": ViolatedViolated")) {
				ContractualHistory stereotype = new ContractualHistory();
				stereotype.setOutcome(Outcome.Violated);
				extractedStypes.add(stereotype);
				return extractedStypes;
			}
			if (treeLines[i].startsWith(": DeadlineViolated")) {
				ContractualHistory stereotype = new ContractualHistory();
				stereotype.setOutcome(Outcome.DeadlineViolated);
				extractedStypes.add(stereotype);
				return extractedStypes;
			}
			if (treeLines[i].startsWith(": DeadlineViolatedFulfilled")) {
				ContractualHistory stereotype = new ContractualHistory();
				stereotype.setOutcome(Outcome.DeadlineViolated);
				extractedStypes.add(stereotype);
				return extractedStypes;
			}
			if (treeLines[i].startsWith(": DeadlineViolatedDeadlineViolated")) {
				ContractualHistory stereotype = new ContractualHistory();
				stereotype.setOutcome(Outcome.DeadlineViolated);
				extractedStypes.add(stereotype);
				return extractedStypes;
			}
			if (treeLines[i].startsWith(": DeadlineViolatedViolated")) {
				ContractualHistory stereotype = new ContractualHistory();
				stereotype.setOutcome(Outcome.DeadlineViolated);
				extractedStypes.add(stereotype);
				return extractedStypes;
			}
			if(treeLines[i].startsWith(": Fulfilled")){
				ContractualHistory stereotype = new ContractualHistory();
				stereotype.setOutcome(Outcome.Fulfilled);
				extractedStypes.add(stereotype);
				return extractedStypes; 
			}
					
			//TODO: handle : true and : null
			
			// start AND condition
			if ((treeLines[i].charAt(0) != '|') && (!treeLines[i].contains(":"))) {
				headLine = treeLines[i];
			}
			// start AND - second level
			if ((treeLines[i].charAt(0) == '|') && (!treeLines[i].contains(":"))) {
				headLine2 = treeLines[i];
			}
			// simple condition
			else if ((treeLines[i].charAt(0) != '|') && (treeLines[i].contains(":"))) {
				ContractualHistory stereotype = getStereotypeId3 (treeLines[i]);
				if (stereotype != null) {
					stereotype.setSupplierName(supplierName);
					extractedStypes.add(stereotype);
				}
			}
			// end condition - second level
			else if ((treeLines[i].contains("|  |"))) {
				String line = headLine + ";" + headLine2 + ";" + treeLines[i];
				ContractualHistory stereotype = getStereotypeId3 (line);
				if (stereotype != null) {
					stereotype.setSupplierName(supplierName);
					extractedStypes.add(stereotype);
				}
			}
				
			// end condition - first level
			//else if ((!treeLines[i].contains("|  |")) &&(treeLines[i].contains("false"))) {
			else if (treeLines[i].charAt(0) == '|') {
				String line = headLine + ";" + treeLines[i];
				ContractualHistory stereotype = getStereotypeId3 (line);
				if (stereotype != null) {
					stereotype.setSupplierName(supplierName);
					extractedStypes.add(stereotype);
				}	
			}
		}	

		
		
		return extractedStypes;
	}
	
	/**
	 * 
	 * @author Joana Urbano - 14/01/2010
	 *		Inner class  	
	 *
	 */
	public class EvidenceClass {
		Vector<ContractualHistory> classEvidences;
		private int numClassQuantLow;
		private int numClassQuantMedium;
		private int numClassQuantHigh;		
		private int numClassDtimeLow;
		private int numClassDtimeMedium;
		private int numClassDtimeBig;
		private int numClassFabricCotton;
		private int numClassFabricChiffon;
		private int numClassFabricVoile;
		private int numClassInstances;
		boolean classOutcome;
		Stereotype.classNames className;
	
		EvidenceClass () {
			classEvidences = new Vector<ContractualHistory> ();
			numClassQuantLow = 0;
			numClassQuantMedium = 0;
			numClassQuantHigh = 0;		
			numClassDtimeLow = 0;
			numClassDtimeMedium = 0;
			numClassDtimeBig = 0;
			numClassFabricCotton = 0;
			numClassFabricChiffon = 0;
			numClassFabricVoile = 0;
		}
		
		public void setNumClassInstances (int numClassInstances) {
			this.numClassInstances = numClassInstances;
		}
		
		public int getNumClassInstances () {
			return numClassInstances;
		}
		
		
		public void incrNumClassQuantLow () {
			numClassQuantLow++;
		}
		
		public void incrNumClassQuantMedium () {
			numClassQuantMedium++;
		}
		
		public void incrNumClassQuantHigh () {
			numClassQuantHigh++;
		}

		public void incrNumClassDtimeLow () {
			numClassDtimeLow++;
		}
		
		public void incrNumClassDtimetMedium () {
			numClassDtimeMedium++;
		}
		
		public void incrNumClassDtimeBig () {
			numClassDtimeBig++;
		}
		
		public void incrNumClassFabricCotton () {
			numClassFabricCotton++;
		}
		
		public void incrNumClassFabricChiffon () {
			numClassFabricChiffon++;
		}
		
		public void incrNumClassFabricVoile () {
			numClassFabricVoile++;
		}
		
		
		public int getNumClassQuantLow () {
			return numClassQuantLow;
		}
		
		public int getNumClassQuantMedium () {
			return numClassQuantMedium;
		}
		
		public int getNumClassQuantHigh () {
			return numClassQuantHigh;
		}

		public int getNumClassDtimeLow () {
			return numClassDtimeLow;
		}
		
		public int getNumClassDtimetMedium () {
			return numClassDtimeMedium;
		}
		
		public int getNumClassDtimeBig () {
			return numClassDtimeBig;
		}
		
		public int getNumClassFabricCotton () {
			return numClassFabricCotton;
		}
		
		public int getNumClassFabricChiffon () {
			return numClassFabricChiffon;
		}
		
		public int getNumClassFabricVoile () {
			return numClassFabricVoile;
		}
		
		public boolean getClassOutcome () {
			return classOutcome;
		}

		public void setClassName (Stereotype.classNames name) {
			className = name;
		}
		
		public Stereotype.classNames getClassName () {
			return className;
		}
		
	}

}
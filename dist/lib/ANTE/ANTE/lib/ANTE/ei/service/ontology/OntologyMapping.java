package ei.service.ontology;

import ei.ElectronicInstitution;
import ei.agent.enterpriseagent.ontology.Attribute;
import ei.agent.enterpriseagent.ontology.Competence;
import ei.agent.enterpriseagent.ontology.Need;
import ei.onto.ontologymapping.OntologyMappingOntology;
import ei.service.PlatformService;
import ei.onto.ontologymapping.ItemMapping;
import ei.onto.ontologymapping.FindMapping;

import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import jade.content.AgentAction;
import jade.content.ContentElement;
import jade.content.lang.Codec;
import jade.content.onto.OntologyException;
import jade.content.onto.basic.Action;
import jade.core.Agent;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.proto.AchieveREResponder;
import jade.proto.SimpleAchieveREResponder;
import jade.domain.FIPANames;
import jade.util.leap.ArrayList;
import jade.util.leap.List;

/**
 * This class represents an agent who provides ontology related services.
 * Changed by DDT
 */
public class OntologyMapping extends PlatformService {
	private static final long serialVersionUID = -898353523811513070L;
	
	private static final double MIN_VALUE_NGRAM = 0.5;
	public static double MINIMUM_CONFIDENCE_VALUE = 0.55;
	public static double WEAK_CONFIDENCE_VALUE = 0.6;
	public static double MODERATE_CONFIDENCE_VALUE = 0.7;
	
	private String host = "127.0.0.1";
	private String port = "6180";
	private String wordnetSimilarityFile = "WordNetSimilarity.dat";
	
	protected void setup() {
		super.setup();
		
		addBehaviour(new OntologyMappingResp(this));
		
		getContentManager().registerOntology(OntologyMappingOntology.getInstance());
		
		//get the host and port configurations for the wordnet server
		if(getConfigurationArguments().containsKey("wordnet_host") && getConfigurationArguments().containsKey("wordnet_port")){
			host = getConfigurationArguments().getProperty("wordnet_host").toString();
			port = getConfigurationArguments().getProperty("wordnet_port").toString();		
		} else {
			System.err.println("ERROR: host/port not defined!");
		}
		
		if(getConfigurationArguments().containsKey("wordnet_file")){
			wordnetSimilarityFile = getConfigurationArguments().getProperty("wordnet_file");
//			System.out.println(getLocalName() + ", " + wordnetSimilarityFile);
		}
		
	}
	
	
	/** 
	 * Handles the request of Supplier Enterprise Agents (SEAg) for mapping a need to a competence.
	 * After receiving the requestMsg from SEAg this method initiates a request...
	 * 
	 * @author Bernd
	 */
	private class OntologyMappingResp extends SimpleAchieveREResponder {
		private static final long serialVersionUID = 882964103647692409L;
		
		// action received in the request message
		AgentAction request_action;
		
		public OntologyMappingResp(Agent agent) {
			super(agent, MessageTemplate.and( AchieveREResponder.createMessageTemplate(FIPA_REQUEST), 
									MessageTemplate.MatchOntology(ElectronicInstitution.ONTOLOGY_MAPPING_ONTOLOGY)) );
		}
		
		protected ACLMessage prepareResponse(ACLMessage request) {
			ACLMessage reply = request.createReply();
			
			try {
				ContentElement ce = getContentManager().extractContent(request);
				if(ce instanceof Action) {
					request_action = (AgentAction) ((Action) ce).getAction();
				}
			} catch(Codec.CodecException cex){
				cex.printStackTrace();
			} catch(OntologyException oe){
				oe.printStackTrace();
			}

			if(request_action instanceof FindMapping) {
				//reply.setPerformative(ACLMessage.AGREE);
				//return reply;
				return null;
			} else {
				reply.setPerformative(ACLMessage.NOT_UNDERSTOOD);
				return reply;
			}
		}
		
		protected  ACLMessage prepareResultNotification(ACLMessage request, ACLMessage response) {
			ACLMessage result = request.createReply();

			if(request_action instanceof FindMapping) {
				FindMapping findMapping = (FindMapping) request_action;

				ItemMapping itemMapping = match(findMapping.getTargetNeed(), findMapping.getListOfKnownCompetences());
				if (itemMapping == null) {
					//It fails!!!
					result.setPerformative( ACLMessage.FAILURE );	
					result.setOntology(ElectronicInstitution.ONTOLOGY_MAPPING_ONTOLOGY);
					result.setLanguage(getSlCodec().getName());
					result.setProtocol(FIPANames.InteractionProtocol.FIPA_REQUEST);
				} else {
					//It finds a competence
					result.setPerformative( ACLMessage.INFORM );
					result.setOntology(ElectronicInstitution.ONTOLOGY_MAPPING_ONTOLOGY);
					result.setLanguage(getSlCodec().getName());
					result.setProtocol(FIPANames.InteractionProtocol.FIPA_REQUEST);

					try {
						getContentManager().fillContent(result, itemMapping);
					} catch(OntologyException oe) {
						oe.printStackTrace();
					} catch(Codec.CodecException ce) {
						ce.printStackTrace();
					}

				}
			}
			
			return result;
		}
		
		/**
		 * Picks an attribute list and builds an hashtable of lists, where each attribute type connects to a list of arguments of the same type.
		 * @param attributes	A list of attributes of any types
		 * @return				An hashtable that is a segmentation of the attributes per type
		 */
		private Hashtable<String,List> segmentAttributeNames(List attributes) {
			Hashtable<String,List> segments = new Hashtable<String, List>();
			
			for(int i=0; i<attributes.size(); i++) {
				Attribute attribute = (Attribute) attributes.get(i);
				List segment = segments.get(attribute.getType());
				if(segment == null) {
					segment = new ArrayList();
					segments.put(attribute.getType(), segment);
				}
				segment.add(attribute);
			}
			
			return segments;
		}
		
		/**
		 * Match the CEAg-Item with all considered (in price-range) SEAg-Items.
		 * First comparse the item-name and description of the items, then a comparsion of the attributes (all three with NGrams).
		 * After that the similarity of the item-name will be calculate by WordNet.
		 * Then the a wighted result will be calculatet of description, attributes and item-name(N-Grams and semantic similarity)
		 * @return if item found: '<item-name>', or if not found: null
		 */
		private ItemMapping match(Need targetNeed, List knownCompetences) { 
			Hashtable<String,ItemMapping> htCompMap = new Hashtable<String,ItemMapping>();

			ItemMapping itemMap =  new ItemMapping();

			Hashtable<String,List> segmentedAttributesOfTargetNeed = segmentAttributeNames(targetNeed.getAttributes());

			if(knownCompetences != null && knownCompetences.size() > 0) {
				Vector<Object> results = new Vector<Object>(knownCompetences.size());

				for(int i = 0; i < knownCompetences.size(); i++) {
					Object[] singleResults = new Object[3];
					Competence knownCompetence = (Competence) knownCompetences.get(i);
					double classTypeScore = 0.0;
					singleResults[0] = knownCompetence.getType();

					classTypeScore = getSatisfiedNGramWordnet(	StopWords.takeOutStopWords(knownCompetence.getType()),
																StopWords.takeOutStopWords(targetNeed.getType()));

					// Attributes comparison ********************************************
					boolean possible_matching = true;
					double attributesScore = 0.0;

					Hashtable<String,List> segmentedAttributesOfKnownCompetence = segmentAttributeNames(knownCompetence.getAttributes());
					//Check if the number of attributes are the same for each type
					Enumeration<String> attributeTypes = segmentedAttributesOfTargetNeed.keys();
					while(attributeTypes.hasMoreElements() && possible_matching) {
						String type = attributeTypes.nextElement();

						if(segmentedAttributesOfTargetNeed.get(type).size() != segmentedAttributesOfKnownCompetence.get(type).size()) {
							possible_matching = false;
						}
					}

					if(possible_matching) {
						// create attribute mapping
						Double nGramsResult = compareAttributes(segmentedAttributesOfTargetNeed, segmentedAttributesOfKnownCompetence, itemMap);
						if(nGramsResult != null) {
							htCompMap.put(targetNeed.getType(),itemMap);
							attributesScore = nGramsResult;
						} else { //It happens when the mapping is not well succeeded
							possible_matching = false;
						}
					}

					if(possible_matching) {
						itemMap.setCompetenceType(knownCompetence.getType());
						itemMap.setNeedType(targetNeed.getType());

						singleResults[1] = classTypeScore;
						singleResults[2] = attributesScore;
						results.add(singleResults);
					}
				}

				// Calculate the result (with weighting).
				if(results.size() > 0) {
					java.util.ArrayList<Object> resultValues = new java.util.ArrayList<Object>(results.size());
					for(int i = 0; i < results.size(); i++) {
						double tmpValue = 0.0;
						double result = 0.0;

						Object[] oArray = (Object[])results.elementAt(i);

						tmpValue += ((Double)oArray[1]).doubleValue();
						tmpValue += ((Double)oArray[2]).doubleValue();

						result = tmpValue / 2;

						Object[] tmpObj = new Object[2];
						tmpObj[0] = oArray[0];

						tmpObj[1] = new Double(result);

						resultValues.add(i, tmpObj);
					}

					final Comparator comparator = new Comparator() {
						public int compare(Object o1, Object o2) {
							Object[] array1 = (Object[])o1;
							Object[] array2 = (Object[])o2;
							Double d1 = (Double)array1[1];
							Double d2 = (Double)array2[1];
							if(d1.doubleValue() > d2.doubleValue()) {
								return -1;		
							} else if(d1.doubleValue() < d2.doubleValue()) {
								return 1;		
							} else {
								return 0;
							}
						}
					};
					// Sort list to find best comparison result.
					Collections.sort(resultValues, comparator);
					String item = (String)((Object[])resultValues.get(0))[0];
					double value= ((Double)((Object[])resultValues.get(0))[1]).doubleValue();

					if(value > MINIMUM_CONFIDENCE_VALUE) {
						itemMap.setCompetenceType(item);

						if(value < WEAK_CONFIDENCE_VALUE) itemMap.setConfidence("weak-confidence");//sres += "-->weak-confidence";
						else if(value < MODERATE_CONFIDENCE_VALUE) itemMap.setConfidence("moderate-confidence");//sres += "-->moderate-confidence";
						else itemMap.setConfidence("high-confidence");//sres += "-->high-confidence";
					}
				} else {
					return null;
				}
			} else {
				return null;
			}
			
			ItemMapping cmTmp = htCompMap.get(targetNeed.getType());
			if(cmTmp != null) {
				if(cmTmp.getAttributeMappings() != null) {
					itemMap.setAttributeMappings(cmTmp.getAttributeMappings());	
				}
			}
			return itemMap;
		}


		/**
		 * Compare two words similarity. First try with N-Gram. Second try only if N-Gram gave bad result
		 * @param w1 - word1 
		 * @param w2 - word2
		 * @return The max value
		 */
		public double getSatisfiedNGramWordnet(String w1, String w2) {
			double maxValue = 0.0;
			NGrams ngrams = new NGrams(3, 2);
			maxValue = new Double(ngrams.compare(w1,w2));

			// If N-gram similarity is not enough, calculate WordNet similarity.
			if(maxValue < OntologyMapping.MINIMUM_CONFIDENCE_VALUE) {
				double item_name_wn = 0.0;
				String w1s[] = w1.split("_");
				String w2s[] = w2.split("_");
				
				if (w2s.length > 1 && (w1s.length == w2s.length)) {
					int i = 0;
					int j = 0;
					for (; i< w1s.length; i++) {
						//If the word as more than 2 letters
						if(w1s[i].length() > 2  && w2s[i].length() > 2)
							item_name_wn += new SemanticSimilarity(host,port,wordnetSimilarityFile).compareWordNet(w1s[i], w2s[i]); 
						else j++;
					}
					item_name_wn /= (i-j);
				}else { 
					item_name_wn = new SemanticSimilarity(host,port,wordnetSimilarityFile).compareWordNet(w1, w2);
				}
				if(item_name_wn != 2.0) {
					if(item_name_wn > maxValue) { 
						maxValue = item_name_wn;
					}
				}
				if(item_name_wn < OntologyMapping.MINIMUM_CONFIDENCE_VALUE) {
					//log("		Result still bad...");
				}
			}
			return maxValue;
		}
		
		/**
		 * Calculate for each attribute-type a N-Gram or Wordnet value and build an average
		 * 
		 * @return				N-Grams similarity between 0.0(no similarity) to 1.0(similar)
		 */
		private Double compareAttributes(Hashtable<String,List> segmentedAttributesOfTargetNeed, Hashtable<String,List> segmentedAttributesOfKnownCompetence, ItemMapping itemMap) {
			Hashtable<String,Object> htAttrMatch = new Hashtable<String,Object>();
			htAttrMatch.put("divisor", new Double(0.0));
			htAttrMatch.put("sum", new Double(0.0));
			//For each type of lists, we try to match the attributes. If there is any attribute which doesn't match we return null
			Enumeration<String> attributeTypes = segmentedAttributesOfTargetNeed.keys();
			while(attributeTypes.hasMoreElements()) {
				String type = attributeTypes.nextElement();
				htAttrMatch = return_matching_attributes(segmentedAttributesOfKnownCompetence.get(type), segmentedAttributesOfTargetNeed.get(type), htAttrMatch);
				if(htAttrMatch==null)
					return null;
			}
			
			double sum = (Double)htAttrMatch.remove("sum");
			double divisor = (Double)htAttrMatch.remove("divisor");

			for(Enumeration<String> en=htAttrMatch.keys();en.hasMoreElements();) {
				String key = (String) en.nextElement();
				if(key != null && key != "") {
					itemMap.addAttributeMapping(key, htAttrMatch.get(key).toString());
				}
			}
			return sum / divisor;
		}
		
		/**
		 * Work out for each attribute-type a N-Gram value, if this one is lower than the limit calculate with the wordnet
		 * 
		 * @param l1	List of attributes (all of the same type) of a known competence
		 * @param l2	List of attributes (all of the same type) of a target need
		 * @param h		Hashtable with the current matching of the different attributes ontology
		 * @return		Hashtable with the current matching of the different attributes ontology
		 */
		private Hashtable<String, Object> return_matching_attributes(List l1, List l2, Hashtable<String, Object> h) {
			// we only care if there are elements in lists 
			if(l1.size() > 0) {
				NGrams nGrams = new NGrams(3, 2);
				double sum = (Double)h.remove("sum");
				double divisor = (Double)h.remove("divisor");

				Vector<Integer> att_used = new Vector<Integer>();

				for(int i=0;i<l1.size();i++){
					String str = "";
					double d = 0;
					double d_max = 0;
					int used_j = 0;
					Attribute attributeL1 = (Attribute) l1.get(i);

					//For the price there is no need to do a test
					if(attributeL1.getName().equals("price")) {
						h.put("price", "price");
						d_max = 1.0;
					} else {
						for(int j=0;j<l2.size();j++) {
							if(att_used.indexOf(j) == -1) {
								Attribute attributeL2 = (Attribute) l2.get(j);
								//log("A testar NG " + attributeL1.getName() + attributeL2.getName());
								d = nGrams.compare(attributeL1.getName(), attributeL2.getName());
								if (d > d_max) {
									d_max = d;
									str = attributeL2.getName();
									used_j = j;
								}
							}
						}

						if(d_max > MIN_VALUE_NGRAM) {
							h.put(str, attributeL1.getName());
							att_used.add(used_j);
						} else {// if there is not enough confidence we try with wordnet
							str = "";
							d_max = 0;
							for(int j=0;j<l2.size();j++){
								d = 0;
								if(att_used.indexOf(j) == -1) {
									Attribute attributeL2 = (Attribute) l2.get(j);
									//Test with different values
									String w1s[] = attributeL1.getName().split("_");
									String w2s[] = attributeL2.getName().split("_");

									if (w2s.length > 1 && (w1s.length == w2s.length)) {
										int ii = 0;
										int jj = 0;
										double valueWN=0.0;

										for (; ii< w1s.length; ii++) {
											//If the word as more than 2 letters
											if(w1s[ii].length() > 2  && w2s[ii].length() > 2)
											{
												valueWN = new SemanticSimilarity(host,port,wordnetSimilarityFile).compareWordNet(w1s[ii], w2s[ii]);
												d += valueWN;
											}
											else jj++;
										}
										d /= (ii-jj);
									} else {
										d = new SemanticSimilarity(host,port,wordnetSimilarityFile).compareWordNet(attributeL1.getName(), attributeL2.getName());
									}

									if (d > d_max) {
										d_max = d;
										str = attributeL2.getName();
										used_j = j;
									}
								}
							}
							if(d_max > MIN_VALUE_NGRAM) {
								h.put(str, attributeL1.getName());
								att_used.add(used_j);
							} else {
								return null;
							}
						}
					}
					sum+= d_max;
					divisor++;
				}
				h.put("sum", new Double(sum)); // we put the sum and divisor in this way, for then calculating the average
				h.put("divisor", new Double(divisor));
			}

			return h;
		}
		
	} // end OntologyMappingResp class


	protected boolean createGUI() {
		return false;
	}
}

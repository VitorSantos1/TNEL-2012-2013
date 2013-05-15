package ei.agent.enterpriseagent;

import ei.contract.ContractWrapper;
import ei.EIAgent;
import ei.ElectronicInstitution;
import ei.agent.enterpriseagent.gui.EnterpriseAgentGUI;
import ei.agent.enterpriseagent.negotiation.AutoRequestModeTicker;
import ei.agent.enterpriseagent.negotiation.MyInterestManifestationResp;
import ei.agent.enterpriseagent.negotiation.MyQFNegotiationRespDispatcher;
import ei.agent.enterpriseagent.negotiation.QFNegotiationInit_Negotiate;
import ei.agent.enterpriseagent.ontology.Attribute;
import ei.agent.enterpriseagent.ontology.Competence;
import ei.agent.enterpriseagent.ontology.Need;
import ei.agent.enterpriseagent.ontology.XMLBusinessReader;
import ei.agent.enterpriseagent.report.IREReportListening;
import ei.agent.enterpriseagent.report.NewContractReportListening;
import ei.agent.enterpriseagent.report.NormEnvSubscriptionInit;
import ei.agent.enterpriseagent.signature.ContractSigningResp_Based_on_ContractNet;
import ei.agent.enterpriseagent.sync.Episode_NegotiationResp;
import ei.agent.gui.InfoGUI;
import ei.onto.synchronization.SynchronizationOntology;
import ei.onto.util.Parameters;
import ei.onto.ctr.*;
import ei.onto.negotiation.*;
import ei.onto.negotiation.qfnegotiation.*;
import ei.onto.normenv.management.*;
import ei.onto.normenv.report.*;
import ei.onto.notary.*;
import ei.onto.ontologymapping.ItemMapping;
import ei.onto.ontologymapping.FindMapping;
import ei.onto.ontologymapping.OntologyMappingOntology;
import ei.service.negotiation.qfnegotiation.QFNegotiationParameters;
import ei.util.LimitedSizeLinkedHashMap;

import jade.core.behaviours.TickerBehaviour;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.domain.FIPANames;
import jade.domain.FIPAService;
import jade.lang.acl.ACLMessage;
import jade.core.AID;
import jade.content.ContentElement;
import jade.content.lang.Codec;
import jade.content.lang.Codec.CodecException;
import jade.content.onto.OntologyException;
import jade.content.onto.UngroundedException;
import jade.content.onto.basic.Action;
import jade.util.leap.ArrayList;
import jade.util.leap.List;

import java.io.File;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Vector;

/**
 * Base class for implementing agents representing enterprises (the users of the platform).
 * 
 * An EnterpriseAgent's business is composed of two parts:
 * <ul>
 * <li> competences: what it is able to supply to the market
 * <li> needs: what it needs to go on with its business
 * </ul>
 * 
 * In this class, the agent already includes the ability to:
 * <ul>
 *  <li> request negotiations to a negotiation mediator using the QF-negotiation protocol
 *  <li> participate as a responder in an interest manifestation protocol
 *  <li> enter into negotiations of the QF-negotiation protocol (acting as a responder), possibly using a q-negotiation strategy
 *  <li> participate as a responder in a contract-signing protocol (for signing contracts it has previously negotiated)
 * </ul>
 * 
 * Each EnterpriseAgent will subscribe the Normative Environment for reports concerning his contracts. The class includes a
 * method <code>handleNEReport</code> that allows programmers to implement appropriate responses for reports coming from the Normative
 * Environment.
 */
public class EnterpriseAgent extends EIAgent {
	private static final long serialVersionUID = -5621722440992136782L;

	private HashMap<String,Competence> competenceMatchings = new HashMap<String,Competence>();   // "agentName_need" --> Competence
	private HashMap<String,ItemMapping> itemMappings = new HashMap<String,ItemMapping>();   // "agentName_needType" --> itemMappings

	private Vector<Need> needs;
	private Vector<Competence> competences;
	
	// the minimum confidence to negotiate
	protected int topNOfAgents = -1;
	
	private int numberOfActiveContracts = 0;
	public final int MAX_NUMBER_OF_ACTIVE_CONTRACTS = 5+(int)(Math.random()*10);

	// negotiated contracts that are waiting to be signed when the Notary asks for  TODO
	//private ArrayList<ContractWrapper> toBeSignedContracts = new ArrayList<ContractWrapper>();   // not being used for now, because the negotiation mediator is not sending the contract (except for the starter...)

	private Hashtable<String,NormEnvSubscriptionInit> neSubscriptions = new Hashtable<String,NormEnvSubscriptionInit>();//the subscriptions to the normative environment. contract id -> ne subscription

	private Vector<String> contractIds = new Vector<String>();   // the established contract ids
	protected LimitedSizeLinkedHashMap<String,ContractWrapper> contracts = new LimitedSizeLinkedHashMap<String,ContractWrapper>();
	// reports received from the normative environment regarding each contract
	protected LimitedSizeLinkedHashMap<String,Vector<Report>> contractReports = new LimitedSizeLinkedHashMap<String,Vector<Report>>(); // contract id -> reports
	// messages exchanged in each negotiation: negotiationId --> ACLMessages
	private LimitedSizeLinkedHashMap<String,Vector<ACLMessage>> negotiationMessages = new LimitedSizeLinkedHashMap<String,Vector<ACLMessage>>();

	private String xsd_file;

	private TickerBehaviour autoRequestModeTicker;
	
	// use Q-negotiation strategy?
	public boolean isUseQNegotiationStrategy() {
		try {
			return Boolean.parseBoolean(getConfigurationArguments().getProperty("useQNegotiationStrategy"));
		} catch(Exception e) {
			return false;
		}
	}

	// use ontology mapping service?
	public boolean isUseOntoMapService() {
		try {
			return Boolean.parseBoolean(getConfigurationArguments().getProperty("useOntoMapService"));
		} catch(Exception e) {
			return false;
		}
	}

	public String getXsd_file() {
		return xsd_file;
	}

	protected void setup(){
		super.setup();
		
		// register additional ontologies
		getContentManager().registerOntology( NegotiationOntology.getInstance()   );
		getContentManager().registerOntology( QFNegotiationOntology.getInstance()   );
		getContentManager().registerOntology( OntologyMappingOntology.getInstance() );
		getContentManager().registerOntology( ContractSigningOntology.getInstance() );
		getContentManager().registerOntology( NEManagementOntology.getInstance()    );
		getContentManager().registerOntology( NEReportOntology.getInstance()        );
		getContentManager().registerOntology( CTROntology.getInstance()      );

		// defines the maximum number of entry in the MyLinkedHashMap structure
		if(getConfigurationArguments().containsKey("maxEntries_negotiationMessages")) {
			negotiationMessages.setMaxEntries(Integer.parseInt(getConfigurationArguments().getProperty("maxEntries_negotiationMessages")));
		}		
		if(getConfigurationArguments().containsKey("maxEntries_contracts")) {
			contracts.setMaxEntries(Integer.parseInt(getConfigurationArguments().getProperty("maxEntries_contracts")));
			contractReports.setMaxEntries(Integer.parseInt(getConfigurationArguments().getProperty("maxEntries_contracts")));
		}
		
		if(getConfigurationArguments().containsKey("stock")) {
			contracts.setMaxEntries(Integer.parseInt(getConfigurationArguments().getProperty("maxEntries_contracts")));
		}
		
		// in synchronized experiment?
		if(isInSynchronizedExperiment()) {
			getContentManager().registerOntology( SynchronizationOntology.getInstance()   );
			addBehaviour(new Episode_NegotiationResp(this));
		}

		// get the xsd_file argument
		xsd_file = getConfigurationArguments().getProperty("contract_xsd");

		loadBusiness();

		// register the agent in the DF
		updateRegistrationWithDF();

		addBehaviour(new MyInterestManifestationResp(this));
		// q-negotiation responder
		addBehaviour(new MyQFNegotiationRespDispatcher(this));

		// contract signing responder
		if(!isInSynchronizedExperiment()) {
			addBehaviour(new ContractSigningResp_Based_on_ContractNet(this));
		}

		// new contract report listener
		addBehaviour(new NewContractReportListening());
		// IRE report listener
		if(isInSynchronizedExperiment()) {
			addBehaviour(new IREReportListening());
		}
		
		// set auto-mode
		if(getConfigurationArguments().containsKey("auto_mode")) {
			setAutoRequestMode(Boolean.parseBoolean(getConfigurationArguments().getProperty(QFNegotiationParameters.AUTO_MODE)));
		} else {
			setAutoRequestMode(QFNegotiationParameters.AUTO_MODE_DEFAULT);
		}
	}

	/**
	 * When terminating, unsubscribes from the Normative Environment for every standing contract.
	 */
	protected void takeDown() {
		super.takeDown();
		Enumeration<NormEnvSubscriptionInit> neSubs = neSubscriptions.elements();
		while(neSubs.hasMoreElements()) {
			neSubs.nextElement().cancelSubscription();
		}
	}

	/**
	 * read the need and competences from the xml file
	 */
	private void loadBusiness() {
		competences = new Vector<Competence>();
		needs = new Vector<Need>();
		
		if(getConfigurationArguments().containsKey("business")) {
			try {
				XMLBusinessReader.loadBusiness(new File(getConfigurationArguments().getProperty("business")), competences, needs);
			} catch(Exception e) {
				e.printStackTrace();
			}
		}
	}

	public Vector<Need> getNeeds() {
		return needs;
	}

	public void setNeeds(Vector<Need> needs) {
		this.needs = needs;
	}

	/**
	 * Gets the competences of this agent.
	 */
	public Vector<Competence> getCompetences() {
		return competences;
	}

	public Competence findCompetence(Need need, AID requester) {
		String itemMatchingKey = requester.getLocalName() + "_" + need.getType();
		
		// check if a matching competence was already searched for
		if(getCompetenceMatchings().containsKey(itemMatchingKey)) {
			return getCompetenceMatchings().get(itemMatchingKey);
		}
		
		Competence competence = findMatchingCompetence(need);
		if(competence != null) {
			getCompetenceMatchings().put(itemMatchingKey, competence);
			return competence;
		}

		if(isUseOntoMapService()) {
			ItemMapping itemMapping = findItemMapping(need, requester);
			// check if the ItemMapping was successful
			if(itemMapping != null) {
				// get my competence using the mapping type
				int i=0;
				while(i < getCompetences().size() && !getCompetences().get(i).getType().equals(itemMapping.getCompetenceType())) {
					i++;
				}
				if(i < getCompetences().size()) {   // should always be true
					competence = getCompetences().get(i);
					return competence;
				}
			}
		}
		return null;
	}
	
	private ItemMapping findItemMapping(Need need, AID requester) {
		// check if an ItemMapping was already attempted
		ItemMapping itemMapping = getItemMapping(need, requester);
		if(itemMapping == null) {
			// use ontology mapping service
			itemMapping = askOntoMapService(need, requester);
			addItemMapping(need, requester, itemMapping);
			if(getGUI() != null) {
				((EnterpriseAgentGUI) getGUI()).refreshOntologyMappings();
			}
		}
		return itemMapping;
	}
	
	/**
	 * Add a competence to the competences vector of this agent.
	 * 
	 * @param competence	The competence to add
	 * @param register	Whether the competence should be announced right away in the DF
	 */
	protected void addCompetence(Competence competence, boolean register) {
		competences.add(competence);
		if(register) {
			updateRegistrationWithDF();
		}
	}

	/**
	 * Clears the competences of this agent. Unregisters the services from the DF.
	 */
	protected void clearCompetences() {
		competences.clear();
		updateRegistrationWithDF();
	}

	/**
	 * Updates this agent registration with the DF.
	 * If the agent has some need, registers as buyer.
	 * If the agent has some competences, registers as seller of those competences.
	 */
	protected void updateRegistrationWithDF() {

		Vector<ServiceDescription> sds = new Vector<ServiceDescription>();
		ServiceDescription sd;

		// generic enterprise agent service
		sd = new ServiceDescription();
		sd.setName(""+getLocalName());
		sd.setType(ElectronicInstitution.ROLE_USER_AGENT);
		sds.add(sd);

		// buyer
		if(needs.size() > 0) {
			sd = new ServiceDescription();
			sd.setName(""+getLocalName()+"_"+ElectronicInstitution.ROLE_BUYER_AGENT);
			sd.setType(ElectronicInstitution.ROLE_BUYER_AGENT);
			sds.add(sd);
		}

		// seller
		//  TODO: MISSING fields (isicClass) in xml file
		//	These should be included as Properties of the ServiceDescription
		if(competences.size() > 0) {
			sd = new ServiceDescription();
			sd.setName(""+getLocalName()+"_"+ElectronicInstitution.ROLE_SELLER_AGENT);
			sd.setType(ElectronicInstitution.ROLE_SELLER_AGENT);
			sds.add(sd);
		}
		
		registerServicesAtDF(sds);
	}

	/**
	 * Gets the messages exchanged in each negotiation
	 * @return the Hashtable object messages exchanged long negotiation
	 */
	public LimitedSizeLinkedHashMap<String,Vector<ACLMessage>> getNegotiationMessages() {
		return negotiationMessages;
	}

	public HashMap<String, Competence> getCompetenceMatchings() {
		return competenceMatchings;
	}

	public HashMap<String, ItemMapping> getItemMappings() {
		return itemMappings;
	}
	
	private String generateItemMappingKey(Need need, AID requester) {
		return requester.getLocalName() + "_" + need.getType();
	}
	
	public ItemMapping getItemMapping(Need need, AID requester) {
		return getItemMappings().get(generateItemMappingKey(need, requester));
	}
	
	public void addItemMapping(Need need, AID requester, ItemMapping itemMapping) {
		getItemMappings().put(generateItemMappingKey(need, requester), itemMapping);
	}

	public void setItemMappings(HashMap<String, ItemMapping> itemMappings) {
		this.itemMappings = itemMappings;
	}

	public List createListOfNeedsToBeNegotiated() {
		// create list of needs to be negotiated
		List needsToBeNegotiated = new ArrayList();
		for(Need need : needs) {
			if(need.isNegotiate()) {
				needsToBeNegotiated.add(need);
			}
		}
		return needsToBeNegotiated;
	}
	
	public void requestNegotiate() {
		AID negotiationMediator = fetchAgent(ElectronicInstitution.SRV_NEGOTIATION_FACILITATOR, false);
		if(negotiationMediator != null) {
			// create list of needs to be negotiated
			List needsToBeNegotiated = createListOfNeedsToBeNegotiated();
			if(needsToBeNegotiated.size() > 0) {
				addBehaviour(new QFNegotiationInit_Negotiate(this, negotiationMediator, needsToBeNegotiated, fillInNegotiationParameters(new Parameters())));
			} else {
				if(getGUI() != null) {
					InfoGUI infoGUI = new InfoGUI("Nothing to negotiate!", getLocalName(), getGUI());
					infoGUI.setVisible(true);
					((EnterpriseAgentGUI) getGUI()).setAutoModeCheckBox(false);
				} else {
					log("Nothing to negotiate!");
				}
				// stop auto mode, if set
				setAutoRequestMode(false);
			}
		} else {   // no mediators found
			if(getGUI() != null) {
				InfoGUI infoGUI = new InfoGUI("PROBLEM: No Negotiation Facilitators Available!", getLocalName(), getGUI());
				infoGUI.setVisible(true);
				((EnterpriseAgentGUI) getGUI()).setAutoModeCheckBox(false);
			} else {
				log("PROBLEM: No Negotiation Facilitators Available!");
			}
			// stop auto mode, if set
			setAutoRequestMode(false);
		}
	}

	public Need translateToOwnItemOntology(Need need, AID requester, ItemMapping itemMapping) {
		// translate attribute names
		for(int k=0;k<need.getAttributes().size();k++){
			Attribute attribute = (Attribute) need.getAttributes().get(k);
			attribute.setName(itemMapping.getAttributeMappingFromName(attribute.getName()).getMappingAttributeName());
		}
		// translate competence name
		need.setType(itemMapping.getCompetenceType());

		return need;
	}
	
	/**
	 * Translates an item and its attributes included in a proposal evaluation to an own item, using a <code>ItemMapping</code>.
	 * 
	 * @param proposalEvaluation	The proposal evaluation including an item class and attributes to be translated
	 * @param itemMapping		    The mapping of item to use in the translation
	 * @return						The proposal evaluation already translated
	 */
	public ProposalEvaluation translateToOwnItemOntology(ProposalEvaluation proposalEvaluation, ItemMapping itemMapping) {
		// translate attribute names
		for(int k=0;k<proposalEvaluation.getAttributeValueEvaluations().size();k++){
			AttributeValue value = (AttributeValue) proposalEvaluation.getAttributeValueEvaluations().get(k);
			value.setName(itemMapping.getAttributeMappingFromName(value.getName()).getMappingAttributeName());
		}
		// translate need name
		proposalEvaluation.setNeedType(itemMapping.getCompetenceType());

		return proposalEvaluation;
	}

	/**
	 * Translates an item and its attributes included in a proposal to a foreign item using a <code>ItemMapping</code>.
	 * 
	 * @param proposal			The proposal including an item  and attributes to be translated
	 * @param itemMapping       The mapping of item to use in the translation
	 * @return					The proposal already translated
	 */
	public Proposal translateToForeignItemOntology(Proposal proposal, ItemMapping itemMapping) {
		// translate attribute names
		for(int k=0;k<proposal.getAttributeValues().size();k++){ 
			if(itemMapping.getAttributeMappingFromMappingName(((AttributeValue) proposal.getAttributeValues().get(k)).getName()) != null) {
				((AttributeValue) proposal.getAttributeValues().get(k)).setName(
						itemMapping.getAttributeMappingFromMappingName(((AttributeValue) proposal.getAttributeValues().get(k)).getName()).getAtributeName());
			}
		}
		// translate need name
		proposal.setNeedType(itemMapping.getNeedType());

		return proposal;
	}

	/**
	 * This method is invoked when a report is received from the Normative Environment.
	 * Each EnterpriseAgent subscribes the Normative Environment for reports regarding each of its contracts.
	 * Programmers that which to do something when such a report is received may implement this method.
	 * 
	 * @param rep	The report received from the Normative Environment
	 */
	public void handleNEReport(Report rep) {
	}

	public Vector<String> getContractIds() {
		return contractIds;
	}

	/**
	 * Gets a contract's reports.
	 * @param contractId the contract id
	 * @return the contract reports
	 */
	public Vector<Report> getContractReports(String contractId) {
		return contractReports.get(contractId);
	}

	public LimitedSizeLinkedHashMap<String, Vector<Report>> getContractReports() {
		return contractReports;
	}

	/**
	 * Gets a contract.
	 * 
	 * @param contractId the contract id
	 * @return the contract
	 */
	public ContractWrapper getContract(String contractId) {
		return contracts.get(contractId);
	}
	
	public void addContract(String contractId, ContractWrapper cw) {
		contracts.put(contractId, cw);
	}

	public int getNumberOfActiveContracts() {
		return numberOfActiveContracts;
	}

	public void incNumberOfActiveContracts() {
		this.numberOfActiveContracts++;
	}
	
	public void decNumberOfActiveContracts() {
		this.numberOfActiveContracts--;
	}

	public Hashtable<String, NormEnvSubscriptionInit> getNeSubscriptions() {
		return neSubscriptions;
	}

	/**
	 * Add any negotiation parameters that are defined internally in this agent (they are not editable or were not sent by the synchronizer agent).
	 */
	public Parameters fillInNegotiationParameters(Parameters negotiationParameters) {
		String[] negotiationParameterNames = {
				QFNegotiationParameters.CONTRACT_TYPE, QFNegotiationParameters.TOP_N, QFNegotiationParameters.USE_CONTEXTUAL_FITNESS,
				QFNegotiationParameters.USE_TRUST_IN_PRESELECTION, QFNegotiationParameters.USE_TRUST_IN_PROPOSAL_EVALUATION, QFNegotiationParameters.USE_TRUST_IN_CONTRACT_DRAFTING,
				QFNegotiationParameters.RISK_TOLERANCE, QFNegotiationParameters.MAPPING_METHOD
				};
		
		// add agent-specific parameters that are not already in the negotiation parameters
		for(int i=0; i<negotiationParameterNames.length; i++) {
			if(getConfigurationArguments().containsKey(negotiationParameterNames[i]) && negotiationParameters.get(negotiationParameterNames[i]) == null) {
				negotiationParameters.add(negotiationParameterNames[i], (String) getConfigurationArguments().getProperty(negotiationParameterNames[i]));
			}
		}
		
		return negotiationParameters;
	}

	public void setAutoRequestMode(boolean autoMode) {
		if(autoMode) {
			if(autoRequestModeTicker == null) {
				autoRequestModeTicker = new AutoRequestModeTicker(this);
				addBehaviour(autoRequestModeTicker);
			}
		} else {
			if(autoRequestModeTicker != null) {
				autoRequestModeTicker.stop();
				autoRequestModeTicker = null;
			}
		}
	}

	/**
	 * Decides if the supplier agent enters the negotiation based on the received proposal.
	 * 
	 * @param compCall
	 * @param competence
	 * @return
	 */
	public boolean decideOnEnteringNegotiation(CompetenceCall compCall, Competence competence) {
		//If fine > riskTolerance --> refuse negotiation
//		if(compCall.getFine() > riskTolerance) {
//			return false;
//		}
		return true;
	}

	/**
	 * Checks if any competence of this agent matches a given need.
	 * In this implementation, a competence is searched that has the same class, and whose attributes match (by name and type) the attributes of the
	 * to-be-matched need. Furthermore, the domains for each attribute should at least overlap (for continuous attributes) or have at least one value
	 * in common (for discrete attributes).
	 * 
	 * @param need	    The need to find a matching competence of this agent
	 * @return			The competence that matches the one passed to this method
	 */
	private Competence findMatchingCompetence(Need need) {

		for(int i=0; i<competences.size(); i++) {   // iterate through my competences
			Competence competence = competences.get(i);

			boolean competenceOK = true;   // assume this is the one!
			// check that the class is the same
			if(competence.getType().equals(need.getType()) ) {

				// check attribute domains: I should be able to propose acceptable values for each attribute
				for(int j=0; j<need.getNumberOfAttributes() && competenceOK; j++) {
					Attribute needAttribute = (Attribute) need.getAttributes().get(j);

					// get attribute with the same name
					Attribute competenceAttribute = competence.getAttribute(needAttribute.getName());
					if(competenceAttribute == null ||
							!needAttribute.getType().equals(competenceAttribute.getType())) {   // check attribute type
						competenceOK = false;
					} else {
						// check domains for this attribute
						if(needAttribute.isDiscrete()) { //at least one value should be in common
							List needAttributeDomain = needAttribute.getDiscreteDomain();
							List competenceAttributeDomain = competenceAttribute.getDiscreteDomain();
							boolean foundDomainValue = false;
							for(int d=0; d<needAttributeDomain.size() && !foundDomainValue; d++) {
								if(competenceAttributeDomain.contains(needAttributeDomain.get(d)))
									foundDomainValue = true;   // domain value in common
							}
							if(!foundDomainValue) {
								competenceOK = false;   // no domain value in common
							}
						} else {   // continuous attribute: intervals must overlap
							float needAttributeDomainMin = Float.parseFloat((String) needAttribute.getContinuousDomainMin());
							float needAttributeDomainMax = Float.parseFloat((String) needAttribute.getContinuousDomainMax());
							float competenceAttributeDomainMin = Float.parseFloat((String) competenceAttribute.getContinuousDomainMin());
							float competenceAttributeDomainMax = Float.parseFloat((String) competenceAttribute.getContinuousDomainMax());

							if( (needAttributeDomainMin < competenceAttributeDomainMin ||
									competenceAttributeDomainMax < needAttributeDomainMin)
									&&
									(competenceAttributeDomainMin < needAttributeDomainMin ||
											needAttributeDomainMax < competenceAttributeDomainMin)) {
								competenceOK = false;   // no domain overlap
							}
						}
					}
				}
				if(competenceOK) {
					return competence;
				}
			}
		}
		// failure: no competence matching the requested one
		return null;
	}

	private ACLMessage buildOntologyMappingRequest(Need need, AID requesterAgent, List listOfCompetences) {
		AID ontology_mapping = fetchAgent(ElectronicInstitution.SRV_ONTOLOGY_MAPPING, false);
		if(ontology_mapping != null) {
			ACLMessage request = new ACLMessage(ACLMessage.REQUEST);
			request.setProtocol(FIPANames.InteractionProtocol.FIPA_REQUEST);
			request.setLanguage(getSlCodec().getName());
			request.setOntology(ElectronicInstitution.ONTOLOGY_MAPPING_ONTOLOGY);
			request.addReceiver(ontology_mapping);

			FindMapping findMapping = new FindMapping(need, requesterAgent, listOfCompetences);
			Action act = new Action(ontology_mapping, findMapping);
			try{
				getContentManager().fillContent(request, act);
			} catch(OntologyException oe) {
				oe.printStackTrace();
				return null;
			} catch(Codec.CodecException cex) { 
				cex.printStackTrace();
				return null;
			}

			return request;
		} else {
			System.err.println("No Ontology Mapping Agent present!");
			return null;
		}
	}
	
	public ItemMapping askOntoMapService(Need need, AID requesterAgent) {
		
		Attribute attribute = need.getAttribute("price");
		float priceMin = Float.parseFloat((String) attribute.getContinuousDomainMin());
		float priceMax = Float.parseFloat((String)attribute.getContinuousDomainMax());
		List listOfCompetences = getCompetencesWithOverlapingPriceDomain(priceMin, priceMax);
		ACLMessage msgInform = null;
		try {
			msgInform = FIPAService.doFipaRequestClient(this, buildOntologyMappingRequest(need, requesterAgent, listOfCompetences), 10000);
		} catch (FIPAException e) {
			log(e.toString());
		}

		if(msgInform != null) {   // i.e., I got an INFORM message
			ContentElement contentElement = null;
			try {
				contentElement = getContentManager().extractContent(msgInform);
				if(contentElement instanceof ItemMapping) {
					return (ItemMapping) contentElement;
				}
			} catch(Codec.CodecException cex) {
				cex.printStackTrace();
			} catch(OntologyException oe) {
				oe.printStackTrace();
			}
		}
			
		return null;
	}


	/**
	 * Answers request from the Ontology Service Agent. Receives a request for a price and finds every competence in which the attribute
	 * price is less than 25% or less higher or lower. Fills a vector with the details for that competences and sends them to the back to the Ontology
	 * Service Agent. The vector is made of several other vectors, each one with the competence name, a comment and another vector for the details.
	 * This other vector has another five vectors inside. One for attributes of type string, one for integers, one for floats, one for booleans,
	 * and one for relations.
	 */
	private List getCompetencesWithOverlapingPriceDomain(float price_clientMin, float price_clientMax ) {
		List listOfCompetences = new jade.util.leap.ArrayList();
		for(int i=0; i < getCompetences().size(); i++) {
			Attribute attribute = (Attribute) getCompetences().get(i).getAttribute("price");
			float price_supplierMin = Float.parseFloat((String) attribute.getContinuousDomainMin());
			float price_supplierMax = Float.parseFloat((String) attribute.getContinuousDomainMax());

			if (!((price_clientMin > price_supplierMax) || (price_clientMax < price_supplierMin))) {
				listOfCompetences.add(getCompetences().get(i));
			}
		}
		return listOfCompetences;
	}

	/**
	 * Adds a message to the Negotiation Messages Hashtable.
	 * @param msg the message to add
	 */
	public void addNegotiationMessage(String negotiationId, Vector<ACLMessage> msgs) {
		getNegotiationMessages().put(negotiationId, msgs); //conversationID (String) -> MessagesExchanged (Vector)
		
		if(getGUI() != null) {					
			ACLMessage msg = msgs.elementAt(0);
			ContentElement ce;
			try {
				ce = getContentManager().extractContent(msg);
				if(ce instanceof CompetenceCall) {
					CompetenceCall cc = (CompetenceCall) ce;
					((EnterpriseAgentGUI) getGUI()).addNegotiationMessageRow(negotiationId, cc.getNeed().getType(), cc.getContractType());
				}
			} catch (UngroundedException e) {
				e.printStackTrace();
			} catch (CodecException e) {
				e.printStackTrace();
			} catch (OntologyException e) {
				e.printStackTrace();
			}
		}
	}

	protected boolean createGUI() {
		gui = new EnterpriseAgentGUI(this);
		gui.setLocationRelativeTo(null);

		return true;
	}
	
}

package tnel.sinalpha;

import ei.EIAgent;
import ei.ElectronicInstitution;
import ei.onto.ctr.*;
import ei.onto.normenv.report.*;
import ei.proto.normenv.report.NEReportInit_SendReports;
import ei.service.PlatformService;
import ei.service.ctr.CTR;
import ei.service.ctr.CTREvidencesRecorder;
import ei.service.ctr.CTRGui;
import ei.service.ctr.ContractEnactment;
import ei.service.ctr.EAgentInfo;
import ei.service.ctr.EvidenceInfo;
import ei.service.ctr.OutcomeGenerator.MappingMethod;
import ei.service.ctr.context.AbstractContextGenerator;
import ei.service.ctr.context.Context;
import ei.sync.EnactmentOutcome;

import jade.content.AgentAction;
import jade.content.ContentElement;
import jade.content.lang.Codec;
import jade.content.onto.OntologyException;
import jade.content.onto.basic.Action;
import jade.core.AID;
import jade.core.Agent;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPANames;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.proto.AchieveREResponder;
import jade.proto.SimpleAchieveREResponder;
import jade.proto.SubscriptionInitiator;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Set;

import jade.util.leap.HashSet;
import jade.util.leap.List;
import java.util.Vector;

/**
 * This class provides the Computational Trust and Reputation service. The CTR
 * service receives information from Normative Environment service regarding the
 * activity of external agents and provide a list of external agents that
 * fulfill the provided requirements, and information about agents.
 *
 * The computational trust and reputation service implements the following
 * interaction protocol roles: <ul> <li>the CTR responder role <li>the NormEnv
 * subscription initiator role <li>the DF subscription initiator role </ul>
 *
 * @author S�rgio
 */
public class SinalphaCTR extends PlatformService {

    private static final long serialVersionUID = -5946291688838853892L;
    /**
     * The struct that record the CTR data
     */
    private Hashtable<String, EAgentInfo> evidenceRecords;
    /**
     * The struct that saves received messages
     */
    private Vector<ACLMessage> receivedMessages;
    /**
     * The struct that saves sent messages
     */
    private Vector<ACLMessage> sentMessages;
    /**
     * The Protocol to receive new information about external agents
     */
    private NormEnvSubscriptionInit subscNormEnv;
    /**
     * The Protocol to receive information about new external agents
     */
    private DFSubscriptionInit subscDF;
    /**
     * The Protocol to reply to information request by Negotiation Agent
     */
    private CTRResp negotiationResp;
    private MappingMethod mapMet = MappingMethod.AllDifferent;
    private String filename = null;

    //The struct that saves the names of negotiated products
//	private Vector<String> products;
//	/**
//	 * Adds a product to products names' list
//	 * @param product
//	 */
//	public void addProduct(String product) {
//		for(int i = 0; i < products.size(); i++)
//			if(products.get(i).equalsIgnoreCase(product))
//				return;
//		this.products.add(product);
//	}
//
//	/**
//	 * @return the products
//	 */
//	private Vector<String> getProducts() {
//		return products;
//	}
    /**
     * Set up of the CTR Agent.
     */
    protected void setup() {
        super.setup();

        //Initialize vectors
        receivedMessages = new Vector<ACLMessage>();
        sentMessages = new Vector<ACLMessage>();
//		products = new Vector<String>();
        // create the evidence records table
        evidenceRecords = new Hashtable<String, EAgentInfo>();

        // register ontologies
        getContentManager().registerOntology(CTROntology.getInstance());
        getContentManager().registerOntology(NEReportOntology.getInstance());

        // responder role for CTR_ONTOLOGY
        negotiationResp = new CTRResp(this);
        addBehaviour(negotiationResp);

        // subscribe the Normative Environment for reports on contracts and IREs
        subscNormEnv = new NormEnvSubscriptionInit(this);
        addBehaviour(subscNormEnv);

        // subscribe the DF Service for reports about new suppliers
        DFAgentDescription dfd = new DFAgentDescription();
        ServiceDescription sd = new ServiceDescription();
        sd.setType(ElectronicInstitution.ROLE_SELLER_AGENT); // TODO: for now only sellers are being tracked (in terms of trust)
        dfd.addServices(sd);
        subscDF = new DFSubscriptionInit(this, dfd);
        addBehaviour(subscDF);

        if (getConfigurationArguments().containsKey("ctr_evidences_filename")) {
            filename = (String) getConfigurationArguments().get("ctr_evidences_filename");
            loadFile(filename);
        } else {
            filename = System.currentTimeMillis() + ".xml";
        }

        // synchronized experiments
        if (isInSynchronizedExperiment()) {
//			addBehaviour(new SyncResetResp(this, getSynchronizer()));
        }

    }

    /**
     * When terminating, unsubscribes from the Normative Environment.
     */
    protected void takeDown() {
        // the takeDown method at the AgentifiedService class handles deregistration from the DF
        super.takeDown();
        // unsubscribe from the Normative Environment
        subscNormEnv.cancelSubscription();
    }

    /**
     * Creates the CTR GUI
     */
    protected boolean createGUI() {
//                CTR newCTR = (CTR) this;

        gui = new SinalphaCTRGui(this);
        return true;
    }

    /**
     * Gets the vector with all received messages
     */
    public Vector<ACLMessage> getReceivedMessages() {
        return receivedMessages;
    }

    /**
     * Returns the HashTable of the agents' information.
     *
     * @return the hashtable of the agents' information
     */
    protected Hashtable<String, EAgentInfo> getCTRRecords() {
        return this.evidenceRecords;
    }

    /**
     * @return the ctrReqList
     */
    public Vector<ACLMessage> getSentMessages() {
        return sentMessages;
    }

    /**
     * Inner class to implement the DF-subscription initiator role. The CTR
     * subscribes the DF for updates on existing agents and their services
     * (roles).
     *
     * @author sergio
     */
    private class DFSubscriptionInit extends SubscriptionInitiator {

        private static final long serialVersionUID = 6944274811217174827L;

        /**
         * Creates a
         * <code>SubscribeDFInit</code> instance
         *
         * @param agent The agent that adds the behaviour.
         */
        DFSubscriptionInit(Agent agent, DFAgentDescription DFAD) {
            super(agent, DFService.createSubscriptionMessage(agent, getDefaultDF(), DFAD, null));
        }

        /**
         * Handles an inform message from the DF.
         */
        protected void handleInform(ACLMessage inform) {
            // decode the message
            try {
                DFAgentDescription[] dfds = DFService.decodeNotification(inform.getContent());
                for (int i = 0; i < dfds.length; i++) {
                    // Create an evidence record
                    String agentName = dfds[i].getName().getLocalName();
                    EAgentInfo rr = evidenceRecords.get(agentName);
                    if (rr == null) {
                        rr = new EAgentInfo(agentName);
                        evidenceRecords.put(agentName, rr);
                    }

                }
            } catch (FIPAException fe) {
                fe.printStackTrace();
            }
        }
    } // end DFSubscriptionInit class

    /**
     * Inner class to implement the ne-report subscription initiator role.
     *
     * @author hlc
     */
    private class NormEnvSubscriptionInit extends NEReportInit_SendReports {

        /**
         *
         */
        private static final long serialVersionUID = -4708091077567642407L;

        /**
         * Creates a
         * <code>NormEnvSubscriptionInit</code> instance
         *
         * @param agent	The agent that adds the behaviour.
         */
        NormEnvSubscriptionInit(Agent agent) {
            super(agent);
        }

        /**
         * Handles an inform message from the Normative Environment.
         */
        protected void handleInform(ACLMessage inform) {
            // check reported predicate
            Report report = null;
            try {
                // extract content
                ContentElement ce = myAgent.getContentManager().extractContent(inform);
                if (ce instanceof Report) {
                    report = (Report) ce;
                }

            } catch (Codec.CodecException cex) {
                cex.printStackTrace();
            } catch (OntologyException oe) {
                oe.printStackTrace();
            }

            // only interested in new contracts, fulfillments and violations
            if (report instanceof NewContract) {
                // get the EAgentInfo for each envisaged agent
                NewContract nC = (NewContract) report;
                List agentList = nC.getAgents();
                String s = nC.getContext();

                Vector<String> agents = new Vector<String>();
                for (int j = 0; j < agentList.size(); j++) {
                    agents.add((String) agentList.get(j));
                }
                //If it's a social contract, it cannot count for number of contracts
                if (s.startsWith("social-contract")) {
                    return;
                }
                for (int i = 0; i < agentList.size(); i++) {
                    String ag = (String) agentList.get(i);
                    EAgentInfo rr = evidenceRecords.get(ag);
                    // increment the number of contracts of the specified agent
                    ContractEnactment c = new ContractEnactment();
                    c.setContractID(s);
                    c.setOwner(ag);

                    c.setAgents(agents);

                    Vector<Context> contexts = new Vector<Context>();
                    contexts.add(generateContext(nC));
                    c.setContexts(contexts);
//					for(Context cont : contexts)
//					{
//						addProduct(cont.getName());
//					}

                    if (rr == null && ag.equalsIgnoreCase(c.getContexts().get(0).getSeller())) {

                        rr = new EAgentInfo(ag);
                        evidenceRecords.put(ag, rr);
                    }
                    if (ag.equalsIgnoreCase(c.getContexts().get(0).getSeller())) {
                        rr.addContract(c);
                    }
                }
                receivedMessages.add(inform);
                /**
                 * Entrada do Sinalpha aqui!!!!!!!!!                 
                 */
            } else if (report instanceof Fulfillment) {
                // get the EAgentInfo for the envisaged agent
                Fulfillment f = (Fulfillment) report;
                String ag = f.getObligation().getBearer();
                //System.out.println("recebeu fulfill: "+ag);
                EAgentInfo eAI = evidenceRecords.get(ag);
                if (eAI == null) // TODO: only sellers are being tracked, therefore an evidence record might not exist for this bearer
                {
                    return;
                }
                // increment the number of fulfilled obligations of the specified agent
                eAI.addFulfObl(f.getObligation(), f.getContext(), f.getWhen() - f.getObligation().getDeadline());
                receivedMessages.add(inform);
                /**
                 * Entrada do Sinalpha aqui!!!!!!!!!                 
                 */
            } else if (report instanceof Violation) {
                Violation v = (Violation) report;
                // get the EAgentInfo for the envisaged agent
                String ag = v.getObligation().getBearer();
                //System.out.println("recebeu viol: "+ag);
                EAgentInfo eAI = evidenceRecords.get(ag);
                if (eAI == null) // TODO: only sellers are being tracked, therefore an evidence record might not exist for this bearer
                {
                    return;
                }
                // increment the number of violated obligations of the specified agent
                eAI.addViolObl(v.getObligation(), v.getContext());
                receivedMessages.add(inform);
                /**
                 * Entrada do Sinalpha aqui!!!!!!!!!                 
                 */
            } else if (report instanceof DeadlineViolation) {
                DeadlineViolation dLV = (DeadlineViolation) report;
                String ag = dLV.getObligation().getBearer();
                //System.out.println("recebeu dLVIol: "+ag);
                EAgentInfo eAI = evidenceRecords.get(ag);
                if (eAI == null) // TODO: only sellers are being tracked, therefore an evidence record might not exist for this bearer
                {
                    return;
                }
                // increment the number of violated deadlines obligations of the specified agent
                eAI.addDlineViolObl(dLV.getObligation(), dLV.getContext());
                receivedMessages.add(inform);
            } else if (report instanceof LivelineViolation) {
                //FIXME decidir o que fazer com uma liveline violation. Para ja ignora-se
                receivedMessages.add(inform);
            } else if (report instanceof ContractEnd) {
                ContractEnd cE = (ContractEnd) report;

                EnactmentOutcome enactmentOutcome = new EnactmentOutcome(cE.getContext());

                String context = cE.getContext();
                Set<String> set = evidenceRecords.keySet();

                String str = null;
                Iterator<String> itr = set.iterator();
                while (itr.hasNext()) {
                    str = itr.next();
                    EAgentInfo eAgentInfo = evidenceRecords.get(str);
                    Vector<ContractEnactment> eAgInfo = eAgentInfo.getContractHistoric();
                    for (int i = 0; i < eAgInfo.size(); i++) {
                        if (eAgInfo.get(i).getName().equalsIgnoreCase(context)) {
                            if (eAgInfo.get(i).getContexts().get(0).getSeller().equalsIgnoreCase(str)) {
                                //FIXME
                                enactmentOutcome.setOutcome(eAgInfo.get(i).getState());
                            }
                            enactmentOutcome.setAgents(eAgInfo.get(i).getAgents());
                            eAgInfo.get(i).setEnded(true);
                            Vector<ObligationEvidence> obls = eAgInfo.get(i).getObligations();
                            for (ObligationEvidence oblig : obls) {
                                // System.out.println(":-:_:-:"+oblig.getFact());
                                //Martelada!!!
                                if (oblig.getFact().contains("delivery")) {
                                    // enactmentOutcome.setPenaltyFd(vec.get(i).getFulfillmentTime(oblig));
                                    // System.out.println("--------------------xegou!!!!  "+vec.get(i).getFulfillmentTime(oblig));
                                }

                            }
                            eAgentInfo.refresh(null);
                            EvidenceInfo eI = new EvidenceInfo();
                            eI.setContracID(context);
                            eI.setTrustValue(eAgentInfo.getCTREval(false, null, ""));
                            eAgentInfo.addReport(eI);
                            eAgentInfo.addNContracts();

                            if (getGUI() != null) {
                                evidenceRecords.get(eAgInfo.get(i).getContexts().get(0).getSeller()).refresh(mapMet);
                                ((SinalphaCTRGui) gui).addValuesRow(mapMet, eAgInfo.get(i).getContexts().get(0).getSeller(), eAgentInfo.getNContracts(), eAgentInfo.getCTREval(false, null, ""));
                            }
                        }
                    }
                }

                if (((EIAgent) myAgent).isInSynchronizedExperiment()) {
                    // enviar informa��o final de execu��o do contrato para o synchronizer
                    ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
                    msg.setOntology("_" + ElectronicInstitution.SYNCHRONIZATION_ONTOLOGY); // martelada!
                    msg.addReceiver(new AID(((EIAgent) myAgent).getSynchronizer(), false));
                    try {
                        msg.setContentObject(enactmentOutcome);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    myAgent.send(msg);
                }


                if (filename != null) {
                    saveFile(filename);
                }

            } else if (report instanceof Obligation) {
                Obligation o = (Obligation) report;
                String ag = o.getBearer();
                EAgentInfo eAI = evidenceRecords.get(ag);
                if (eAI == null) // TODO: only sellers are being tracked, therefore an evidence record might not exist for this bearer
                {
                    return;
                }
                // increment the number of obligations of the specified agent
                eAI.addObligation(o);
            }
        }
    } // end NormEnvSubscriptionInit class

    /**
     * Inner class to implement the Synchronisation responder role.
     *
     * @author hlc
     */
    private class SyncResetResp extends SimpleAchieveREResponder {

        private static final long serialVersionUID = 2130794273613266404L;

        SyncResetResp(Agent agent, String synchronizer) {
            super(agent, MessageTemplate.and(
                    MessageTemplate.and(AchieveREResponder.createMessageTemplate(FIPANames.InteractionProtocol.FIPA_REQUEST),
                    MessageTemplate.and(MessageTemplate.MatchOntology(ElectronicInstitution.SYNCHRONIZATION_ONTOLOGY),
                    MessageTemplate.MatchSender(new AID(synchronizer, false)))),
                    MessageTemplate.MatchContent("reset")));
        }

        public ACLMessage prepareResultNotification(ACLMessage request, ACLMessage response) {

            // reset reputation records
            Enumeration<String> e = evidenceRecords.keys();
            while (e.hasMoreElements()) {
                String s = e.nextElement();
                EAgentInfo rr = new EAgentInfo(s);
                evidenceRecords.put(s, rr);
            }

            ACLMessage result = request.createReply();
            result.setPerformative(ACLMessage.INFORM);
            return result;
        }
    } // end SyncResetResp class

    /**
     * Inner class to implement the CTR responder role. The CTR agent will
     * receive requests on trust information of agents.
     *
     * @author hlc
     */
    private class CTRResp extends SimpleAchieveREResponder {

        private static final long serialVersionUID = 7405618360737605794L;
        // action received in the request message
        AgentAction request_action;

        /**
         * Creates a
         * <code>CTRResp</code> instance.
         *
         * @param agent The agent that adds the behaviour.
         */
        CTRResp(Agent agent) {
            super(agent, MessageTemplate.and(SimpleAchieveREResponder.createMessageTemplate(FIPA_REQUEST),
                    MessageTemplate.MatchOntology(ElectronicInstitution.CTR_ONTOLOGY)));
        }

        /**
         * Prepares an appropriate response.
         *
         * @param request the request
         *
         * @return the ACL message
         */
        protected ACLMessage prepareResponse(ACLMessage request) {
            ACLMessage reply = request.createReply();
            request_action = null;

            // check requested action
            try {
                // extract content
                ContentElement ce = myAgent.getContentManager().extractContent(request);
                if (ce instanceof Action) {
                    request_action = (AgentAction) ((Action) ce).getAction();
                }
            } catch (Codec.CodecException ce) {
                ce.printStackTrace();
            } catch (OntologyException oe) {
                oe.printStackTrace();
            }

            /**
             * Preararar resposta (Sinalpha) aqui!!!
             */
            
            //Only process requests such as SendInfo, SendTopAgents, SendCTREval
            if (request_action instanceof SendInfo || request_action instanceof SendCTREval
                    || request_action instanceof SendTopAgents) // if requested action is known and can have any sender, agree
            {
                reply.setPerformative(ACLMessage.AGREE);
            } else // reply with not-understood
            {
                reply.setPerformative(ACLMessage.NOT_UNDERSTOOD);
            }

            // return the created reply
            return reply;
        }

        /**
         * Prepares an appropriate reply.
         *
         * @param request the request
         * @param response the response
         *
         * @return the ACL message
         */
        protected ACLMessage prepareResultNotification(ACLMessage request, ACLMessage response) {
            ACLMessage reply = request.createReply();

            // check if an agent is specified
            if (request_action instanceof CTRAgentAction || request_action instanceof AgentAction) {

                // check requested action
                if (request_action instanceof SendInfo) {
                    // get the EAgentInfo for the agent
                    String ag = ((SendInfo) request_action).getAgent();
                    EAgentInfo eAI = evidenceRecords.get(ag);
                    if (eAI != null) {
                        reply.setPerformative(ACLMessage.INFORM);
                        Info info = new Info();

//FIXME

                        try {
                            myAgent.getContentManager().fillContent(reply, info);
                        } catch (Codec.CodecException ce) {
                            ce.printStackTrace();
                        } catch (OntologyException oe) {
                            oe.printStackTrace();
                        }
                    } else {
                        reply.setPerformative(ACLMessage.FAILURE);
                        reply.setContent("no such agent in evidence records");
                    }
                } else if (request_action instanceof SendCTREval) {
                    // get the EAgent record for the agent
                    String ag = ((SendCTREval) request_action).getAgent();
                    EAgentInfo eAI = evidenceRecords.get(ag);
                    // send the ctr evaluation of the specified agent
                    if (eAI != null) {
                        reply.setPerformative(ACLMessage.INFORM);
                        CTREval cTRE = new CTREval();
                        cTRE.setAgent(eAI.getAgent());

                        //Contextual Fitness
                        if (((SendCTREval) request_action).isContext()) {
                            cTRE.setValue(eAI.getCTREval(false, ((SendCTREval) request_action).getContract(), getARFF(eAI)));
                            //Without Contextual Fitness
                        } else {
                            cTRE.setValue(eAI.getCTREval(false, null, getARFF(eAI)));
                        }
                        try {
                            myAgent.getContentManager().fillContent(reply, cTRE);
                        } catch (Codec.CodecException ce) {
                            ce.printStackTrace();
                        } catch (OntologyException oe) {
                            oe.printStackTrace();
                        }
                    } else {
                        reply.setPerformative(ACLMessage.FAILURE);
                        reply.setContent("no such agent in CTR records");
                    }
                } else if (request_action instanceof SendTopAgents) {
                    // send the N agents with best CTR evaluation
                    SendTopAgents sTA = ((SendTopAgents) request_action);
                    int topNumberOfAg = sTA.getTopNumberOfAgents();
                    boolean useContextual = ((SendTopAgents) request_action).isUseContextual();
                    mapMet = sTA.getMapMet();
                    //MappingMethod mapMet = OutcomeGenerator.MappingMethod.Mapping4;
                    //System.out.println(":::"+mapMet);
                    reply.setPerformative(ACLMessage.INFORM);
                    TopAgents tA = new TopAgents();
                    Vector<AID> interestedAgents = sTA.getInterestedAgents();

                    Vector<AID> topAgents = new Vector<AID>();
                    Hashtable<AID, Vector<Double>> table = new Hashtable<AID, Vector<Double>>();

                    //Sele��o dos que tem handicap
                    if (topNumberOfAg == SendTopAgents.NO_HANDICAP) {
                        if ((interestedAgents == null) || (interestedAgents.isEmpty())) {
                            DFAgentDescription template = new DFAgentDescription();
                            ServiceDescription sd1 = new ServiceDescription();
                            sd1.setType(ElectronicInstitution.ROLE_SELLER_AGENT);
                            template.addServices(sd1);
                            DFAgentDescription[] result = null; //the result variable is an array of agents who are registered as enterprise agents
                            try {
                                result = DFService.search(myAgent, template);
                            } catch (FIPAException e) {
                                e.printStackTrace();
                            }
                            for (int a = 0; a < result.length; a++) {
                                AID agent = result[a].getName();

                                EAgentInfo eAI = evidenceRecords.get(agent.getLocalName());

                                eAI.refresh(mapMet);
                                double valueContext = eAI.getCTREval(true, sTA.getContract(), getARFF(eAI));
                                double value = eAI.getCTREval(false, null, "");
                                if (eAI.hasHandicap(sTA.getContract(), getARFF(eAI)) != 0.0) {
                                    Vector<Double> vec = new Vector<Double>();
                                    vec.add(value);
                                    if (useContextual) {
                                        vec.add(valueContext);
                                    } else {
                                        vec.add(value);
                                    }
                                    table.put(agent, vec);
                                }
                            }
                        } else {
                            for (AID ia : interestedAgents) {
                                EAgentInfo evRe = evidenceRecords.get(ia.getLocalName());

                                evRe.refresh(mapMet);
                                double valueContext = evRe.getCTREval(true, sTA.getContract(), getARFF(evRe));
                                double value = evRe.getCTREval(false, null, "");
                                if (evRe.hasHandicap(sTA.getContract(), getARFF(evRe)) != 0.0) {
                                    Vector<Double> vec = new Vector<Double>();
                                    vec.add(value);
                                    if (useContextual) {
                                        vec.add(valueContext);
                                    } else {
                                        vec.add(value);
                                    }
                                    table.put(ia, vec);
                                }
                            }
                        }
                    } else if (interestedAgents == null) {
                        DFAgentDescription template = new DFAgentDescription();
                        template = new DFAgentDescription();
                        ServiceDescription sd1 = new ServiceDescription();
                        sd1.setType(ElectronicInstitution.ROLE_SELLER_AGENT);
                        template.addServices(sd1);
                        DFAgentDescription[] result = null; //the result variable is an array of agents who are registered as enterprise agents
                        try {
                            result = DFService.search(myAgent, template);
                        } catch (FIPAException e) {
                            e.printStackTrace();
                        }
                        for (int a = 0; a < result.length; a++) {
                            AID agent = result[a].getName();
                            EAgentInfo eAgentInfo = evidenceRecords.get(agent.getLocalName());

//							if(eAgentInfo == null)
//							{
//									Enumeration<EAgentInfo> elems = evidenceRecords.elements();
//									while(elems.hasMoreElements())
//									{
//										EAgentInfo elem = elems.nextElement();
//										System.out.println("::"+elem.getAgent());
//									}
//							}
                            eAgentInfo.refresh(mapMet);
                            Vector<Double> vec = new Vector<Double>();
                            vec.add((eAgentInfo.getCTREval(false, null, "")));

                            if (useContextual) {
                                vec.add((eAgentInfo.getCTREval(true, sTA.getContract(), getARFF(eAgentInfo))));
                            } else {
                                vec.add((eAgentInfo.getCTREval(false, null, "")));
                            }
                            table.put(agent, vec);
                        }
                    } else if (!interestedAgents.isEmpty()) {
                        for (AID ia : interestedAgents) {
                            EAgentInfo eAgentInfo = evidenceRecords.get(ia.getLocalName());
                            eAgentInfo.refresh(mapMet);
                            Vector<Double> vec = new Vector<Double>();
                            vec.add((eAgentInfo.getCTREval(false, null, "")));

                            if (useContextual) {
                                vec.add((eAgentInfo.getCTREval(true, sTA.getContract(), getARFF(eAgentInfo))));
                            } else {
                                vec.add((eAgentInfo.getCTREval(false, null, "")));
                            }
                            table.put(ia, vec);
                        }
                    }

                    Vector<AID> nTopAgents = sortHashTable(table, useContextual);
                    Vector<Double> values = new Vector<Double>();
                    Vector<Double> valuesContext = new Vector<Double>();
                    int nValidAgents = ((topNumberOfAg == SendTopAgents.ALL || topNumberOfAg == SendTopAgents.NO_HANDICAP) ? nTopAgents.size() : topNumberOfAg);
                    for (int i = 0; i < nValidAgents && i < nTopAgents.size(); i++) {
                        topAgents.add(nTopAgents.get(i));
                        values.add(table.get(nTopAgents.get(i)).get(0));
                        valuesContext.add(table.get(nTopAgents.get(i)).get(1));
                    }
                    tA.setUseContextual(sTA.isUseContextual());
                    tA.setAgents(topAgents);
                    tA.setCtrEvaluations(values);
                    tA.setCtrContextualEvaluations(valuesContext);

                    try {
                        myAgent.getContentManager().fillContent(reply, tA);
                    } catch (Codec.CodecException ce) {
                        ce.printStackTrace();
                    } catch (OntologyException oe) {
                        oe.printStackTrace();
                    }
                }
            }
            receivedMessages.add(request);

            // return the created reply
            sentMessages.add(reply);
            return reply;

        }
    } // end CTRResp class

    /**
     * Returns the ARFF String to use in contextual evaluation
     *
     * @return
     */
    private String getARFF(EAgentInfo eAgentInfo) {
        // build ARFF string
        String ARFFString = new String("@relation product\n");

        // get remaining attributes (static)

        //ter um vector com todos os bens transacionados e procurar la para acrescentar
        ARFFString = ARFFString + "@attribute good {";

        HashSet products = new HashSet();
        Vector<ContractEnactment> contractEnactments = eAgentInfo.getContractHistoric();
        for (int i = 0; i < contractEnactments.size(); i++) {
            Vector<Context> contexts = contractEnactments.get(i).getContexts();
            for (int j = 0; j < contexts.size(); j++) {
                products.add(contexts.get(j).getName());
            }
        }
        Iterator it = products.iterator();
        if (it.hasNext()) {
            ARFFString = ARFFString + (String) it.next();
            while (it.hasNext()) {
                ARFFString = ARFFString + ", " + (String) it.next();
            }
        }

//		Vector<String> vec = getProducts();
//		if(vec.size()>0) {
//			ARFFString = ARFFString + vec.get(0);
//			for(int i = 1; i < vec.size(); i++) {
//				ARFFString = ARFFString + ", " + vec.get(i);
//			}
//		}

        ARFFString = ARFFString + "}\n";

        ARFFString = ARFFString + "@attribute quantity {low,medium,high}\n";
        ARFFString = ARFFString + "@attribute dtime {low,medium,big}\n";

        ARFFString = ARFFString + "@attribute supplier {";
        //Procurar tds os enterprise agents no CTRRecords
        DFAgentDescription[] dfads = fetchAgents(ElectronicInstitution.ROLE_USER_AGENT);
        ARFFString = ARFFString + dfads[0].getName().getLocalName();
        for (int i = 1; i < dfads.length; i++) {
            ARFFString = ARFFString + ", " + dfads[i].getName().getLocalName();
        }
        ARFFString = ARFFString + "}\n";

        ARFFString = ARFFString + "@attribute success {Fulfilled, Violated, DeadlineViolated, DeadlineViolatedFulfilled, DeadlineViolatedDeadlineViolated, DeadlineViolatedViolated, ViolatedFulfilled, ViolatedDeadlineViolated, ViolatedViolated, null}\n";
        return ARFFString;
    }

    /**
     * Returns a vector of AID sort by agents' evaluation
     *
     * @param table
     * @param useContextual
     * @return
     */
    @SuppressWarnings("unchecked")
    private Vector<AID> sortHashTable(Hashtable<AID, Vector<Double>> table, boolean useContextual) {
        Vector<AID> finalVec = new Vector<AID>();
        Hashtable<AID, Vector<Double>> tableAux = (Hashtable<AID, Vector<Double>>) table.clone();

        AID aux = null;
        //Double auxContextual = null, auxNoContextual = null;
        while (tableAux.size() > 0) {
            java.util.List<AID> l = (java.util.List<AID>) Collections.list(tableAux.keys());

            Collections.shuffle(l);

            Enumeration<AID> auxVec = Collections.enumeration(l);
            aux = auxVec.nextElement();
            while (auxVec.hasMoreElements()) {
                if (useContextual) {
                    AID aux2 = auxVec.nextElement();
                    if (table.get(aux2).get(1) > table.get(aux).get(1)) {
                        aux = aux2;
                    } else if (table.get(aux2).get(1) == table.get(aux).get(1)) {
                        if (table.get(aux2).get(0) > table.get(aux).get(0)) {
                            aux = aux2;
                        }
                    }

                } else {
                    AID aux2 = auxVec.nextElement();
                    if (table.get(aux2).get(0) > table.get(aux).get(0)) {
                        aux = aux2;
                    } else if (table.get(aux2).get(0) == table.get(aux).get(0)) {
                        if (table.get(aux2).get(1) > table.get(aux).get(1)) {
                            aux = aux2;
                        }
                    }
                }
            }
            finalVec.add(aux);
            tableAux.remove(aux);
        }
        return finalVec;
    }

    private Context generateContext(NewContract newContract) {
        // using reflection to create the proper contract generator

        List contractualInfos = newContract.getContractualInfos();
        String contractType = newContract.getType();

        // create context generator class name from contract type:
        // - the first character and all characters after hyphens ('-') or underscores ('_') will be uppercased
        // - hyphens and underscores will be removed
        StringBuffer contextGenerator = new StringBuffer("ei.service.ctr.context.");
        boolean capitalizeNext = true;
        for (int i = 0; i < contractType.length(); i++) {
            if (contractType.charAt(i) == '-' || contractType.charAt(i) == '_') {
                capitalizeNext = true;
            } else {
                if (capitalizeNext) {
                    contextGenerator.append(Character.toUpperCase(contractType.charAt(i)));
                    capitalizeNext = false;
                } else {
                    contextGenerator.append(contractType.charAt(i));
                }
            }
        }
        contextGenerator.append("ContextGenerator");

        try {
            Constructor c = Class.forName(contextGenerator.toString()).getConstructor(new Class[]{jade.util.leap.List.class});
            return ((AbstractContextGenerator) c.newInstance(contractualInfos)).generateContext();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void loadFile(String filename) {
//		CTREvidencesRecorder recorder = new CTREvidencesRecorder(evidenceRecords, filename);
//
//		Hashtable<String, EAgentInfo> ev = recorder.load();
//		if(ev != null)
//			this.evidenceRecords = ev;
    }

    public void saveFile(String filename) {
//		CTREvidencesRecorder recorder = new CTREvidencesRecorder(evidenceRecords, filename);
//		synchronized (recorder) {		
//			boolean result = recorder.save();
//			if(result) {
//				return;
//			} else {
//				System.err.println("ERROR CTR NOT SAVED");
//			}
//		}
    }

    public void setMapMet(MappingMethod mapMet) {
        this.mapMet = mapMet;
    }

    public MappingMethod getMapMet() {
        return mapMet;
    }
}
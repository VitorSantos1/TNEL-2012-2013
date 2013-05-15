package ei.proto.negotiation.qfnegotiation;

import ei.ElectronicInstitution;
import jade.core.Agent;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.proto.SSResponderDispatcher;

/**
 */
public abstract class QFNegotiationRespDispatcher extends SSResponderDispatcher {
	private static final long serialVersionUID = 1L;
	
    public QFNegotiationRespDispatcher(Agent agent) {
		super(agent,
				MessageTemplate.and(
						MessageTemplate.and(
								MessageTemplate.MatchPerformative(ACLMessage.CFP),
								MessageTemplate.MatchProtocol(ElectronicInstitution.QF_NEGOTIATION_PROTOCOL)),
						MessageTemplate.MatchOntology(ElectronicInstitution.QF_NEGOTIATION_ONTOLOGY))
			);
	}
    
}

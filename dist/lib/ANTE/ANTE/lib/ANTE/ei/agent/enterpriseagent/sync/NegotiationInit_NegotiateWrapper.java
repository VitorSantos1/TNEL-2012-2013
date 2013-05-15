package ei.agent.enterpriseagent.sync;

import jade.core.behaviours.WrapperBehaviour;
import jade.lang.acl.ACLMessage;

import java.io.IOException;

import ei.agent.enterpriseagent.negotiation.QFNegotiationInit_Negotiate;

/**
 * This class is for SYNCHRONIZATION purposes.
 * 
 * @author hlc
 */
public class NegotiationInit_NegotiateWrapper extends WrapperBehaviour {

	private static final long serialVersionUID = -3075370720612797685L;

	private Episode_NegotiationResp episodeResp;

	NegotiationInit_NegotiateWrapper(QFNegotiationInit_Negotiate negotiationInit_Negotiate, Episode_NegotiationResp episodeResp) {
		super(negotiationInit_Negotiate);

		this.episodeResp = episodeResp;
	}

	public boolean done() {
		boolean ret = this.getWrappedBehaviour().done();
		if(ret) {
			ACLMessage result = episodeResp.request.createReply();
			result.setPerformative(ACLMessage.INFORM);

			try {
				result.setContentObject(((QFNegotiationInit_Negotiate) this.getWrappedBehaviour()).getNegotiationOutcome());
			} catch (IOException e) {
				e.printStackTrace();
			}

			episodeResp.getDataStore().put(episodeResp.RESULT_NOTIFICATION_KEY, result);
		}
		return ret;
	}

} // end NegotiationInit_NegotiateWrapper class


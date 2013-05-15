package ei.service.negotiation.qfnegotiation;

import ei.service.ctr.OutcomeGenerator.MappingMethod;

/**
 * Negotiation Parameters 
 */
public class  QFNegotiationParameters {
	public static final String NEGOTIATION_ROUNDS = "negotiationRounds";
	public static final String CONTRACT_TYPE = "contractType";
	public static final String TOP_N = "topNumberOfAgents";
	public static final String USE_CONTEXTUAL_FITNESS = "useContextualFitness";
	public static final String USE_TRUST_IN_PRESELECTION = "useTrustInPreselection";
	public static final String USE_TRUST_IN_PROPOSAL_EVALUATION = "useTrustInProposalEvaluation";
	public static final String USE_TRUST_IN_CONTRACT_DRAFTING = "useTrustInContractDrafting";
	public static final String RISK_TOLERANCE = "risk_tolerance";
	public static final String MAPPING_METHOD = "mapping_method";
	public static final String AUTO_MODE = "auto_mode";
	
	public static final int NEGOTIATION_ROUNDS_DEFAULT = 8;
	public static final String CONTRACT_TYPE_DEFAULT = "contract-of-sale";
	public static final int TOP_N_DEFAULT = -1;
	public static final boolean USE_CONTEXTUAL_FITNESS_DEFAULT = false;
	public static final boolean USE_TRUST_IN_PRESELECTION_DEFAULT = false;
	public static final boolean USE_TRUST_IN_PROPOSAL_EVALUATION_DEFAULT = false;
	public static final boolean USE_TRUST_IN_CONTRACT_DRAFTING_DEFAULT = false;
	public static final double RISK_TOLERANCE_DEFAULT = 0.0;
	public static final MappingMethod MAPPING_METHOD_DEFAULT = MappingMethod.values()[0];
	public static final boolean AUTO_MODE_DEFAULT = false;
}

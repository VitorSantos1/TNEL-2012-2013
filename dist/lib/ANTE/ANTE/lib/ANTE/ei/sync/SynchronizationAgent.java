package ei.sync;

import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;   
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Vector;

import ei.ElectronicInstitution;
import ei.agent.ExternalAgent;
import ei.onto.normenv.report.NEReportOntology;
import ei.onto.synchronization.StartEpisode;
import ei.onto.synchronization.SynchronizationOntology;
import ei.onto.util.Parameters;
import ei.service.ctr.OutcomeGenerator.Outcome;
import ei.service.negotiation.qfnegotiation.NegotiationOutcome;

import jade.content.lang.Codec;
import jade.content.onto.OntologyException;
import jade.content.onto.basic.Action;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.ParallelBehaviour;
import jade.core.behaviours.SequentialBehaviour;
import jade.core.behaviours.SimpleBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPANames;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;
import jade.proto.SimpleAchieveREInitiator;

public class SynchronizationAgent extends ExternalAgent {

	private static final long serialVersionUID = -7444981393031606832L;

	private static final String SIMULATION_ID = "5";
	
/*###############################################################################################################*/
/*                                                                                                               */	
/*                                                                                                               */	
/*                                                                                                               */	
/*                                                                                                               */	
/**/   private static final String RUN_ID = "129"; // because NUMBER_OF_RUNS does not work for now...            */
/*                                                                                                               */	
/*                                                                                                               */	
/*                                                                                                               */	
/*                                                                                                               */
/*###############################################################################################################*/

	private static final int NUMBER_OF_RUNS = 1;
	private static final int NUMBER_OF_EPISODES = 500;
	private static final String CONTRACT_TYPE = "asaig-plus";

	private static final int NEGOTIATION_ROUNDS = 50;
	private static final int TOP_N = -1;
	private static final boolean USE_CONTEXTUAL = false;
	private static final boolean USE_TRUST_IN_PRESELECTION = false;	
	private static final boolean USE_TRUST_IN_PROPOSAL_EVALUATION = false;
	private static final boolean USE_TRUST_IN_CONTRACT_DRAFTING = false;
	
	private static final double PERCENT_UTILITY_LOSS_MIN = 0.0;   // minimum utility loss in unsuccessful contracts
	private static final double PERCENT_UTILITY_LOSS_MAX = 0.0;   // maximum utility loss in unsuccessful contracts (exclusive)
	private static final double PERCENT_UTILITY_LOSS_STEP = 0.10;
	
	private DecimalFormat decimalFormat = new DecimalFormat("0.00");

	private static final String FOLDER = "sync/";
	private static final String FILE_EXTENSION = ".txt";
	private static final String SIMULATION_FILENAME_BASE = FOLDER+"sim_"+SIMULATION_ID+"_"+RUN_ID;
	private static final String SIMULATION_FILENAME = FOLDER+"sim_"+SIMULATION_ID + FILE_EXTENSION;
	
	private Vector<AID> buyerAgents = new Vector<AID>();
	private Vector<EnactmentOutcome> enactmentOutcomes = new Vector<EnactmentOutcome>();

	private String ctr = null;
	
	public void setup() {
		super.setup();

		// register ontology
		getContentManager().registerOntology( SynchronizationOntology.getInstance()   );
		getContentManager().registerOntology( NEReportOntology.getInstance()   );
		
		DFAgentDescription template = new DFAgentDescription();
		template = new DFAgentDescription();
		ServiceDescription sd = new ServiceDescription();
		sd.setType(ElectronicInstitution.ROLE_BUYER_AGENT);
		template.addServices(sd);
		DFAgentDescription[] result = null;
		try {
			result = DFService.search(this, template);
		} catch (FIPAException e) {
			e.printStackTrace();
		}
		for(int a=0;a<result.length;a++) {
			buyerAgents.add(result[a].getName());
		}

		// add listener for contract enactment outcomes
		if(getConfigurationArguments().containsKey("ctr")) {
			ctr = (String) getConfigurationArguments().get("ctr");
		} else {
			ctr = "ctr";
		}
		addBehaviour(new EnactmentOutcomeListening(this, ctr));
		
		// start synchronized experiment
		addBehaviour(new Synchronization(this, NUMBER_OF_RUNS, NUMBER_OF_EPISODES));
	}


	private class Synchronization extends SequentialBehaviour {

		private static final long serialVersionUID = -4287819322823401407L;

		public Synchronization(Agent agent, int numberOfRuns, int numberOfEpisodes) {
			super(agent);
			
			// create runs
			for(int r=0; r<numberOfRuns; r++) {
				
//				// reset the ctr
//				if(((SynchronizationAgent) myAgent).ctr != null) {
//					addSubBehaviour(new SyncResetInit(myAgent, ((SynchronizationAgent) myAgent).ctr));
//				}
				
				String fileName = SIMULATION_FILENAME_BASE + "_run_" + 2 + FILE_EXTENSION;
				addSubBehaviour(new Run(myAgent, fileName, r, numberOfEpisodes));
			}
		}

	}
	
	private class Run extends SequentialBehaviour {

		private static final long serialVersionUID = -4287819322823401407L;

		private String fileName;
		private ArrayList<EpisodeWrapper> episodeWrapperList;
		private int runNumber;
		
		public Run(Agent agent, String fileName, int runNumber, int numberOfEpisodes) {
			super(agent);
			
			this.fileName = fileName;
			episodeWrapperList = new ArrayList<EpisodeWrapper>();
			this.runNumber = runNumber;
			
			// create episodes
			for(int e=0; e<numberOfEpisodes; e++) {
				EpisodeWrapper episodeWrapper = new EpisodeWrapper(myAgent, fileName, e, buyerAgents);
				addSubBehaviour(episodeWrapper);
				episodeWrapperList.add(episodeWrapper);
			}
		}
		
		public void onStart() {
			//System.out.println("************** Start Run " + runNumber + " **************");
			writeRunLine(fileName, "*** RUN " + runNumber);
		}
		
		public int onEnd() {
			//System.out.println("************** End Run " + runNumber + " **************");

			// log summary for this run

			float avgNumberOfDifferentSuppliers = 0.0f;
			float avgAvgNegotiatedUtility = 0.0f;
			float avgAvgLastRoundUtility = 0.0f;			
			float avgLastRoundUtilityStDev = 0.0f;			
			float avgNumberOfF_NAContracts = 0.0f;
			float avgNumberOfFd_FContracts = 0.0f;
			float avgNumberOfFd_VContracts = 0.0f;
			float avgNumberOfV_FContracts = 0.0f;
			float avgNumberOfV_VContracts = 0.0f;
			double avgTFFHutility = 0.0;
			double avgTFFButility = 0.0;
			double avgTTFRutility = 0.0;
			double avgIFFHutility = 0.0;
			double avgIFFButility = 0.0;
			double avgITFRutility = 0.0;

			double avgNumberOfFineTFFH = 0d;
			double avgNumberOfPayedFineTFFH = 0d;
			double avgNumberOfFineIFFH = 0d;
			double avgNumberOfPayedFineIFFH = 0d;
			double avgNumberOfFineTFFB = 0d;
			double avgNumberOfPayedFineTFFB = 0d;
			double avgNumberOfFineIFFB = 0d;
			double avgNumberOfPayedFineIFFB = 0d;
			double avgNumberOfFineTTFR = 0d;
			double avgNumberOfPayedFineTTFR = 0d;
			double avgNumberOfFineITFR = 0d;
			double avgNumberOfPayedFineITFR = 0d;
			
			double avgNumberOfSupPerTFFH = 0d;
			double avgNumberOfSupPerIFFH = 0d;
			double avgNumberOfSupPerTFFB = 0d;
			double avgNumberOfSupPerIFFB = 0d;
			double avgNumberOfSupPerTTFR = 0d;
			double avgNumberOfSupPerITFR = 0d;
			
			double avgFineTTFH = 0.0;
			double avgFineITFH = 0.0;
			double avgFineTTFB = 0.0;
			double avgFineITFB = 0.0;
			double avgFineTTFR = 0.0;
			double avgFineITFR = 0.0;
			
			float avgAvgSuccessfulUtility = 0.0f;
			float[] avgAvgUtility = new float[(int) Math.round((PERCENT_UTILITY_LOSS_MAX - PERCENT_UTILITY_LOSS_MIN) / PERCENT_UTILITY_LOSS_STEP)];
			for(int j=0; j<avgAvgUtility.length; j++) {
				avgAvgUtility[j] = 0.0f;
			}
			
			for(int i=0; i<episodeWrapperList.size(); i++) {
				avgNumberOfDifferentSuppliers += episodeWrapperList.get(i).numberOfDifferentSuppliers;
				avgAvgNegotiatedUtility += episodeWrapperList.get(i).avgNegotiatedUtility;
				avgAvgLastRoundUtility += episodeWrapperList.get(i).avgEpisodeLastRoundUtility;	
				avgLastRoundUtilityStDev += episodeWrapperList.get(i).totalLastUtilityStDev;
				avgNumberOfF_NAContracts += episodeWrapperList.get(i).numberOfF_NAContracts;
				avgNumberOfFd_FContracts += episodeWrapperList.get(i).numberOfFd_FContracts;
				avgNumberOfFd_VContracts += episodeWrapperList.get(i).numberOfFd_VContracts;
				avgNumberOfV_FContracts += episodeWrapperList.get(i).numberOfV_FContracts;
				avgNumberOfV_VContracts += episodeWrapperList.get(i).numberOfV_VContracts;
				avgAvgSuccessfulUtility += episodeWrapperList.get(i).avgSuccessfulUtility;
				
				avgTFFHutility += episodeWrapperList.get(i).avgTFFHutility;
				avgTFFButility += episodeWrapperList.get(i).avgTFFButility;
				avgTTFRutility += episodeWrapperList.get(i).avgTTFRutility;
				avgIFFHutility += episodeWrapperList.get(i).avgIFFHutility;
				avgIFFButility += episodeWrapperList.get(i).avgIFFButility;
				avgITFRutility += episodeWrapperList.get(i).avgITFRutility;

				avgNumberOfSupPerTFFH += episodeWrapperList.get(i).numberOfSupPerTFFH;
				avgNumberOfSupPerIFFH += episodeWrapperList.get(i).numberOfSupPerIFFH;
				avgNumberOfSupPerTFFB += episodeWrapperList.get(i).numberOfSupPerTFFB;
				avgNumberOfSupPerIFFB += episodeWrapperList.get(i).numberOfSupPerIFFB;
				avgNumberOfSupPerTTFR += episodeWrapperList.get(i).numberOfSupPerTTFR;
				avgNumberOfSupPerITFR += episodeWrapperList.get(i).numberOfSupPerITFR;
				
				avgNumberOfFineTFFH += episodeWrapperList.get(i).numberOfFineTFFH;
				avgNumberOfPayedFineTFFH += episodeWrapperList.get(i).numberOfPayedFineTFFH;;
				avgNumberOfFineIFFH += episodeWrapperList.get(i).numberOfFineIFFH;
				avgNumberOfPayedFineIFFH += episodeWrapperList.get(i).numberOfPayedFineIFFH;
				avgNumberOfFineTFFB += episodeWrapperList.get(i).numberOfFineTFFB;
				avgNumberOfPayedFineTFFB += episodeWrapperList.get(i).numberOfPayedFineTFFB;
				avgNumberOfFineIFFB += episodeWrapperList.get(i).numberOfFineIFFB;
				avgNumberOfPayedFineIFFB += episodeWrapperList.get(i).numberOfPayedFineIFFB;
				avgNumberOfFineTTFR += episodeWrapperList.get(i).numberOfFineTTFR;
				avgNumberOfPayedFineTTFR += episodeWrapperList.get(i).numberOfPayedFineTTFR;
				avgNumberOfFineITFR += episodeWrapperList.get(i).numberOfFineITFR;
				avgNumberOfPayedFineITFR += episodeWrapperList.get(i).numberOfPayedFineITFR;
				
				avgFineTTFH += episodeWrapperList.get(i).fineTTFH;
				avgFineITFH += episodeWrapperList.get(i).fineITFH;
				avgFineTTFB += episodeWrapperList.get(i).fineTTFB;
				avgFineITFB += episodeWrapperList.get(i).fineITFB;
				avgFineTTFR += episodeWrapperList.get(i).fineTTFR;
				avgFineITFR += episodeWrapperList.get(i).fineITFR;
				
				for(int j=0; j<avgAvgUtility.length; j++) {
					avgAvgUtility[j] += episodeWrapperList.get(i).avgUtility[j];
				}
			}
			avgNumberOfDifferentSuppliers /= episodeWrapperList.size();
			avgAvgNegotiatedUtility /= episodeWrapperList.size();
			avgAvgLastRoundUtility /= episodeWrapperList.size();
			avgAvgSuccessfulUtility /= episodeWrapperList.size();
			System.out.println(" ------------>"+(episodeWrapperList.size()*enactmentOutcomes.size()));
			avgLastRoundUtilityStDev /= (episodeWrapperList.size()*enactmentOutcomes.size()); //dividir por (episodeWrapperList.size() * nClient) FIXME
			
			avgNumberOfF_NAContracts /= episodeWrapperList.size();
			avgNumberOfFd_FContracts /= episodeWrapperList.size();
			avgNumberOfFd_VContracts /= episodeWrapperList.size();
			avgNumberOfV_FContracts /= episodeWrapperList.size();
			avgNumberOfV_VContracts /= episodeWrapperList.size();
			
			avgTFFHutility /= episodeWrapperList.size();
			avgTFFButility /= episodeWrapperList.size();
			avgTTFRutility /= episodeWrapperList.size();
			avgIFFHutility /= episodeWrapperList.size();
			avgIFFButility /= episodeWrapperList.size();
			avgITFRutility /= episodeWrapperList.size();
			
			avgNumberOfSupPerTFFH /= episodeWrapperList.size();
			avgNumberOfSupPerIFFH /= episodeWrapperList.size();
			avgNumberOfSupPerTFFB /= episodeWrapperList.size();
			avgNumberOfSupPerIFFB /= episodeWrapperList.size();
			avgNumberOfSupPerTTFR /= episodeWrapperList.size();
			avgNumberOfSupPerITFR /= episodeWrapperList.size();

			avgNumberOfFineTFFH /= episodeWrapperList.size();
			avgNumberOfPayedFineTFFH /= episodeWrapperList.size();
			avgNumberOfFineIFFH /= episodeWrapperList.size();
			avgNumberOfPayedFineIFFH /= episodeWrapperList.size();
			avgNumberOfFineTFFB /= episodeWrapperList.size();
			avgNumberOfPayedFineTFFB /= episodeWrapperList.size();
			avgNumberOfFineIFFB /= episodeWrapperList.size();
			avgNumberOfPayedFineIFFB /= episodeWrapperList.size();
			avgNumberOfFineTTFR /= episodeWrapperList.size();
			avgNumberOfPayedFineTTFR /= episodeWrapperList.size();
			avgNumberOfFineITFR /= episodeWrapperList.size();
			avgNumberOfPayedFineITFR /= episodeWrapperList.size();
			
			avgFineTTFH /= episodeWrapperList.size();
			avgFineITFH /= episodeWrapperList.size();
			avgFineTTFB /= episodeWrapperList.size();
			avgFineITFB /= episodeWrapperList.size();
			avgFineTTFR /= episodeWrapperList.size();
			avgFineITFR /= episodeWrapperList.size();
			
			StringBuffer sb1 = new StringBuffer("\tavgDiffSuppliers\tavgNegUtility\tavgF\tavgFd_F\tavgFd_V\tavgV_F\tavgV_V\tavgUtility0\tavgTFFHutility\tavgIFFHutility\tavgTFFButility\tavgIFFButility\tavgTTFRutility\tavgITFRutility\tavgTFFH\tavgIFFH\tavgTFFB\tavgIFFB\tavgTTFR\tavgITFR\tavgPayedTFFH\tavgPayedIFFH\tavgPayedTFFB\tavgPayedIFFB\tavgPayedTTFR\tavgPayedITFR\tavgPenaltyTTFH\tavgPenaltyITFH\tavgPenaltyTTFB\tavgPenaltyITFB\tavgPenaltyTTFR\tavgPenaltyITFR  \tavgRefSupTFFH\tavgRefSupIFFH\tavgRefSupTFFB\tavgRefSupIFFB\tavgRefSupTTFR\tavgRefSupITFR");
			StringBuffer sb2 = new StringBuffer("\t" + avgNumberOfDifferentSuppliers + 
												"\t" + decimalFormat.format(avgAvgNegotiatedUtility) + 
												"\t" + avgNumberOfF_NAContracts + 
												"\t" + avgNumberOfFd_FContracts + 
												"\t" + avgNumberOfFd_VContracts + 
												"\t" + avgNumberOfV_FContracts + 
												"\t" + avgNumberOfV_VContracts + 
												"\t" + decimalFormat.format(avgAvgSuccessfulUtility) + 
												"\t" + decimalFormat.format(avgTFFHutility) + 
												"\t" + decimalFormat.format(avgIFFHutility) + 
												"\t" + decimalFormat.format(avgTFFButility) + 
												"\t" + decimalFormat.format(avgIFFButility) + 
												"\t" + decimalFormat.format(avgTTFRutility) + 
												"\t" + decimalFormat.format(avgITFRutility) + 
												"\t" + avgNumberOfFineTFFH + 
												"\t" + avgNumberOfFineIFFH + 
												"\t" + avgNumberOfFineTFFB + 
												"\t" + avgNumberOfFineIFFB + 
												"\t" + avgNumberOfFineTTFR + 
												"\t" + avgNumberOfFineITFR + 
												"\t" + avgNumberOfPayedFineTFFH + 
												"\t" + avgNumberOfPayedFineIFFH + 
												"\t" + avgNumberOfPayedFineTFFB + 
												"\t" + avgNumberOfPayedFineIFFB + 
												"\t" + avgNumberOfPayedFineTTFR + 
												"\t" + avgNumberOfPayedFineITFR + 
												"\t" + decimalFormat.format(avgFineTTFH) + 
												"\t" + decimalFormat.format(avgFineITFH) + 
												"\t" + decimalFormat.format(avgFineTTFB) + 
												"\t" + decimalFormat.format(avgFineITFB) + 
												"\t" + decimalFormat.format(avgFineTTFR) + 
												"\t" + decimalFormat.format(avgFineITFR) + 
												"\t" + avgNumberOfSupPerTFFH + 
												"\t" + avgNumberOfSupPerIFFH + 
												"\t" + avgNumberOfSupPerTFFB + 
												"\t" + avgNumberOfSupPerIFFB + 
												"\t" + avgNumberOfSupPerTTFR + 
												"\t" + avgNumberOfSupPerITFR);
			
			int j=0;
			for(double d=PERCENT_UTILITY_LOSS_MIN; d<PERCENT_UTILITY_LOSS_MAX; d+=PERCENT_UTILITY_LOSS_STEP, j++) {
				System.out.println("-->"+avgAvgUtility[j]);
				avgAvgUtility[j] /= episodeWrapperList.size();
				System.out.println("-->"+avgAvgUtility[j]);
				sb1.append("\tavgUtility" + decimalFormat.format(d).substring(2));
				sb2.append("\t" + decimalFormat.format(avgAvgUtility[j]));
			}

			String runLine1 = sb1.toString() + "\n";
			String runLine2 = sb2.toString();

			writeRunLine(fileName, runLine1+runLine2);
			
			writeRunLine(SIMULATION_FILENAME, runLine2);

			return 0;
		}
		
	    private void writeRunLine(String fileName, String line) {
	    	FileWriter fw;
	    	try {
	    		fw = new FileWriter(fileName, true);
	    		fw.write(line + "\n");
	    		fw.close();
	    	} catch(IOException e) {
	    		e.printStackTrace();
	    	}
	    }

	}


	// FIXME: for now it is assumed that every buyer succeeds in negotiating a contract
	// (the number of negotiationOutcomes and enactmentOutcomes is equal to the number of buyers)
	private class EpisodeWrapper extends SequentialBehaviour {
		
		private static final long serialVersionUID = -9138670219624582750L;
		
		private String fileName;
		private int episodeNumber;
		private Episode_NegotiationParallelInits episode_NegotiationParallelInits;

		private int numberOfDifferentSuppliers;
		private float avgNegotiatedUtility;
		private float avgEpisodeLastRoundUtility;		
		private float totalLastUtilityStDev;
		private double avgTFFHutility;
		private double avgTFFButility;
		private double avgTTFRutility;
		private double avgIFFHutility;
		private double avgIFFButility;
		private double avgITFRutility;
		
		private int numberOfF_NAContracts;
		private int numberOfFd_FContracts;
		private int numberOfFd_VContracts;
		private int numberOfV_FContracts;
		private int numberOfV_VContracts;
		private int numberOfFineTFFH;
		private int numberOfPayedFineTFFH;
		private int numberOfFineIFFH;
		private int numberOfPayedFineIFFH;
		private int numberOfFineTFFB;
		private int numberOfPayedFineTFFB;
		private int numberOfFineIFFB;
		private int numberOfPayedFineIFFB;
		private int numberOfFineTTFR;
		private int numberOfPayedFineTTFR;
		private int numberOfFineITFR;
		private int numberOfPayedFineITFR;
		
		private int numberOfSupPerTFFH;
		private int numberOfSupPerIFFH;
		private int numberOfSupPerTFFB;
		private int numberOfSupPerIFFB;
		private int numberOfSupPerTTFR;
		private int numberOfSupPerITFR;
		
		
		private int TFFH;
		private int TFFB;
		private int TTFR;
		private int IFFH;
		private int IFFB;
		private int ITFR;
		private double fineTTFH;
		private double fineITFH;
		private double fineTTFB;
		private double fineITFB;
		private double fineTTFR;
		private double fineITFR;
		
		private float avgSuccessfulUtility;
		private float[] avgUtility;
		
		public EpisodeWrapper(Agent agent, String fileName, int episodeNumber, Vector<AID> buyerAgents) {
			super(agent);
			
			this.fileName = fileName;
			this.episodeNumber = episodeNumber;
			
			episode_NegotiationParallelInits = new Episode_NegotiationParallelInits(myAgent, episodeNumber, buyerAgents);
			addSubBehaviour(episode_NegotiationParallelInits);
			
			addSubBehaviour(new WaitForEpisodeEnd(myAgent, episode_NegotiationParallelInits));

			totalLastUtilityStDev = 0;
			numberOfF_NAContracts = 0;
			numberOfFd_FContracts = 0;
			numberOfFd_VContracts = 0;
			numberOfV_FContracts = 0;
			numberOfV_VContracts = 0;
			
			numberOfFineTFFH = 0;
			numberOfPayedFineTFFH = 0;
			numberOfFineIFFH = 0;
			numberOfPayedFineIFFH = 0;
			numberOfFineTFFB = 0;
			numberOfPayedFineTFFB = 0;
			numberOfFineIFFB = 0;
			numberOfPayedFineIFFB = 0;
			numberOfFineTTFR = 0;
			numberOfPayedFineTTFR = 0;
			numberOfFineITFR = 0;
			numberOfPayedFineITFR = 0;

			TFFH = 0;
			TFFB = 0;
			TTFR = 0;
			IFFH = 0;
			IFFB = 0;
			ITFR = 0;
			
			numberOfSupPerTFFH = 0;
			numberOfSupPerIFFH = 0;
			numberOfSupPerTFFB = 0;
			numberOfSupPerIFFB = 0;
			numberOfSupPerTTFR = 0;
			numberOfSupPerITFR = 0;
			
			fineTTFH = 0d;
			fineITFH = 0d;
			fineTTFB = 0d;
			fineITFB = 0d;
			fineTTFR = 0d;
			fineITFR = 0d;
			
			avgUtility = new float[(int) Math.round((PERCENT_UTILITY_LOSS_MAX - PERCENT_UTILITY_LOSS_MIN) / PERCENT_UTILITY_LOSS_STEP)];
		}

		public void onStart() {
			/*FileWriter out;
			try {
				out = new FileWriter("printLog.txt", true);
				out.write("--- Start Episode " + episodeNumber+"\n");
				out.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}*/

			System.out.println("--- Start Episode " + episodeNumber);
		}
		
		public int onEnd() {
			log("--- End Episode " + episodeNumber);
			// log outcome of episode
			
			String[] episodeOutcomes = new String[episode_NegotiationParallelInits.episode_NegotiationInitList.size()];
			double negotiatedUtility;
			double avgLastRoundUtility;
			double lastRoundUtilityStDev;
			
			float totalUtility = 0.0f;
			float totalAvgLastRoundUtility = 0.0f;			
			float totalLastRoundUtilityStDev = 0.0f;
			float totalSuccUtility = 0.0f;
			double TFFHutility = 0.0;
			double TFFButility = 0.0;
			double TTFRutility = 0.0;
			double IFFHutility = 0.0;
			double IFFButility = 0.0;
			double ITFRutility = 0.0;
			
			//MappingMethod mapMet = MappingMethod.IntFailDel;

			double fine;

			String typeClient = "";
			int[] numbSup = new int[6];

			int numberOfRefusedSuppliers;			
			
			for(int i=0; i<episode_NegotiationParallelInits.episode_NegotiationInitList.size(); i++) {
				String buyer = episode_NegotiationParallelInits.episode_NegotiationInitList.get(i).buyerAgent.getLocalName();
				negotiatedUtility = episode_NegotiationParallelInits.episode_NegotiationInitList.get(i).negotiationOutcome.getUtility();
				avgLastRoundUtility = episode_NegotiationParallelInits.episode_NegotiationInitList.get(i).negotiationOutcome.getLastRoundAvgUtility();
				lastRoundUtilityStDev = episode_NegotiationParallelInits.episode_NegotiationInitList.get(i).negotiationOutcome.getLastRoundUtilityStDev();	

				fine = episode_NegotiationParallelInits.episode_NegotiationInitList.get(i).negotiationOutcome.getFine();


				numberOfRefusedSuppliers = episode_NegotiationParallelInits.episode_NegotiationInitList.get(i).negotiationOutcome.getNumberOfRefusedSuppliers();
				
/*
				
				System.out.println(
						" +++++++++++++++++++++---- " +  penaltyFd +
								" ----- " +  penaltyV + 
								" ----- " + fine +
								" ----- " + riskTolerance
				);*/
				
				StringBuffer episodeOutcome = new StringBuffer();
				// find buyer in enactmentOutcomes
				for(int e=0; e<enactmentOutcomes.size(); e++) {
					
					//Client	Supplier	negUtility	outcome(F, FdF,..., VV)	negUtility*outcomeMapeado
					
					if(enactmentOutcomes.get(e).getAgents().contains(buyer)) {
						
						//Client
						episodeOutcome.append(buyer);
						String seller;
						if(enactmentOutcomes.get(e).getAgents().get(0).equals(buyer)) {
							seller = enactmentOutcomes.get(e).getAgents().get(1);
						} else {
							seller = enactmentOutcomes.get(e).getAgents().get(0);
						}
						
						//Supplier
						episodeOutcome.append("\t" + seller);
						
						//negUtility
						episodeOutcome.append("\t" + decimalFormat.format(negotiatedUtility)); 
						
						Vector<Outcome> vec = enactmentOutcomes.get(e).getEnactment();


						if(vec.get(0).equals(Outcome.Fulfilled))
						{
							//System.out.println("aqui!");
							//F_NA
							episodeOutcome.append("\tF_NA");
							numberOfF_NAContracts++;
							fine = 0.0;
						}
						
						totalAvgLastRoundUtility += avgLastRoundUtility;						
						totalLastRoundUtilityStDev += lastRoundUtilityStDev;
						episodeOutcome.append("\t" + typeClient);
						episodeOutcome.append("\t" + decimalFormat.format(fine));
						//episodeOutcome.append("\t" + decimalFormat.format(avgLastRoundUtility));
						//episodeOutcome.append("\t" + decimalFormat.format(lastRoundUtilityStDev));		
						//drop off for loop
						e = enactmentOutcomes.size();
					}
				}
				episodeOutcomes[i] = episodeOutcome.toString();
			}
			avgNegotiatedUtility = totalUtility / episode_NegotiationParallelInits.episode_NegotiationInitList.size();
			avgSuccessfulUtility = totalSuccUtility / episode_NegotiationParallelInits.episode_NegotiationInitList.size();
			
			if(TFFH<1)
				avgTFFHutility = 0d;
			else
				avgTFFHutility = TFFHutility / TFFH;
			
			if(IFFH<1)
				avgIFFHutility = 0d;
			else
				avgIFFHutility = IFFHutility / IFFH;
			
			if(TFFB<1)
				avgTFFButility = 0d;
			else
				avgTFFButility = TFFButility / TFFB;
			
			if(IFFB<1)
				avgIFFButility = 0d;
			else
				avgIFFButility = IFFButility / IFFB;
			
			if(TTFR<1)
				avgTTFRutility = 0d;
			else
				avgTTFRutility = TTFRutility / TTFR;
			
			if(ITFR<1)
				avgITFRutility = 0d;
			else
				avgITFRutility = ITFRutility / ITFR;
			
			avgEpisodeLastRoundUtility = totalAvgLastRoundUtility / episode_NegotiationParallelInits.episode_NegotiationInitList.size();
			
			totalLastUtilityStDev += totalLastRoundUtilityStDev;			
			
			// collect the *different* agents (buyers and suppliers) that participated
			HashSet<String> agents = new HashSet<String>();
			for(int i=0; i<episodeOutcomes.length; i++) {
				for(int j=0; j<enactmentOutcomes.get(i).getAgents().size(); j++) {
					agents.add(enactmentOutcomes.get(i).getAgents().get(j));
				}
			}
			// the number of different suppliers is the total number of agents minus the number of buyers, which is equal to the number of enactment outcomes
			numberOfDifferentSuppliers = agents.size()-enactmentOutcomes.size();
			String episodeHeader = "Episode " + episodeNumber;
			
			StringBuffer sb1 = new StringBuffer("\tdiffSuppliers\tnegUtility\tF\tFd_F\tFd_V\tV_F\tV_V\tutility0\tTFFHutility\tITFHutility\tTFFButility\tIFFButility\tTTFRutility\tITFRutility\tTTFH\tIFFH\tTFFB\tIFFB\tTTFR\tITFR\tpayedTTFH\tpayedIFFH\tpayedTFFB\tpayedIFFB\tpayedTTFR\tpayedITFR\tpenaltyTTFH\tpenaltyITFH\tpenaltyTTFB\tpenaltyITFB\tpenaltyTTFR\tpenaltyITFR\tRefSupTTFH\tRefSupIFFH\tRefSupTFFB\tRefSupIFFB\tRefSupTTFR\tRefSupITFR");
			StringBuffer sb2 = new StringBuffer("\t" + numberOfDifferentSuppliers + 
												"\t" + decimalFormat.format(avgNegotiatedUtility) + 
												"\t" + numberOfF_NAContracts + 
												"\t" + numberOfFd_FContracts + 
												"\t" + numberOfFd_VContracts + 
												"\t" + numberOfV_FContracts + 
												"\t" + numberOfV_VContracts + 
												"\t" + decimalFormat.format(avgSuccessfulUtility) + 
												"\t" + decimalFormat.format(avgTFFHutility) + 
												"\t" + decimalFormat.format(avgIFFHutility) + 
												"\t" + decimalFormat.format(avgTFFButility) + 
												"\t" + decimalFormat.format(avgIFFButility) + 
												"\t" + decimalFormat.format(avgTTFRutility) + 
												"\t" + decimalFormat.format(avgITFRutility) + 
												"\t" + numberOfFineTFFH + 
												"\t" + numberOfFineIFFH + 
												"\t" + numberOfFineTFFB + 
												"\t" + numberOfFineIFFB + 
												"\t" + numberOfFineTTFR + 
												"\t" + numberOfFineITFR + 
												"\t" + numberOfPayedFineTFFH + 
												"\t" + numberOfPayedFineIFFH + 
												"\t" + numberOfPayedFineTFFB + 
												"\t" + numberOfPayedFineIFFB + 
												"\t" + numberOfPayedFineTTFR + 
												"\t" + numberOfPayedFineITFR + 
												"\t" + decimalFormat.format(fineTTFH) + 
												"\t" + decimalFormat.format(fineITFH) + 
												"\t" + decimalFormat.format(fineTTFB) + 
												"\t" + decimalFormat.format(fineITFB) + 
												"\t" + decimalFormat.format(fineTTFR) + 
												"\t" + decimalFormat.format(fineITFR) + 
												"\t" + numberOfSupPerTFFH +	
												"\t" + numberOfSupPerIFFH + 
												"\t" + numberOfSupPerTFFB + 
												"\t" + numberOfSupPerIFFB + 
												"\t" + numberOfSupPerTTFR + 
												"\t" + numberOfSupPerITFR);
			
			
			/*int i=0;
			for(double d=PERCENT_UTILITY_LOSS_MIN; d<PERCENT_UTILITY_LOSS_MAX; d+=PERCENT_UTILITY_LOSS_STEP, i++) {
				avgUtility[i] = (float) (totalSuccessfulUtility2 - totalUnsuccessfulUtility2*d) / episode.episodeInitList.size();
				sb1.append("\tutility" + decimalFormat.format(d).substring(2));
				sb2.append("\t" + decimalFormat.format(avgUtility[i]));
			}*/
			
			String episodeFooter = sb1.toString() + "\n" + sb2.toString();
			
			writeEpisodeData(episodeHeader, episodeOutcomes, episodeFooter);
			
			return 0;
		}
		
	    private void writeEpisodeData(String episodeHeader, String[] episodeOutcomes, String episodeFooter) {
	    	FileWriter fw;
	    	try {
	    		fw = new FileWriter(fileName, true);

	    		fw.write(episodeHeader + "\n");
	    		for(int i=0; i<episodeOutcomes.length; i++) {
	        		fw.write(episodeOutcomes[i] + "\n");
	    		}
	    		fw.write(episodeFooter + "\n");
	    		
	    		fw.close();
	    	} catch(IOException e) {
	    		e.printStackTrace();
	    	}
	    }

	}
	
	
	private class WaitForEpisodeEnd extends SimpleBehaviour {
		private static final long serialVersionUID = 434688142534008127L;
		
		private Episode_NegotiationParallelInits episode_NegotiationParallelInits;
		
		public WaitForEpisodeEnd(Agent agent, Episode_NegotiationParallelInits episode_NegotiationParallelInits) {
			super(agent);
			this.episode_NegotiationParallelInits = episode_NegotiationParallelInits;
		}
		
		public void action() {
		}
		
		public boolean done() {
			//System.out.println(" Waiting for Episode End.........., enactmentOutcomes: " + enactmentOutcomes.size() +", numberOfSuccessfulNegotiations: " + episode_NegotiationParallelInits.numberOfSuccessfulNegotiations);
			return enactmentOutcomes.size() == episode_NegotiationParallelInits.numberOfSuccessfulNegotiations;
		}
		
	}
	

	private class Episode_NegotiationParallelInits extends ParallelBehaviour {
		private static final long serialVersionUID = -4759347369154911802L;
		
		ArrayList<Episode_NegotiationInit> episode_NegotiationInitList;
		int numberOfSuccessfulNegotiations = 0;
		
		public Episode_NegotiationParallelInits(Agent agent, int episodeNumber, Vector<AID> buyerAgents) {
			super(agent, ParallelBehaviour.WHEN_ALL);
			
			episode_NegotiationInitList = new ArrayList<Episode_NegotiationInit>();
			
			for(int i=0; i<buyerAgents.size(); i++) {
				ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
				Episode_NegotiationInit episodeInit = new Episode_NegotiationInit(myAgent, msg, episodeNumber, buyerAgents.get(i));
				addSubBehaviour(episodeInit);
				episode_NegotiationInitList.add(episodeInit);
			}
		}
		
		public void onStart() {
			enactmentOutcomes.clear();
		}
		
		public int onEnd() {
			int n = 0;
			for(int i=0; i<episode_NegotiationInitList.size(); i++) {
				if(episode_NegotiationInitList.get(i).negotiationOutcome != null) {   // FIXME: assuming that an unsuccessful negotiation brings a null value
					n++;
				}
			}
			numberOfSuccessfulNegotiations = n;
			
			return 0;
		}

	}
	
	
	private class Episode_NegotiationInit extends SimpleAchieveREInitiator {

		private static final long serialVersionUID = -8010474940863823176L;
		
		private AID buyerAgent;
		private int episodeNumber;
		NegotiationOutcome negotiationOutcome;
		
		public Episode_NegotiationInit(Agent a, ACLMessage msg, int episodeNumber, AID buyerAgent) {
			super(a, msg);
			
			this.episodeNumber = episodeNumber;
			this.buyerAgent = buyerAgent;
		}
		
		protected ACLMessage prepareRequest(ACLMessage msg) {
			msg.setProtocol(FIPANames.InteractionProtocol.FIPA_REQUEST);
			msg.setLanguage(getSlCodec().getName());
			msg.setOntology(ElectronicInstitution.SYNCHRONIZATION_ONTOLOGY);
			msg.addReceiver(buyerAgent);

			StartEpisode sE = new StartEpisode(episodeNumber);
			Parameters parameters = new Parameters();
			parameters.add("contractType", CONTRACT_TYPE);
			parameters.add("negotiationRounds", String.valueOf(NEGOTIATION_ROUNDS));
			parameters.add("topNumberOfAgents", String.valueOf(TOP_N));
			parameters.add("useTrustInPreselection", String.valueOf(USE_TRUST_IN_PRESELECTION));			
			parameters.add("useContextualFitness", String.valueOf(USE_CONTEXTUAL));
			parameters.add("useTrustInContractDrafting", String.valueOf(USE_TRUST_IN_CONTRACT_DRAFTING));
			parameters.add("useTrustInProposalEvaluation", String.valueOf(USE_TRUST_IN_PROPOSAL_EVALUATION));
			sE.setParameters(parameters);

			Action a = new Action(buyerAgent, sE);
			try {
				myAgent.getContentManager().fillContent(msg, a);
			} catch(Codec.CodecException ce) {
				ce.printStackTrace();
			} catch(OntologyException oe) {
				oe.printStackTrace();
			}

			return msg;
		}

		protected void handleInform(ACLMessage inform) {
			try {
				negotiationOutcome = (NegotiationOutcome) inform.getContentObject();
			} catch (UnreadableException e) {
				e.printStackTrace();
			}
		}
		
		public int onEnd() {
			if(negotiationOutcome != null) {
				log(buyerAgent.getLocalName() + " got a contract");
			} else {
				log(buyerAgent.getLocalName() + " did not get a contract");
			}
			return 0;
		}

	}
	
	
	private class EnactmentOutcomeListening extends CyclicBehaviour {
		
		private static final long serialVersionUID = 2130794273613266404L;
		
		private MessageTemplate mt;
		
		EnactmentOutcomeListening(Agent agent, String ctr) {
			super(agent);
			
			mt = MessageTemplate.and(	MessageTemplate.and(
											MessageTemplate.MatchPerformative(ACLMessage.INFORM),
											MessageTemplate.MatchOntology("_"+ElectronicInstitution.SYNCHRONIZATION_ONTOLOGY)), // martelada!
										MessageTemplate.MatchSender(new AID(ctr, false)));
		}
		
		public void action() {
			ACLMessage msg = myAgent.receive(mt);
			
			if (msg != null) {
				try {
					//System.out.println("got EnactmentOutcome for " + ((EnactmentOutcome) msg.getContentObject()).getContractId());
					enactmentOutcomes.add((EnactmentOutcome) msg.getContentObject());
				} catch(UnreadableException ue) {
					ue.printStackTrace();
				}
			} else {
				block();
			}
		}
		
	} // end EnactmentOutcomeListening class

	
	private class SyncResetInit extends SimpleAchieveREInitiator {
		
		private static final long serialVersionUID = 2130794273613266404L;
		
		String ctr;
		
		SyncResetInit(Agent agent, String ctr) {
			super(agent, new ACLMessage(ACLMessage.REQUEST));
			this.ctr = ctr;
		}
		
		public ACLMessage prepareRequest(ACLMessage msg) {
			msg.setProtocol(FIPANames.InteractionProtocol.FIPA_REQUEST);
			msg.setOntology(ElectronicInstitution.SYNCHRONIZATION_ONTOLOGY);
			msg.addReceiver(new AID(ctr, false));
			msg.setContent("reset");
			
			return msg;
		}
		
	} // end SyncResetInit class


	protected boolean createGUI() {
		return false;
	}

}

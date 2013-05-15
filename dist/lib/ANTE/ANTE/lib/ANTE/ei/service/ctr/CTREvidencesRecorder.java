package ei.service.ctr;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import ei.onto.ctr.ObligationEvidence;
import ei.onto.normenv.report.Obligation;
import ei.service.ctr.OutcomeGenerator.MappingMethod;
import ei.service.ctr.OutcomeGenerator.Outcome;
import ei.service.ctr.context.Context;

public class CTREvidencesRecorder {
	
	private Hashtable<String, EAgentInfo> evidenceRecords;
	private String fileName = "";
	
	CTREvidencesRecorder(Hashtable<String, EAgentInfo> evidenceRecords, String fileName)
	{
		this.evidenceRecords = evidenceRecords;
		this.fileName = fileName;
	}

	public boolean save()
	{
		String header = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n";
		String body = "<CTR>\n";
		
		Enumeration<String> keys = evidenceRecords.keys();
		while(keys.hasMoreElements())
		{
			
			EAgentInfo agent = evidenceRecords.get(keys.nextElement());
			
			body += "\t<Agent>\n" +
					"\t\t<name>"+agent.getAgent()+"</name>\n" +
					"\t\t<nContracts>"+agent.getContractHistoric().size()+"</nContracts>\n" +
					"\t\t<nObligations>"+agent.getNObl()+"</nObligations>\n" +
					"\t\t<nFulObl>"+agent.getNFulfObl()+"</nFulObl>\n" +
					"\t\t<nViolObl>"+agent.getNViolObl()+"</nViolObl>\n" +
					"\t\t<nDLineViolObl>"+agent.getnDlineViolObl()+"</nDLineViolObl>\n" +
					"\t\t<evidences_info>\n";
			Vector<EvidenceInfo> evidences_info = agent.getReports();

			for(int i = 0; i < evidences_info.size(); i++)
			{
				body += "\t\t\t<evidence>\n" +
						"\t\t\t\t<contract_id>"+evidences_info.get(i).getContracID()+"</contract_id>\n" +
						"\t\t\t\t<updated_values>"+evidences_info.get(i).getTrustValue()+"</updated_values>\n" +
						"\t\t\t</evidence>\n";
			}
			body += "\t\t</evidences_info>\n" +
					"\t\t<contracts>\n";
			
			Vector<ContractEnactment> contracts = agent.getContractHistoric();
			
			for(int i = 0; i < contracts.size(); i++)
			{
				if(contracts.get(i).isEnded())
				{
				
					body += "\t\t\t<contract>\n" +
					"\t\t\t\t<contract_id>"+contracts.get(i).getName()+"</contract_id>\n" +
					"\t\t\t\t<agents>\n";

					Vector<String> agents = contracts.get(i).getAgents();
					for(String s: agents)
					{
						body += "\t\t\t\t\t<agent>"+s+"</agent>\n";
					}

					body += "\t\t\t\t</agents>\n" +
					"\t\t\t\t<owner>"+contracts.get(i).getOwner()+"</owner>\n" +
					"\t\t\t\t<outcome>"+contracts.get(i).getOutcome()+"</outcome>\n" +
					"\t\t\t\t<obligations>\n";

					Vector<ObligationEvidence> obligations = contracts.get(i).getObligations();
					for(ObligationEvidence Obl: obligations)
					{
						body += "\t\t\t\t\t<obligation>\n" +
						"\t\t\t\t\t\t<bearer>"+Obl.getBearer()+"</bearer>\n" +
						"\t\t\t\t\t\t<counterparty>"+Obl.getCounterparty()+"</counterparty>\n" +
						"\t\t\t\t\t\t<fact>"+Obl.getFact()+"</fact>\n" +
						"\t\t\t\t\t\t<liveline>"+Obl.getLiveline()+"</liveline>\n" +
						"\t\t\t\t\t\t<deadline>"+Obl.getDeadline()+"</deadline>\n" +
						"\t\t\t\t\t\t<fulfilled_when>"+Obl.getFulfilled()+"</fulfilled_when>\n" +
						"\t\t\t\t\t</obligation>\n";
					}

					body += "\t\t\t\t</obligations>\n" +
					"\t\t\t\t<contexts>\n";

					Vector<Context> contexts = contracts.get(i).getContexts();
					
					for(Context c : contexts)
					{
						body += "\t\t\t\t\t<context>\n" +
						"\t\t\t\t\t\t<product>"+c.getName()+"</product>\n" +
						"\t\t\t\t\t\t<quantity>"+c.getQuantity()+"</quantity>\n" +
						"\t\t\t\t\t\t<dtime>"+c.getDeliveryTime()+"</dtime>\n" +
						"\t\t\t\t\t\t<seller>"+c.getSeller()+"</seller>\n" +
						"\t\t\t\t\t\t<fine>"+c.getFine()+"</fine>\n" +
						"\t\t\t\t\t</context>\n";
					}

					body += "\t\t\t\t</contexts>\n" +
					"\t\t\t\t<fulfillments>\n";

					Vector<Integer> fulfillments = contracts.get(i).getFulfillments();

					for(Integer ind : fulfillments)
					{
						body += "\t\t\t\t\t<ind>"+ind.intValue()+"</ind>\n";
					}

					body += "\t\t\t\t</fulfillments>\n" +
					"\t\t\t\t<violations>\n";

					Vector<Integer> violations = contracts.get(i).getViolations();

					for(Integer ind : violations)
					{
						body += "\t\t\t\t\t<ind>"+ind.intValue()+"</ind>\n";
					}

					body += "\t\t\t\t</violations>\n" +
					"\t\t\t\t<dLineViols>\n";

					Vector<Integer> dLineViol = contracts.get(i).getdLineViolations();
					if (dLineViol != null) {
						for(Integer ind : dLineViol) {
							body += "\t\t\t\t\t<ind>"+ind.intValue()+"</ind>\n";
						}
					}
					body += "\t\t\t\t</dLineViols>\n" +
					"\t\t\t\t<state>\n";

					Vector<Outcome> states = contracts.get(i).getState();
					for(Outcome ind : states) {
						if (ind != null) {
							body += "\t\t\t\t\t<ind>"+ind.name()+"</ind>\n";
						}
					}
					body += "\t\t\t\t</state>\n" +
					"\t\t\t</contract>\n";
				}				
			}
			
			body += "\t\t</contracts>\n" +
					"\t</Agent>\n";
		}
		
		body += "</CTR>\n";
		
		writeToFile(header, body);
		
		return true;
	}
	
	private void writeToFile(String header, String body) {
		// TODO Auto-generated method stub
		FileWriter fstream;
		try{
			File f = new File("config/compTrust/");
			f.mkdir();
			fstream = new FileWriter("config/compTrust/"+fileName);
			
			BufferedWriter out = new BufferedWriter(fstream);
			out.write(header);
			out.write(body);
			out.close();
			
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

	public void setEvidenceRecords(Hashtable<String, EAgentInfo> evidenceRecords) {
		this.evidenceRecords = evidenceRecords;
	}

	public Hashtable<String, EAgentInfo> getEvidenceRecords() {
		return evidenceRecords;
	}

	
	public Hashtable<String, EAgentInfo> load(){
		
		Hashtable<String, EAgentInfo> evidences;
		File file = new File("config/compTrust/"+fileName);
		
		//check if parameter file exists before trying to load the account data
		if(file.exists()){
			
			evidences = new Hashtable<String, EAgentInfo>();
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			// create Document object
			NodeList agents;
			
			try {
				DocumentBuilder builder = factory.newDocumentBuilder();
				
				Document ctr_config = builder.parse(file);
				
				agents = ((Element) ctr_config.getElementsByTagName("CTR").item(0)).getElementsByTagName("Agent");
				
				// iterate through <agent> nodes
				for(int i=0; i<agents.getLength(); i++) {
					Node ag = agents.item(i);
					
					EAgentInfo eAI;
					
					String name = "";
					int nContracts, nObligations, nFulObl, nViolObl, nDlineViol;
					Vector<EvidenceInfo> evidencesInfo = null;
					Vector<ContractEnactment> contracts = null;
					
					name = ((Element) ag).getElementsByTagName("name").item(0).getFirstChild().getNodeValue();
					nContracts = Integer.valueOf(((Element) ag).getElementsByTagName("nContracts").item(0).getFirstChild().getNodeValue());
					nObligations = Integer.valueOf(((Element) ag).getElementsByTagName("nObligations").item(0).getFirstChild().getNodeValue());
					nFulObl = Integer.valueOf(((Element) ag).getElementsByTagName("nFulObl").item(0).getFirstChild().getNodeValue());
					nViolObl = Integer.valueOf(((Element) ag).getElementsByTagName("nViolObl").item(0).getFirstChild().getNodeValue());
					nDlineViol = Integer.valueOf(((Element) ag).getElementsByTagName("nDLineViolObl").item(0).getFirstChild().getNodeValue());
					
					NodeList evInf = ((Element) ((Element) ag).getElementsByTagName("evidences_info").item(0)).getElementsByTagName("evidence");
					
					evidencesInfo = new Vector<EvidenceInfo>();
					
					for(int j=0; j<evInf.getLength(); j++) {
						Node evid = evInf.item(j);
					
						Double updatedValue = Double.parseDouble(((Element) evid).getElementsByTagName("updated_values").item(0).getFirstChild().getNodeValue());
						String contracID = ((Element) evid).getElementsByTagName("contract_id").item(0).getFirstChild().getNodeValue();
						
						EvidenceInfo eI = new EvidenceInfo();
						eI.setContracID(contracID);
						eI.setTrustValue(updatedValue);
						evidencesInfo.add(eI);
					}
					
					
					NodeList contList = ((Element) ((Element) ag).getElementsByTagName("contracts").item(0)).getElementsByTagName("contract");
					
					contracts = new Vector<ContractEnactment>();
					
					for(int j=0; j<contList.getLength(); j++) {
						Node cont = contList.item(j);
					
						ContractEnactment cE = new ContractEnactment();
						
						NodeList agentsList = ((Element) ((Element) cont).getElementsByTagName("agents").item(0)).getElementsByTagName("agent");
						
						Vector<String> agentsVec = new Vector<String>();
						
						for(int k=0; k<agentsList.getLength(); k++) {
							Node agent = agentsList.item(k);
						
							String agName = agent.getFirstChild().getNodeValue();
							agentsVec.add(agName);
						}
						
						String owner = ((Element) cont).getElementsByTagName("owner").item(0).getFirstChild().getNodeValue(); 
						
						String contract_ID = ((Element) cont).getElementsByTagName("contract_id").item(0).getFirstChild().getNodeValue();
						
						String s = ((Element) cont).getElementsByTagName("outcome").item(0).getFirstChild().getNodeValue();
						Float outcome = null;
						
						if(!s.equalsIgnoreCase("null"))
							outcome = Float.parseFloat(s);
						
						NodeList obligations = ((Element) ((Element) cont).getElementsByTagName("obligations").item(0)).getElementsByTagName("obligation");
						
						Vector<ObligationEvidence> obligationsVec = new Vector<ObligationEvidence>();
						
						for(int k=0; k<obligations.getLength(); k++) {
							Node obl = obligations.item(k);
						
							Obligation oblig = new Obligation();
							
							String bearer = ((Element) obl).getElementsByTagName("bearer").item(0).getFirstChild().getNodeValue();
							
							String counterparty = ((Element) obl).getElementsByTagName("counterparty").item(0).getFirstChild().getNodeValue();
							
							String fact = ((Element) obl).getElementsByTagName("fact").item(0).getFirstChild().getNodeValue();
							
							Long liveline = Long.parseLong(((Element) obl).getElementsByTagName("liveline").item(0).getFirstChild().getNodeValue());
							
							Long deadline = Long.parseLong(((Element) obl).getElementsByTagName("deadline").item(0).getFirstChild().getNodeValue());
							
							Long fulfilled_when = Long.parseLong(((Element) obl).getElementsByTagName("fulfilled_when").item(0).getFirstChild().getNodeValue());
							
							oblig.setBearer(bearer);
							oblig.setCounterparty(counterparty);
							oblig.setDeadline(deadline);
							oblig.setFact(fact);
							oblig.setLiveline(liveline);
							
							ObligationEvidence oblEv = new ObligationEvidence(oblig);
							
							oblEv.setFulfilled(fulfilled_when);
							
							obligationsVec.add(oblEv);
						}
						
						NodeList contextList = ((Element) cont).getElementsByTagName("context");
						
						Vector<Context> products = new Vector<Context>();
						
						for(int k=0; k<contextList.getLength(); k++) {
							Node product = contextList.item(k);
						
							String nameProd = ((Element) product).getElementsByTagName("product").item(0).getFirstChild().getNodeValue();
							int quantity = Integer.parseInt(((Element) product).getElementsByTagName("quantity").item(0).getFirstChild().getNodeValue());
							Long deliveryTime = Long.parseLong(((Element) product).getElementsByTagName("dtime").item(0).getFirstChild().getNodeValue());
							String seller = ((Element) product).getElementsByTagName("seller").item(0).getFirstChild().getNodeValue();
							
							String f = ((Element) product).getElementsByTagName("fine").item(0).getFirstChild().getNodeValue();
							Double fine = null;
							if(!f.equalsIgnoreCase(f))
								fine = Double.parseDouble(f);
							
							Context c = new Context();
							c.setDeliveryTime(deliveryTime);
							c.setFine(fine);
							c.setName(nameProd);
							c.setQuantity(quantity);
							c.setSeller(seller);
							
							products.add(c);
							
							//System.out.println("--->"+deliveryTime+" - "+nameProd+" - "+quantity+" - "+fine+" - "+seller);
						}
						
						NodeList fulfillmentList = ((Element) ((Element) cont).getElementsByTagName("fulfillments").item(0)).getElementsByTagName("ind");
						
						Vector<Integer> fulfillments = new Vector<Integer>();
						
						for(int k=0; k<fulfillmentList.getLength(); k++) {
							Node node = fulfillmentList.item(k);
						
							Integer ind = Integer.parseInt(((Element) node).getFirstChild().getNodeValue());
							
							fulfillments.add(ind);
						}
						
						NodeList violationsList = ((Element) ((Element) cont).getElementsByTagName("violations").item(0)).getElementsByTagName("ind");
						
						Vector<Integer> violations = new Vector<Integer>();
						
						for(int k=0; k<violationsList.getLength(); k++) {
							Node node = violationsList.item(k);
						
							Integer ind = Integer.parseInt(((Element) node).getFirstChild().getNodeValue());
							
							violations.add(ind);
						}
						
						NodeList dLineViolationsList = ((Element) ((Element) cont).getElementsByTagName("dLineViols").item(0)).getElementsByTagName("ind");
						
						Vector<Integer> dLineviolations = new Vector<Integer>();
						
						for(int k=0; k<dLineViolationsList.getLength(); k++) {
							Node node = dLineViolationsList.item(k);
						
							Integer ind = Integer.parseInt(((Element) node).getFirstChild().getNodeValue());
							
							dLineviolations.add(ind);
						}
						
						NodeList stateList = ((Element) ((Element) cont).getElementsByTagName("state").item(0)).getElementsByTagName("ind");
						
						Vector<OutcomeGenerator.Outcome> state = new Vector<OutcomeGenerator.Outcome>();
						
						for(int k=0; k<stateList.getLength(); k++) {
							Node node = stateList.item(k);
						
							String ind = ((Element) node).getFirstChild().getNodeValue();
							
							state.add(Outcome.valueOf(ind));
						}
						
						cE.setAgents(agentsVec);
						cE.setOwner(owner);
						cE.setContractID(contract_ID);
						cE.setOutcome(outcome);
						cE.setObligations(obligationsVec);
						cE.setdLineViolations(dLineviolations);
						cE.setState(state);
						cE.setEnded(true);
						cE.setContexts(products);
						contracts.add(cE);
					}
					
					eAI = new EAgentInfo(name);
					eAI.setContractHistoric(contracts);
					eAI.setNContracts(nContracts);
					eAI.setnDlineViolObl(nDlineViol);
					eAI.setNFulfObl(nFulObl);
					eAI.setNObl(nObligations);
					eAI.setNViolObl(nViolObl);
					eAI.setReports(evidencesInfo);
					eAI.refresh(MappingMethod.values()[0]);
					evidences.put(name, eAI);
					
//					EAgentInfo eA = evidences.get(name);
//					System.out.println("-->"+eA.getAgent()+" - "+eA.getNContracts()+" - ");
				}
				
				return evidences;
				
			} catch(Exception e) {
				//javax.xml.parsers.ParserConfigurationException
				//org.xml.sax.SAXException
				//java.io.IOException ioe
				System.err.println("Error occurred while parsing the CTR evidences file");
				e.printStackTrace();
				return null;
			}
		}
		else
			System.out.println("CTR Evidences File does not exist!");
		
		return null;
	}
	
}
package ei.scripts;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Vector;

import ei.ElectronicInstitution;
import ei.service.negotiation.qfnegotiation.QFNegotiationParameters;


public class Main {

	private static final int NUMBER_OF_RUNS = 150;
	private static final int NUMBER_OF_BUYERS = 20;
	private static final int NUMBER_OF_SELLERS = 40;
	private static final String FOLDER = "config/experiments/textile/";
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		FileWriter fstream;
		try {
			
			for(int r = 0; r < NUMBER_OF_RUNS; r++) {
				
				try{
					File f = new File(FOLDER + "RUN_" + r);
					f.mkdir();
				} catch(Exception e) {
					e.printStackTrace();
				} 
				
				fstream = new FileWriter(FOLDER + "RUN_" + r + "/ei-config.xml");
				String s = getFirst();

				try{
					File f = new File(FOLDER + "RUN_" + r + "/owl");
					f.mkdir();
				} catch(Exception e) {
					e.printStackTrace();
				} 
				
				for(int i = 0; i < NUMBER_OF_SELLERS; i++)
				{
					Vector<Double> vec = getSellerPercents();
					if(i < 9)
						s = s + "<agent name=\"Supplier0"+(i+1)+"\">\n";
					else
						s = s + "<agent name=\"Supplier"+(i+1)+"\">\n";
					s = s + "<class>ei.agent.enterpriseagent.Supplier</class>\n"+
					"<argument>components=config\\experiments\\textile\\RUN_" + r + "\\owl\\Supplier"+(i+1)+".owl</argument>\n"+
					"<argument>contract_xsd=contracts\\contract.xsd</argument>\n"+
					"<argument>synchronizer=sync-agent</argument>\n"+
					"<argument>useQNegotiationStrategy=false</argument>\n"+					
					"<argument>maxEntries_negotiationMessages=1</argument>\n"+
					"<argument>maxEntries_contracts=1</argument>\n"+					
					"<argument>delivery_F_Percent="+vec.get(0)+"</argument>\n"+
					"<argument>delivery_Fd_Percent="+vec.get(1)+"</argument>\n"+
					"<argument>delivery_V_Percent="+vec.get(2)+"</argument>\n"+
					"<argument>penalty_F_Percent="+vec.get(3)+"</argument>\n"+
					"<argument>penalty_V_Percent="+vec.get(4)+"</argument>\n"+
					"<argument>risk_tolerance="+vec.get(5)+"</argument>\n"+
					"</agent>\n\n";
				}

				for(int i = 0; i < NUMBER_OF_BUYERS; i++)
				{
					Vector<Double> vec = getClientPercents();
					if(i < 9)
						s = s + "<agent name=\"Client0"+(i+1)+"\">\n";
					else
						s = s + "<agent name=\"Client"+(i+1)+"\">\n";

					s = s + "<class>ei.agent.enterpriseagent.AutomaticClient</class>\n"+
					"<argument>good=config\\experiments\\textile\\RUN_" + r + "\\owl\\Requester"+(i+1)+".owl</argument>\n"+			
					"<argument>contract_xsd=contracts\\contract.xsd</argument>\n"+
					"<argument>denounce_deadline_obligations=true</argument>\n"+
					"<argument>synchronizer=sync-agent</argument>\n"+	
					"<argument>maxEntries_contracts=1</argument>\n"+					
					"<argument>" + QFNegotiationParameters.MAPPING_METHOD + "="+vec.get(3).intValue()+"</argument>\n"+
					"<argument>" + QFNegotiationParameters.RISK_TOLERANCE + "="+vec.get(2)+"</argument>\n"+
					"<argument>time_to_denounce="+4000+"</argument>\n"+
					"</agent>\n\n";
				}

				s = s + getFinal();

				BufferedWriter out = new BufferedWriter(fstream);
				out.write(s);
				out.close();

			}

		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	private static Vector<Double> getSellerPercents() {
		//0 - delF
		//1 - delFd
		//2 - delV
		//3 - penF
		//4 - penV
		//5 - RT
		
		int aux = (int) (Math.random()*9);
		Vector<Double> vec = new Vector<Double>(6);
		switch(aux)
		{
		case 0:
			vec.add(0.85);
			vec.add(0.1);
			vec.add(0.05);
			vec.add(0.8);
			vec.add(0.2);
			vec.add(0.2);
			break;
		case 1:
			vec.add(0.85);
			vec.add(0.1);
			vec.add(0.05);
			vec.add(0.8);
			vec.add(0.2);
			vec.add(0.3);
			break;
		case 2:
			vec.add(0.85);
			vec.add(0.1);
			vec.add(0.05);
			vec.add(0.5);
			vec.add(0.5);
			vec.add(0.2);
			break;
		case 3:
			vec.add(0.75);
			vec.add(0.2);
			vec.add(0.05);
			vec.add(0.8);
			vec.add(0.2);
			vec.add(0.2);
			break;
		case 4:
			vec.add(0.75);
			vec.add(0.2);
			vec.add(0.05);
			vec.add(0.8);
			vec.add(0.2);
			vec.add(0.3);
			break;
		case 5:
			vec.add(0.75);
			vec.add(0.2);
			vec.add(0.05);
			vec.add(0.5);
			vec.add(0.5);
			vec.add(0.2);
			break;
		case 6:
			vec.add(0.5);
			vec.add(0.4);
			vec.add(0.1);
			vec.add(0.8);
			vec.add(0.2);
			vec.add(0.2);
			break;
		case 7:
			vec.add(0.5);
			vec.add(0.4);
			vec.add(0.1);
			vec.add(0.8);
			vec.add(0.2);
			vec.add(0.3);
			break;
		case 8:
			vec.add(0.5);
			vec.add(0.4);
			vec.add(0.1);
			vec.add(0.5);
			vec.add(0.5);
			vec.add(0.2);
			break;
		}
		return vec;
	}

	private static Vector<Double> getClientPercents() {
		int n = (int)(Math.random()*6);
		Vector<Double> vec = new Vector<Double>(3);
		
		switch(n)
		{
		case 0:
			vec.add(0.7);
			vec.add(1.0);
			vec.add(0.8);
			vec.add(1.0);
			break;
		case 1:
			vec.add(0.5);
			vec.add(0.2);
			vec.add(0.8);
			vec.add(1.0);
			break;
		case 2:
			vec.add(0.05);
			vec.add(0.7);
			vec.add(0.8);
			vec.add(1.0);
			break;
		case 3:
			vec.add(0.7);
			vec.add(1.0);
			vec.add(0.2);
			vec.add(3.0);
			break;
		case 4:
			vec.add(0.5);
			vec.add(0.2);
			vec.add(0.2);
			vec.add(2.0);
			break;
		case 5:
			vec.add(0.05);
			vec.add(0.7);
			vec.add(0.2);
			vec.add(2.0);
			break;
		}
	
		return vec;
	}

	private static String getFinal() {
		String s = 	"</institutional-agents>\n"+
					"</ei-config>\n";
		return s;
	}

	private static String getFirst() {
		String s = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"+
				   "<!DOCTYPE ei-config SYSTEM \"../../ei-config.dtd\">\n"+
				   "<ei-config>\n"+
				   "<jade-parameters>\n"+
				   "<parameter>local-port=2010</parameter>\n"+
				   "</jade-parameters>\n\n"+
			
			"<institutional-agents>\n"+
				"<agent name=\"rma\">\n"+
					"<class>jade.tools.rma.rma</class>\n"+
				"</agent>\n\n"+
				
				"<agent name=\"negmed\">\n"+
					"<class>ei.service.negotiation.qfnegotiation.QFNegotiationMediator</class>\n"+
					"<argument>service=" + ElectronicInstitution.SRV_NEGOTIATION_FACILITATOR + "</argument>\n"+
					"<argument>contract_xsd=contracts\\contract.xsd</argument>\n"+
					"<argument>maxEntries_needsNegotiation=1</argument>\n"+
					"<argument>synchronizer=sync-agent</argument>\n"+
				"</agent>\n\n"+		
			
				"<agent name=\"normenv\">\n"+
					"<class>ei.service.normenv.NormativeEnvironment</class>\n"+
					"<argument>service=" + ElectronicInstitution.SRV_NORMATIVE_ENVIRONMENT + "</argument>\n"+
					"<argument>jess_file=config\\normenv\\normenv.clp</argument>\n"+
					"<argument>logging=true</argument>\n"+
					"<argument>contract_xsd=contracts\\contract.xsd</argument>\n"+
					"<argument>synchronizer=sync-agent</argument>\n"+
				"</agent>\n\n"+
				
				"<agent name=\"ctr\">\n"+
					"<class>ei.service.ctr.CTR</class>\n"+
					"<argument>service=" + ElectronicInstitution.SRV_CTR + "</argument>\n"+
					"<argument>synchronizer=sync-agent</argument>"+
				"</agent>\n\n"+

				"<agent name=\"bank\">\n"+
					"<class>ei.agent.role.BankAgent</class>\n"+
					"<argument>role=" + ElectronicInstitution.ROLE_BANK + "</argument>"+
					"<argument>accounts_file=config\\bank\\accounts.xml</argument>\n"+
					"<argument>synchronizer=sync-agent</argument>\n"+
				"</agent>\n\n"+
				"<agent name=\"delivery-tracker\">\n"+
					"<class>ei.agent.role.DeliveryTrackerAgent</class>\n"+
					"<argument>role=" + ElectronicInstitution.ROLE_DELIVERY_TRACKER + "</argument>"+
					"<argument>synchronizer=sync-agent</argument>\n"+
				"</agent>\n\n"+
				"<agent name=\"messenger\">\n"+
					"<class>ei.agent.role.MessengerAgent</class>\n"+
					"<argument>role=" + ElectronicInstitution.ROLE_MESSENGER + "</argument>"+
					"<argument>synchronizer=sync-agent</argument>\n"+
				"</agent>\n\n\n"+
				
				"<!-- Enterprise Agents -->\n\n\n";
				
				return s;
	}

}

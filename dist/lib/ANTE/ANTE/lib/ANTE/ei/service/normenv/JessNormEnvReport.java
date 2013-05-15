package ei.service.normenv;

import ei.onto.normenv.report.*;

import jade.util.leap.ArrayList;

import jess.Context;
import jess.Deftemplate;
import jess.Fact;
import jess.Funcall;
import jess.JessException;
import jess.QueryResult;
import jess.Userfunction;
import jess.Value;
import jess.ValueVector;

/**
 * This class implements the Jess userfunction to deal with new facts in the normative environment which may have to be reported
 * to interested agents.
 * It can be used by Jess by using the name <code>norm-env-report</code>.
 * 
 * @author hlc
 */
public class JessNormEnvReport implements Userfunction {
	
	private NormativeEnvironment ne;
	
	public JessNormEnvReport(NormativeEnvironment ne) {
		this.ne = ne;
	}
	
	// Name by which the function appears in Jess
	public String getName() {
		return ("norm-env-report");
	}
	
	// Called when (norm-env-report ...) is encountered at Jess
	public Value call(ValueVector vv, Context jc) throws JessException {
		// Jess calls (norm-env-report ?fact)
		Fact f = vv.get(1).factValue(jc);
		Deftemplate dt = f.getDeftemplate();
		Deftemplate tdt = NormEnvEngine.findTopDeftemplate(dt);
		
		// create report
		Report rep = null;
		
		// check fact passed to norm-env-report
		if(tdt.getBaseName().equals("context")) {
			// report a new contract
			rep = new NewContract();
			rep.setContext(f.getSlotValue("id").symbolValue(jc));
			rep.setWhen(f.getSlotValue("when").longValue(jc));
			((NewContract) rep).setType(dt.getBaseName());
			((NewContract) rep).setSuperContext(f.getSlotValue("super-context").symbolValue(jc));
			// create agent list
			ValueVector agents = f.getSlotValue("who").listValue(jc);
			ArrayList agentList = new ArrayList();
			for(int i=0; i<agents.size(); i++)
				agentList.add(agents.get(i).symbolValue(jc));
			((NewContract) rep).setAgents(agentList);
			// create contractual-info list
			ArrayList contractualInfos = new ArrayList();
			try {
				// get contractual-infos for the contract
				QueryResult contextualInfos = ne.normEnvBeh.jess.runQueryStar("MAIN::get-contextual-infos", new ValueVector().add(f.getSlotValue("id").symbolValue(jc)));
				while (contextualInfos.next()) {
				    Fact cInfo = contextualInfos.get("contextual-info").factValue(jc);
					// create contractual-info frame
					NewContract.Frame contractualInfo = new NewContract.Frame(cInfo.getDeftemplate().getBaseName());
					// set the slot/value pairs for the contractual-info frame
					String[] slotNames = cInfo.getDeftemplate().getSlotNames();
					for(int i=0; i<slotNames.length; i++) {
						contractualInfo.addSlot(new NewContract.Frame.Slot(slotNames[i], cInfo.getSlotValue(slotNames[i]).symbolValue(jc)));
					}
					contractualInfos.add(contractualInfo);
				}
			} catch(JessException je) {
				je.printStackTrace();
			}
			((NewContract) rep).setContractualInfos(contractualInfos);
			
		} else if(dt.getBaseName().equals("start-context")) {
			// report a contract start
			rep = new ContractStart();
			rep.setContext(f.getSlotValue("context").symbolValue(jc));
			((ContractStart) rep).setWhen(f.getSlotValue("when").longValue(jc));
			
		} else if(dt.getBaseName().equals("end-context")) {
			// report a contract end
			rep = new ContractEnd();
			rep.setContext(f.getSlotValue("context").symbolValue(jc));
			((ContractEnd) rep).setWhen(f.getSlotValue("when").longValue(jc));
			
		} else if(dt.getBaseName().equals("ifact")) {
			// report a new ifact
			rep = makeIFactFromFact(f, jc);
			
		} else if(dt.getBaseName().equals("obligation")) {
			// report a new obligation
			rep = makeObligationFromFact(f, jc);
			
		} else if(dt.getBaseName().equals("liveline-violation")) {
			// report a liveline violation
			
			// prepare the referred obligation object
			Fact oblFact = f.getSlotValue("obl").factValue(jc);
			Obligation obl = makeObligationFromFact(oblFact, jc);
			
			// prepare the referred ifact object
			Fact ifaFact = f.getSlotValue("ifa").factValue(jc);
			IFact ifa = makeIFactFromFact(ifaFact, jc);

			// create the LivelineViolation report
			rep = new LivelineViolation(f.getSlotValue("context").symbolValue(jc), f.getSlotValue("when").longValue(jc), obl, ifa);
			
		} else if(dt.getBaseName().equals("deadline-violation")) {
			// report a deadline violation

			// prepare the referred obligation object
			Fact oblFact = f.getSlotValue("obl").factValue(jc);
			Obligation obl = makeObligationFromFact(oblFact, jc);
			
			// create the DeadlineViolation report
			rep = new DeadlineViolation(f.getSlotValue("context").symbolValue(jc), f.getSlotValue("when").longValue(jc), obl);
			
		} else if(dt.getBaseName().equals("fulfillment")) {
			// report a fulfillment
			
			// prepare the referred obligation object
			Fact oblFact = f.getSlotValue("obl").factValue(jc);
			Obligation obl = makeObligationFromFact(oblFact, jc);
			
			// prepare the referred ifact object
			Fact ifaFact = f.getSlotValue("ifa").factValue(jc);
			IFact ifa = makeIFactFromFact(ifaFact, jc);

			// create the Fulfillment report
			rep = new Fulfillment(f.getSlotValue("context").symbolValue(jc), f.getSlotValue("when").longValue(jc), obl, ifa);
			
		} else if(dt.getBaseName().equals("violation")) {
			// report a violation

			// prepare the referred obligation object
			Fact oblFact = f.getSlotValue("obl").factValue(jc);
			Obligation obl = makeObligationFromFact(oblFact, jc);
			
			// create the Violation report
			rep = new Violation(f.getSlotValue("context").symbolValue(jc), f.getSlotValue("when").longValue(jc), obl);
			
		} else if(dt.getBaseName().equals("denounce")) {
			// report a denounce

			// prepare the referred obligation object
			Fact oblFact = f.getSlotValue("obl").factValue(jc);
			Obligation obl = makeObligationFromFact(oblFact, jc);
			
			// create the Denounce report
			rep = new Denounce(f.getSlotValue("context").symbolValue(jc), f.getSlotValue("when").longValue(jc), obl);
			
		} // TODO: time - should these be reported?
		
		if(rep != null) {
			ne.newReport(rep);
		}
		
		return Funcall.TRUE;
	}

	/**
	 * This is not implemented as an IFact constructor because if so the IFact class, which is an ontology class, would have to mess with Jess stuff.
	 * 
	 * @param ifaFact
	 * @param jc
	 * @return
	 * @throws JessException
	 */
	private IFact makeIFactFromFact(Fact ifaFact, Context jc) throws JessException {
		IFact ifa = new IFact();
		ifa.setContext(ifaFact.getSlotValue("context").symbolValue(jc));
		ifa.setWhen(ifaFact.getSlotValue("when").longValue(jc));
		ifa.setFact(multiSlot2String(ifaFact, "fact", jc));
		return ifa;
	}

	/**
	 * This is not implemented as an Obligation constructor because if so the Obligation class, which is an ontology class, would have to mess with Jess stuff.
	 * 
	 * @param oblFact
	 * @param jc
	 * @return
	 * @throws JessException
	 */
	private Obligation makeObligationFromFact(Fact oblFact, Context jc) throws JessException {
		Obligation obl = new Obligation();
		obl.setId(oblFact.getSlotValue("id").symbolValue(jc));
		obl.setContext(oblFact.getSlotValue("context").symbolValue(jc));
		obl.setWhen(oblFact.getSlotValue("when").longValue(jc));
		obl.setBearer(oblFact.getSlotValue("bearer").symbolValue(jc));
		obl.setCounterparty(oblFact.getSlotValue("counterparty").symbolValue(jc));
		obl.setFact(multiSlot2String(oblFact, "fact", jc));
		obl.setLiveline(oblFact.getSlotValue("liveline").longValue(jc));
		obl.setDeadline(oblFact.getSlotValue("deadline").longValue(jc));
		obl.setFine(oblFact.getSlotValue("fine").floatValue(jc));
		return obl;
	}
	
	/**
	 * An auxiliary method to create a String from a multislot.
	 */
	private String multiSlot2String(Fact f, String slot, Context jc) throws JessException {
		ValueVector factSlot = f.getSlotValue(slot).listValue(jc);
		StringBuffer slotStr = new StringBuffer();
		for(int i=0; i<factSlot.size(); i++) {
			slotStr.append(factSlot.get(i).stringValue(jc));
			slotStr.append(" ");
		}
		return slotStr.toString();
	}
	
}

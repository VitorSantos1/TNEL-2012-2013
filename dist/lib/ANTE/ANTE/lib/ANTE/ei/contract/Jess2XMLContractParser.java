package ei.contract;

import ei.contract.xml.*;
import ei.service.normenv.NormEnvEngine;

import java.io.StringWriter;
import java.util.Iterator;

import jess.ConditionalElement;
import jess.Context;
import jess.Defrule;
import jess.Deftemplate;
import jess.Fact;
import jess.Funcall;
import jess.Group;
import jess.JessException;
import jess.Pattern;
import jess.ValueVector;
import jess.xml.XMLVisitor;

/**
 * A parser for converting Jess code to parts of an XMLContract.
 * <p>
 * WORK IN PROGRESS.
 * This class is tightly coupled with the EI Jess definitions. Any changes to that will likely require changes here... FIXME
 * 
 * @author hlc
 */
public class Jess2XMLContractParser {
	
	/**
	 * Converts a norm in Jess (a Defrule) into XML.
	 * 
	 * TODO: avoid using the unstable API. Should parse JessML instead!
	 * 
	 * @param jess_norm	The norm in Jess
	 * @return			The XML norm
	 */
	public static String toXMLNorm(Defrule jess_norm) {
		XMLVisitor visitor = new XMLVisitor(jess_norm);
		System.out.println(visitor.toString());   // this gives me a String, not a JAXB object that I can use to browse the JessML
		// 2 options: create a JAXB object from this String, or implement another visitor that creates the appropriate JAXB objects
		// in any case, I must generate the JAXB classes from JessML.xsd
		
		// TODO
		
		return "";
	}
	
	/**
	 * Converts a norm in Jess (a Defrule) into XML.
	 * 
	 * FIXME: using an unstable API (namely the jess.Pattern class)... Should parse JessML instead!
	 * 
	 * @param jess_norm	The norm in Jess
	 * @param jc		The Jess context
	 * @return			The XML norm
	 */
	public static String toXMLNorm(Defrule jess_norm, Context jc) {
		ObjectFactory objFactory = new ObjectFactory();

		NormType norm = objFactory.createNormType();
		
		// name
		norm.setName(jess_norm.getName());
		
		// situation (assuming implicit ANDed LHS)
		NormType.Situation situation = objFactory.createNormTypeSituation();
		ConditionalElement ce = jess_norm.getConditionalElements();
		if(ce.isGroup() && ce.getName().equals(Group.AND)) {
			for(int i=0; i<((Group) ce).getGroupSize(); i++) {
				ConditionalElement cei = ((Group) ce).getConditionalElement(i);
				if(!cei.isGroup()) {   // ignore not's
					Deftemplate pt = ((Pattern) cei).getDeftemplate();
					// check the pattern's template
					// contractual-info
					if(NormEnvEngine.findTopDeftemplate(pt).getBaseName().equals("contextual-info")) {
						ContractualInfoType contractualInfo = objFactory.createContractualInfoType();
						contractualInfo.setName(pt.getBaseName());
						for(int j=0; j<((Pattern) cei).getNSlots(); j++) {
							try {
								Iterator it = ((Pattern) cei).getTests(j);
								while(it.hasNext()) {
									jess.Test1 t = (jess.Test1) it.next();
									if(!t.getSlotName().equals("context")) {   // omit context slot
										ContractualInfoType.Slot slot = objFactory.createContractualInfoTypeSlot();
										slot.setName(t.getSlotName());
										VarAllowedType value = objFactory.createVarAllowedType();
										value.setContent(t.getValue().toString());
//										value.setType(...);
										slot.setValue(value);
										contractualInfo.getSlot().add(slot);
									}
								}
							} catch (JessException je) {
								je.printStackTrace();
							}
						}
						situation.getSituationElement().add(contractualInfo);
					} else
					// ifact
					if(pt.getBaseName().equals("ifact")) {
						IfactType ifact = objFactory.createIfactType();
						int factSlotIndex = pt.getSlotIndex("fact");
						try {
							if(((Pattern) cei).getNTests(factSlotIndex) > 0) {
								FrameType fra = objFactory.createFrameType();
								Iterator it = ((Pattern) cei).getTests(factSlotIndex);   // multislot
								StringBuffer sb = new StringBuffer();
								while(it.hasNext()) {
									sb.append(" ");
									sb.append(((jess.Test1) it.next()).getValue().toString());
								}
								fra.setName(sb.toString());
//								FrameType.Slot slo = objFactory.createFrameTypeSlot();
//								slo.setName("---");
//								FrameType.Slot.Value val = objFactory.createFrameTypeSlotValue();
//								val.setContent();
//								val.setType(...);
//								slo.setValue(val);
//								fra.getSlot().add(slo);
								ifact.setFact(fra);
							}
						} catch (JessException je) {
							je.printStackTrace();
						}
						int whenSlotIndex = pt.getSlotIndex("when");
						try {
							if(((Pattern) cei).getNTests(whenSlotIndex) > 0) {
								VarAllowedType when = objFactory.createVarAllowedType();
								when.setContent(((jess.Test1) ((Pattern) cei).getTests(whenSlotIndex).next()).getValue().toString());
								ifact.setWhen(when);
							}
						} catch (JessException je) {
							je.printStackTrace();
						}
						situation.getSituationElement().add(ifact);
					} else
						// time
						if(pt.getBaseName().equals("time")) {
							TimeType time = objFactory.createTimeType();
							int whenSlotIndex = pt.getSlotIndex("when");
							try {
								if(((Pattern) cei).getNTests(whenSlotIndex) > 0) {
									VarAllowedType when = objFactory.createVarAllowedType();
									when.setContent(((jess.Test1) ((Pattern) cei).getTests(whenSlotIndex).next()).getValue().toString());
									time.setWhen(when);
								}
							} catch (JessException je) {
								je.printStackTrace();
							}
							situation.getSituationElement().add(time);
					} else
					// liveline-violation
					if(pt.getBaseName().equals("liveline-violation")) {
						LivelineViolationType lvio = objFactory.createLivelineViolationType();
						try {
							int agentSlotIndex = pt.getSlotIndex("bearer");
							if(((Pattern) cei).getNTests(agentSlotIndex) > 0) {
								VarAllowedType agent = objFactory.createVarAllowedType();
								agent.setContent(((jess.Test1) ((Pattern) cei).getTests(agentSlotIndex).next()).getValue().toString());
								lvio.setBearer(agent);
							}
							agentSlotIndex = pt.getSlotIndex("counterparty");
							if(((Pattern) cei).getNTests(agentSlotIndex) > 0) {
								VarAllowedType agent = objFactory.createVarAllowedType();
								agent.setContent(((jess.Test1) ((Pattern) cei).getTests(agentSlotIndex).next()).getValue().toString());
								lvio.setCounterparty(agent);
							}
						} catch (JessException je) {
							je.printStackTrace();
						}
						int factSlotIndex = pt.getSlotIndex("fact");
						try {
							if(((Pattern) cei).getNTests(factSlotIndex) > 0) {
								FrameType fra = objFactory.createFrameType();
								Iterator it = ((Pattern) cei).getTests(factSlotIndex);   // multislot
								StringBuffer sb = new StringBuffer();
								while(it.hasNext()) {
									sb.append(" ");
									sb.append(((jess.Test1) it.next()).getValue().toString());
								}
								fra.setName(sb.toString());
								lvio.setFact(fra);
							}
						} catch (JessException je) {
							je.printStackTrace();
						}
						int whenSlotIndex = pt.getSlotIndex("when");
						try {
							if(((Pattern) cei).getNTests(whenSlotIndex) > 0) {
								VarAllowedType when = objFactory.createVarAllowedType();
								when.setContent(((jess.Test1) ((Pattern) cei).getTests(whenSlotIndex).next()).getValue().toString());
								lvio.setWhen(when);
							}
						} catch (JessException je) {
							je.printStackTrace();
						}
						situation.getSituationElement().add(lvio);
					} else
					// deadline-violation
					if(pt.getBaseName().equals("deadline-violation")) {
						DeadlineViolationType dvio = objFactory.createDeadlineViolationType();
						try {
							int agentSlotIndex = pt.getSlotIndex("bearer");
							if(((Pattern) cei).getNTests(agentSlotIndex) > 0) {
								VarAllowedType agent = objFactory.createVarAllowedType();
								agent.setContent(((jess.Test1) ((Pattern) cei).getTests(agentSlotIndex).next()).getValue().toString());
								dvio.setBearer(agent);
							}
							agentSlotIndex = pt.getSlotIndex("counterparty");
							if(((Pattern) cei).getNTests(agentSlotIndex) > 0) {
								VarAllowedType agent = objFactory.createVarAllowedType();
								agent.setContent(((jess.Test1) ((Pattern) cei).getTests(agentSlotIndex).next()).getValue().toString());
								dvio.setCounterparty(agent);
							}
						} catch (JessException je) {
							je.printStackTrace();
						}
						int factSlotIndex = pt.getSlotIndex("fact");
						try {
							if(((Pattern) cei).getNTests(factSlotIndex) > 0) {
								FrameType fra = objFactory.createFrameType();
								Iterator it = ((Pattern) cei).getTests(factSlotIndex);   // multislot
								StringBuffer sb = new StringBuffer();
								while(it.hasNext()) {
									sb.append(" ");
									sb.append(((jess.Test1) it.next()).getValue().toString());
								}
								fra.setName(sb.toString());
								dvio.setFact(fra);
							}
						} catch (JessException je) {
							je.printStackTrace();
						}
						int whenSlotIndex = pt.getSlotIndex("when");
						try {
							if(((Pattern) cei).getNTests(whenSlotIndex) > 0) {
								VarAllowedType when = objFactory.createVarAllowedType();
								when.setContent(((jess.Test1) ((Pattern) cei).getTests(whenSlotIndex).next()).getValue().toString());
								dvio.setWhen(when);
							}
						} catch (JessException je) {
							je.printStackTrace();
						}
						situation.getSituationElement().add(dvio);
					} else
					// fulfillment
					if(pt.getBaseName().equals("fulfillment")) {
						FulfillmentType ful = objFactory.createFulfillmentType();
						try {
							int agentSlotIndex = pt.getSlotIndex("bearer");
							if(((Pattern) cei).getNTests(agentSlotIndex) > 0) {
								VarAllowedType agent = objFactory.createVarAllowedType();
								agent.setContent(((jess.Test1) ((Pattern) cei).getTests(agentSlotIndex).next()).getValue().toString());
								ful.setBearer(agent);
							}
							agentSlotIndex = pt.getSlotIndex("counterparty");
							if(((Pattern) cei).getNTests(agentSlotIndex) > 0) {
								VarAllowedType agent = objFactory.createVarAllowedType();
								agent.setContent(((jess.Test1) ((Pattern) cei).getTests(agentSlotIndex).next()).getValue().toString());
								ful.setCounterparty(agent);
							}
						} catch (JessException je) {
							je.printStackTrace();
						}
						int factSlotIndex = pt.getSlotIndex("fact");
						try {
							if(((Pattern) cei).getNTests(factSlotIndex) > 0) {
								FrameType fra = objFactory.createFrameType();
								Iterator it = ((Pattern) cei).getTests(factSlotIndex);   // multislot
								StringBuffer sb = new StringBuffer();
								while(it.hasNext()) {
									sb.append(" ");
									sb.append(((jess.Test1) it.next()).getValue().toString());
								}
								fra.setName(sb.toString());
								ful.setFact(fra);
							}
						} catch (JessException je) {
							je.printStackTrace();
						}
						int whenSlotIndex = pt.getSlotIndex("when");
						try {
							if(((Pattern) cei).getNTests(whenSlotIndex) > 0) {
								VarAllowedType when = objFactory.createVarAllowedType();
								when.setContent(((jess.Test1) ((Pattern) cei).getTests(whenSlotIndex).next()).getValue().toString());
								ful.setWhen(when);
							}
						} catch (JessException je) {
							je.printStackTrace();
						}
						situation.getSituationElement().add(ful);
					} else
					// violation
					if(pt.getBaseName().equals("violation")) {
						ViolationType vio = objFactory.createViolationType();
						try {
							int agentSlotIndex = pt.getSlotIndex("bearer");
							if(((Pattern) cei).getNTests(agentSlotIndex) > 0) {
								VarAllowedType agent = objFactory.createVarAllowedType();
								agent.setContent(((jess.Test1) ((Pattern) cei).getTests(agentSlotIndex).next()).getValue().toString());
								vio.setBearer(agent);
							}
							agentSlotIndex = pt.getSlotIndex("counterparty");
							if(((Pattern) cei).getNTests(agentSlotIndex) > 0) {
								VarAllowedType agent = objFactory.createVarAllowedType();
								agent.setContent(((jess.Test1) ((Pattern) cei).getTests(agentSlotIndex).next()).getValue().toString());
								vio.setCounterparty(agent);
							}
						} catch (JessException je) {
							je.printStackTrace();
						}
						int factSlotIndex = pt.getSlotIndex("fact");
						try {
							if(((Pattern) cei).getNTests(factSlotIndex) > 0) {
								FrameType fra = objFactory.createFrameType();
								Iterator it = ((Pattern) cei).getTests(factSlotIndex);   // multislot
								StringBuffer sb = new StringBuffer();
								while(it.hasNext()) {
									sb.append(" ");
									sb.append(((jess.Test1) it.next()).getValue().toString());
								}
								fra.setName(sb.toString());
								vio.setFact(fra);
							}
						} catch (JessException je) {
							je.printStackTrace();
						}
						int whenSlotIndex = pt.getSlotIndex("when");
						try {
							if(((Pattern) cei).getNTests(whenSlotIndex) > 0) {
								VarAllowedType when = objFactory.createVarAllowedType();
								when.setContent(((jess.Test1) ((Pattern) cei).getTests(whenSlotIndex).next()).getValue().toString());
								vio.setWhen(when);
							}
						} catch (JessException je) {
							je.printStackTrace();
						}
						situation.getSituationElement().add(vio);
//					} else
//					// rel-condition
//					if() {
					}
				}
			}
		}
		norm.setSituation(situation);
		
		// prescription
		NormType.Prescription prescription = objFactory.createNormTypePrescription();
		for(int i=0; i<jess_norm.getNActions(); i++) {
			Funcall f = jess_norm.getAction(i);
			try {
				if(f.getName().equals("assert")) {
					Fact fact = f.get(1).factValue(jc);
					if(fact.getName().equals("MAIN::obligation")) {
						ObligationType obligation = objFactory.createObligationType();
						VarAllowedType agent = objFactory.createVarAllowedType();
						agent.setContent(fact.getSlotValue("bearer").toString());
						obligation.setBearer(agent);
						agent = objFactory.createVarAllowedType();
						agent.setContent(fact.getSlotValue("counterparty").toString());
						obligation.setCounterparty(agent);
						FrameType frame = objFactory.createFrameType();
						ValueVector vv = fact.getSlotValue("fact").listValue(jc);   // multislot
						StringBuffer sb = new StringBuffer();
						for(int j=0; j<vv.size(); j++) {
							sb.append(" ");
							sb.append(vv.get(j));
						}
						frame.setName(sb.toString());
						obligation.setFact(frame);
						ExpressionType expr = objFactory.createExpressionType();
						VarAllowedType operand = objFactory.createVarAllowedType();
						operand.setContent(fact.getSlotValue("liveline").toString());
						expr.setOperand(operand);
						obligation.setLiveline(expr);
						expr = objFactory.createExpressionType();
						operand = objFactory.createVarAllowedType();
						operand.setContent(fact.getSlotValue("deadline").toString());
						expr.setOperand(operand);
						obligation.setDeadline(expr);
						prescription.getObligation().add(obligation);
					}
				}
			} catch (JessException je) {
				je.printStackTrace();
			}
		}
		norm.setPrescription(prescription);
		
		// marshal the norm
		try {
			javax.xml.bind.JAXBContext jaxbContext = javax.xml.bind.JAXBContext.newInstance("ei.contract.xml");
			javax.xml.bind.Marshaller marshaller = jaxbContext.createMarshaller();
			marshaller.setProperty(javax.xml.bind.Marshaller.JAXB_FRAGMENT, new Boolean(true));
			marshaller.setProperty(javax.xml.bind.Marshaller.JAXB_FORMATTED_OUTPUT, new Boolean(true));
			StringWriter sw = new StringWriter();
			marshaller.marshal(new javax.xml.bind.JAXBElement<NormType>(new javax.xml.namespace.QName(""), (Class<NormType>) norm.getClass(), norm), sw);
			return sw.toString();
		} catch(javax.xml.bind.JAXBException jaxbe) {
			jaxbe.printStackTrace();
			return null;
		}
	}
	
}

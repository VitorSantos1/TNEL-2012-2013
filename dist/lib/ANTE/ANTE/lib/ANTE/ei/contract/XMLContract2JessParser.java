package ei.contract;

import ei.contract.xml.*;

import java.util.ArrayList;
import java.util.List;

/**
 * A parser for converting the different parts of an XML Contract to Jess code.
 * <br>
 * Note that this class is tightly coupled with the XML contract XSD. Any changes to the XSD imply changes in the JAXB generated
 * classes, which will likely require changes here...
 * <br>
 * Also, this class is tightly coupled with the EI Jess definitions. Any changes to that will likely require changes here...
 * <br>
 * --> TODO: This class needs extensive testing!
 * 
 * @author hlc
 */
public class XMLContract2JessParser {

	private Contract contract;

	/**
	 * Constructor for an XMLContract2JessParser object.
	 * 
	 * @param contract	The XMLContract to parse.
	 */
	public XMLContract2JessParser(Contract contract) {
		this.contract = contract;
	}

	/**
	 * Module definition for the contract passed to the constructor, in Jess.
	 */
	public String moduleCode() {
		return "(defmodule " + contract.getHeader().getId() + " (declare (auto-focus TRUE)))";
	}

	/**
	 * Context for the contract passed to the constructor, in Jess.
	 */
	public String contextCode() {
		StringBuffer sb = new StringBuffer();
		String aux;
		aux = contract.getHeader().getType();
		if(aux != null) sb.append("(" + aux + " ");
		else sb.append("(context ");
		aux = contract.getHeader().getSuper();
		if(aux != null) sb.append("(super-context " + aux + ") ");
		sb.append("(id " + contract.getHeader().getId() + ") ");
		sb.append("(when " + contract.getHeader().getWhen().toGregorianCalendar().getTime().getTime() + ") ");
		List<String> ags = contract.getHeader().getWho().getAgent();
		sb.append("(who");
		for(int i=0; i<ags.size(); i++) {
			sb.append(" " + ags.get(i));
		}
		sb.append("))");
		return sb.toString();
	}

	/**
	 * Contractual-info in Jess. The contract passed to the constructor will be used as a context.
	 * Note that this method is applicable only to contractual-infos that are part of the contract's header.
	 * 
	 * @param cinfo	The contractual-info to represent in Jess.
	 */
	public String contractualInfoCode(Contract.Header.ContractualInfo cinfo) {
		StringBuffer sb = new StringBuffer();
		sb.append("(" + cinfo.getName() + " ");
		sb.append("(context " + contract.getHeader().getId() + ")");
		List<Contract.Header.ContractualInfo.Slot> slots = cinfo.getSlot();
		for(int i=0; i<slots.size(); i++) {
			if(!slots.get(i).getValue().isEmpty()) {// != null && slots.get(i).getValue() != "") {
				sb.append(" (" + slots.get(i).getName() + " " + slots.get(i).getValue() + ")");
			}
		}
		sb.append(")");
		return sb.toString();
	}

	/**
	 * Contractual-info template definition in Jess.
	 * FIXME: since all deftemplates are in MAIN, care should be taken regarding name collision
	 * 
	 * @param cinfo	The contractual-info to define a template in Jess.
	 */
	public String contractualInfoTemplateCode(Contract.Header.ContractualInfo cinfo) {
		StringBuffer sbt = new StringBuffer();
		sbt.append("(deftemplate MAIN::" + cinfo.getName() + " extends contextual-info ");
		List<Contract.Header.ContractualInfo.Slot> slots = cinfo.getSlot();
		for(int i=0; i<slots.size(); i++) {
			sbt.append(" (slot " + slots.get(i).getName() + ")");
		}
		sbt.append(")");
		return sbt.toString();
	}

	/**
	 * Contractual-info in Jess.
	 * 
	 * @param cinfo	The contractual-info to represent in Jess.
	 */
	public String contractualInfoCode(ContractualInfoType cinfo, String context_id) {
		StringBuffer sb = new StringBuffer();
		sb.append("(" + cinfo.getName() + " ");
		sb.append("(context " + context_id + ")");
		List<ContractualInfoType.Slot> slots = cinfo.getSlot();
		for(int i=0; i<slots.size(); i++) {
			sb.append(" (" + slots.get(i).getName() + " " + slots.get(i).getValue().getContent() + ")");
		}
		sb.append(")");
		return sb.toString();
	}
	
	/**
	 * Contractual-info template definition in Jess.
	 * FIXME: since all deftemplates are in MAIN, care should be taken regarding name collision
	 * 
	 * @param cinfo	The contractual-info to define a template in Jess.
	 */
	public String contractualInfoTemplateCode(ContractualInfoType cinfo) {
		StringBuffer sbt = new StringBuffer();
		sbt.append("(deftemplate MAIN::" + cinfo.getName() + " extends contextual-info ");
		List<ContractualInfoType.Slot> slots = cinfo.getSlot();
		for(int i=0; i<slots.size(); i++) {
			sbt.append(" (slot " + slots.get(i).getName() + ")");
		}
		sbt.append(")");
		return sbt.toString();
	}

	/**
	 * Rule definition, in Jess. The contract passed to the constructor will be used as a module.
	 * 
	 * @param rule	The rule to define in Jess.
	 */
	public String ruleCode(RuleType rule) {
		StringBuffer sb = new StringBuffer();
		sb.append("(defrule " + contract.getHeader().getId() + "::" + rule.getName());
		if(rule.getComment() != null) {
			sb.append("\n\t\"" + rule.getComment() + "\"");
		}
		List<IfactType> conds = rule.getIf().getIfact();
		for(int j=0; j<conds.size(); j++) {
			sb.append("\n\t" + ifactCode(conds.get(j), contract.getHeader().getId()));
		}
		sb.append("\n\t=>");
		sb.append("\n\t(assert ");
		sb.append(ifactCode(rule.getThen().getIfact(), contract.getHeader().getId()));
		sb.append(") )");
		return sb.toString();
	}

	/**
	 * Norm definition, in Jess. The contract passed to the constructor will be used as a module.
	 * <br>
	 * Note that for the norm inheritance mechanism the normative state portion used consists only of those IREs that are (implicitly
	 * ANDed) at the root of the norm's situation (LHS); note that if the LHS does not contain any (non-negated) IRE, then the
	 * normative state portion will be void.
	 * <p>
	 * (See also The documentation on the general structure of a norm in <code>normenv.clp</code>)
	 * 
	 * @param norm					The norm to define in Jess.
	 * @param contractualInfoSits	This is an out parameter, which obtains the situation elements which are contractual-info.
	 */
	public String normCode(NormType norm, ArrayList<ContractualInfoType> contractualInfoSits) {
		StringBuffer sb = new StringBuffer();
		sb.append("(defrule " + contract.getHeader().getId() + "::" + norm.getName());
		if(norm.getComment() != null) {
			sb.append("\n\t\"" + norm.getComment() + "\"");
		}

		// LHS of norm
		
		// norm scope
		String scope_id;
		if(norm.getScope() == null) {
			// no scope defined: assume contract scope
			scope_id = contract.getHeader().getId();
			String aux;
			aux = contract.getHeader().getType();
			if(aux != null) sb.append("\n\t(" + aux + " ");
			else sb.append("\n\t(context ");
		} else {
			// scope defined
			scope_id = norm.getScope().getId().getContent();
			if(norm.getScope().getContractType() == null)
				sb.append("\n\t(context ");
			else
				sb.append("\n\t(" + norm.getScope().getContractType() + " ");
		}
		sb.append("(id " + scope_id + "))");
		
		// analyze situation
		ArrayList<String> ireSits = new ArrayList<String>();   // the strings for IREs at the root
		StringBuffer nonIreSits = new StringBuffer();   // the strings for non IREs at the root
		// get situation elements
		List<SituationElementType> situationElements = norm.getSituation().getSituationElement();
		SituationElementType situationElement;
		// distinguish between IREs in the root and other situation elements
		for(int i=0; i<situationElements.size(); i++) {
			situationElement = situationElements.get(i);
			// check if the situation element is an IRE
			if(situationElement instanceof StartContractType || situationElement instanceof IfactType ||
					situationElement instanceof LivelineViolationType || situationElement instanceof DeadlineViolationType ||
					situationElement instanceof FulfillmentType || situationElement instanceof ViolationType ||
					situationElement instanceof TimeType) {
				ireSits.add(situationElementCode(situationElement, scope_id));
			} else {
				// not an IRE
				nonIreSits.append("\n\t" + situationElementCode(situationElement, scope_id));
				if(situationElement instanceof ContractualInfoType)
					contractualInfoSits.add((ContractualInfoType) situationElement);   // FIXME: this will only get me cinfos at root!
			}
		}

		// prepare norm-fired-on assertion
		StringBuffer norm_fired_on_assertion = new StringBuffer("\n\t(assert (norm-fired-on (ires");
		// handle ireSits
		if(ireSits.size() == 1) {
			// only 1: it is more efficient to test the processing-context directly in the pattern and to simplify the norm-fired-on check
			// add bindings and processing-context slot to ireSit
			sb.append("\n\t" + "?ire <- (" + ireSits.get(0).substring(1, ireSits.get(0).length()-1) +   // FIXME: this does not work, as some ireSits include an extra pattern binding on the referred to obligation!
					"(processing-context " + contract.getHeader().getId() + "))");
			// add norm-fired-on check
			sb.append("\n\t(not (norm-fired-on (ires ?ire) (context ~" + contract.getHeader().getId() + ")");
			// build on norm-fired-on assertion
			norm_fired_on_assertion.append(" ?ire");
		} else {
			// prepare test for processing-context
			StringBuffer test_for_processing_context = new StringBuffer("\n\t(test (member$ " + contract.getHeader().getId() + " (list");
			// prepare test for norm-fired-on
			StringBuffer test_for_norm_fired_on = new StringBuffer("\n\t(not (norm-fired-on (ires $?ires&:(equal-sets? ?ires (list");

			for(int i=0; i<ireSits.size(); i++) {
				// add bindings and processing-context slot to ireSit
				sb.append("\n\t" + "?ire_" + i + " <- (" + ireSits.get(i).substring(1, ireSits.get(i).length()-1) +   // FIXME: this does not work, as some ireSits include an extra pattern binding on the referred to obligation!
						"(processing-context ?pc_" + i + "))");

				// build on test for processing-context
				test_for_processing_context.append(" ?pc_" + i);
				// build on test for norm-fired-on
				test_for_norm_fired_on.append(" ?ire_" + i);
				// build on norm-fired-on assertion
				norm_fired_on_assertion.append(" ?ire_" + i);
			}
			// conclude test for processing-context
			test_for_processing_context.append(")))");
			// conclude test for norm-fired-on
			test_for_norm_fired_on.append("))) (context ~" + contract.getHeader().getId() + ")))");

			// add test for processing-context
			sb.append(test_for_processing_context.toString());
			// add test for norm-fired-on
			sb.append(test_for_norm_fired_on.toString());
		}
		// conclude norm-fired-on assertion
		norm_fired_on_assertion.append(") (context " + contract.getHeader().getId() + ")))");

		// handle nonIreSits
		sb.append(nonIreSits.toString());

		// =>
		sb.append("\n\t=>");

		// RHS of norm
		
		// prescription
		// - must test end-contract first, because the optional list of obligations will never be null, although it may be empty
		EndContractType end = norm.getPrescription().getEndContract();
		if(end != null) {
			// choice: end-contract
			sb.append("\n\t(assert ");
			sb.append(endContractCode(end, scope_id));
			sb.append(")");
		} else {
			// choice: obligations (list may be empty, but not null)
			List<ObligationType> obls = norm.getPrescription().getObligation();
			for(int j=0; j<obls.size(); j++) {
				sb.append("\n\t(assert ");
				sb.append(obligationCode(obls.get(j), scope_id));
				sb.append(")");
			}
		}

		// add norm-fired-on assertion
		sb.append(norm_fired_on_assertion.toString());

		// conclude norm
		sb.append(" )");
		return sb.toString();
	}

	/**
	 * Start-contract in Jess.
	 * 
	 * @param ifact	The institutional-fact to represent in Jess.
	 */
	private String startContractCode(StartContractType start, String context_id) {
		return "(start-context (context " + context_id + ") (when" + start.getWhen().getContent() + "))";
	}
	
	/**
	 * End-contract in Jess.
	 * 
	 * @param ifact	The institutional-fact to represent in Jess.
	 */
	private String endContractCode(EndContractType end, String context_id) {
		return "(end-context (context " + context_id + ") (when" + end.getWhen().getContent() + "))";
	}
	
	/**
	 * Institutional-fact in Jess.
	 * 
	 * @param ifact	The institutional-fact to represent in Jess.
	 */
	private String ifactCode(IfactType ifact, String context_id) {
		StringBuffer sb = new StringBuffer();
		sb.append("(ifact ");
		sb.append("(context " + context_id + ") ");
		sb.append("(fact " + ifact.getFact().getName());
		// slots in the ifact will be added to the fact multislot in a sequence, each labeled with its name
		List<FrameType.Slot> slots = ifact.getFact().getSlot();
		for(int i=0; i<slots.size(); i++) {
			sb.append(" " + slots.get(i).getName() + " " + slots.get(i).getValue().getContent());
		}
		sb.append(") ");
		sb.append("(when " + ifact.getWhen().getContent() + ")");
		sb.append(")");
		return sb.toString();
	}

	/**
	 * Obligation in Jess.
	 * 
	 * @param obligation	The obligation to represent in Jess.
	 */
	private String obligationCode(ObligationType obligation, String context_id) {
		StringBuffer sb = new StringBuffer();
		sb.append("(obligation ");
		sb.append("(context " + context_id + ") ");
		sb.append("(bearer " + obligation.getBearer().getContent() + ") ");
		sb.append("(counterparty " + obligation.getCounterparty().getContent() + ") ");
		sb.append("(fact " + obligation.getFact().getName());
		// slots in the obligation will be added to the fact multislot in a sequence, each labeled with its name
		List<FrameType.Slot> slots = obligation.getFact().getSlot();
		for(int i=0; i<slots.size(); i++) {
			sb.append(" " + slots.get(i).getName() + " " + slots.get(i).getValue().getContent());
		}
		sb.append(") ");
		if(obligation.getLiveline() != null) {
			sb.append("(liveline " + obligation.getLiveline().getOperand().getContent() + ")");   // TODO Handle expressions in deadline (see XSD)
		}
		sb.append("(deadline " + obligation.getDeadline().getOperand().getContent() + ")");   // TODO Handle expressions in deadline (see XSD)
		sb.append(")");
		return sb.toString();
	}

	/**
	 * Liveline-violation in Jess.
	 * 
	 * @param liveline_violation	The liveline-violation to represent in Jess.
	 */
	private String livelineViolationCode(LivelineViolationType liveline_violation, String context_id) {
		StringBuffer sb = new StringBuffer();
		String oblRef = "?obl" + sb.hashCode();   // generate a unique id for the obligation fact
		sb.append("(liveline-violation ");
		sb.append("(context " + context_id + ") ");
		sb.append("(when " + liveline_violation.getWhen().getContent() + ") ");
		sb.append("(obl " + oblRef + ")");
		sb.append(")\n");
		sb.append(oblRef + "<- (obligation ");
		sb.append("(context " + context_id + ") ");
		sb.append("(bearer " + liveline_violation.getBearer().getContent() + ") ");
		sb.append("(counterparty " + liveline_violation.getCounterparty().getContent() + ") ");
		sb.append("(fact " + liveline_violation.getFact().getName());
		// slots in the liveline-violation will be added to the fact multislot in a sequence, each labeled with its name
		List<FrameType.Slot> slots = liveline_violation.getFact().getSlot();
		for(int i=0; i<slots.size(); i++) {
			sb.append(" " + slots.get(i).getName() + " " + slots.get(i).getValue().getContent());
		}
		sb.append(")");
		sb.append(")");
		return sb.toString();
	}
	
	/**
	 * Deadline-violation in Jess.
	 * 
	 * @param deadline_violation	The deadline-violation to represent in Jess.
	 */
	private String deadlineViolationCode(DeadlineViolationType deadline_violation, String context_id) {
		StringBuffer sb = new StringBuffer();
		String oblRef = "?obl" + sb.hashCode();   // generate a unique id for the obligation fact
		sb.append("(deadline-violation ");
		sb.append("(context " + context_id + ") ");
		sb.append("(when " + deadline_violation.getWhen().getContent() + ") ");
		sb.append("(obl " + oblRef + ")");
		sb.append(")\n");
		sb.append(oblRef + "<- (obligation ");
		sb.append("(context " + context_id + ") ");
		sb.append("(bearer " + deadline_violation.getBearer().getContent() + ") ");
		sb.append("(counterparty " + deadline_violation.getCounterparty().getContent() + ") ");
		sb.append("(fact " + deadline_violation.getFact().getName());
		// slots in the deadline-violation will be added to the fact multislot in a sequence, each labeled with its name
		List<FrameType.Slot> slots = deadline_violation.getFact().getSlot();
		for(int i=0; i<slots.size(); i++) {
			sb.append(" " + slots.get(i).getName() + " " + slots.get(i).getValue().getContent());
		}
		sb.append(")");
		sb.append(")");
		return sb.toString();
	}
	
	/**
	 * Fulfillment in Jess.
	 * 
	 * @param fulfillment	The fulfillment to represent in Jess.
	 */
	private String fulfillmentCode(FulfillmentType fulfillment, String context_id) {
		StringBuffer sb = new StringBuffer();
		String oblRef = "?obl" + sb.hashCode();   // generate a unique id for the obligation fact
		sb.append("(fulfillment ");
		sb.append("(context " + context_id + ") ");
		sb.append("(when " + fulfillment.getWhen().getContent() + ") ");
		sb.append("(obl " + oblRef + ")");
		sb.append(")\n");
		sb.append(oblRef + "<- (obligation ");
		sb.append("(context " + context_id + ") ");
		sb.append("(bearer " + fulfillment.getBearer().getContent() + ") ");
		sb.append("(counterparty " + fulfillment.getCounterparty().getContent() + ") ");
		sb.append("(fact " + fulfillment.getFact().getName());
		// slots in the fulfillment will be added to the fact multislot in a sequence, each labeled with its name
		List<FrameType.Slot> slots = fulfillment.getFact().getSlot();
		for(int i=0; i<slots.size(); i++) {
			sb.append(" " + slots.get(i).getName() + " " + slots.get(i).getValue().getContent());
		}
		sb.append(")");
		sb.append(")");
		return sb.toString();
	}

	/**
	 * Violation in Jess.
	 * 
	 * @param violation	The violation to represent in Jess.
	 */
	private String violationCode(ViolationType violation, String context_id) {
		StringBuffer sb = new StringBuffer();
		String oblRef = "?obl" + sb.hashCode();   // generate a unique id for the obligation fact
		sb.append("(violation ");
		sb.append("(context " + context_id + ") ");
		sb.append("(when " + violation.getWhen().getContent() + ") ");
		sb.append("(obl " + oblRef + ")");
		sb.append(")\n");
		sb.append(oblRef + "<- (obligation ");
		sb.append("(context " + context_id + ") ");
		sb.append("(bearer " + violation.getBearer().getContent() + ") ");
		sb.append("(counterparty " + violation.getCounterparty().getContent() + ") ");
		sb.append("(fact " + violation.getFact().getName());
		// slots in the violation will be added to the fact multislot in a sequence, each labeled with its name
		List<FrameType.Slot> slots = violation.getFact().getSlot();
		for(int i=0; i<slots.size(); i++) {
			sb.append(" " + slots.get(i).getName() + " " + slots.get(i).getValue().getContent());
		}
		sb.append(")");
		sb.append(")");
		return sb.toString();
	}

	/**
	 * Time instant in Jess.
	 * 
	 * @param time	The time instant to represent in Jess.
	 */
	private String timeCode(TimeType time, String context_id) {
		StringBuffer sb = new StringBuffer();
		sb.append("(time ");
		sb.append("(context " + context_id + ") ");
		sb.append("(when " + time.getWhen().getContent() + ")");
		sb.append(")");
		return sb.toString();
	}

	/**
	 * Relational condition in Jess.
	 * 
	 * @param relCondition	The relational condition to represent in Jess.
	 */
	private String relConditionCode(RelConditionType relCondition) {
		StringBuffer sb = new StringBuffer();
		sb.append("(");
		String relOperator = relCondition.getRelOperator();
		if(relOperator.equals("eq")) sb.append("eq*");
		else if(relOperator.equals("gt")) sb.append(">");
		else if(relOperator.equals("ge")) sb.append(">=");
		else if(relOperator.equals("lt")) sb.append("<");
		else if(relOperator.equals("le")) sb.append("<=");
		else if(relOperator.equals("ne")) sb.append("neq");
		List<ExpressionType> expressions = relCondition.getExpression();
		for(int i=0; i<expressions.size(); i++)
			sb.append(" " + expressions.get(i).getOperand().getContent());   // TODO Handle expressions in operands (see XSD)
		sb.append(")");
		return sb.toString();
	}

	/**
	 * And in Jess.
	 * 
	 * @param and	The and to represent in Jess.
	 */
	private String andCode(AndType and, String context_id) {
		StringBuffer sb = new StringBuffer();
		sb.append("(and");
		List<SituationElementType> situationElements = and.getSituationElement();
		for(int i=0; i<situationElements.size(); i++)
			sb.append(" " + situationElementCode(situationElements.get(i), context_id));
		sb.append(")");
		return sb.toString();
	}

	/**
	 * Or in Jess.
	 * 
	 * @param or	The or to represent in Jess.
	 */
	private String orCode(OrType or, String context_id) {
		StringBuffer sb = new StringBuffer();
		sb.append("(or");
		List<SituationElementType> situationElements = or.getSituationElement();
		for(int i=0; i<situationElements.size(); i++)
			sb.append(" " + situationElementCode(situationElements.get(i), context_id));
		sb.append(")");
		return sb.toString();
	}

	/**
	 * Not in Jess.
	 * 
	 * @param not	The not to represent in Jess.
	 */
	private String notCode(NotType not, String context_id) {
		StringBuffer sb = new StringBuffer();
		sb.append("(not ");
		sb.append(situationElementCode(not.getSituationElement(), context_id));
		sb.append(")");
		return sb.toString();
	}

	/**
	 * Situation-element in Jess.
	 * 
	 * @param situationElement	The situation-element to represent in Jess.
	 */
	private String situationElementCode(SituationElementType situationElement, String context_id) {
		// start-context, ifact, fulfillment, violation, time, contractual-info, rel-condition, and, or, not
		if(situationElement instanceof StartContractType)
			return startContractCode((StartContractType) situationElement, context_id);
		else if(situationElement instanceof IfactType)
			return ifactCode((IfactType) situationElement, context_id);
		else if(situationElement instanceof LivelineViolationType)
			return livelineViolationCode((LivelineViolationType) situationElement, context_id);
		else if(situationElement instanceof DeadlineViolationType)
			return deadlineViolationCode((DeadlineViolationType) situationElement, context_id);
		else if(situationElement instanceof FulfillmentType)
			return fulfillmentCode((FulfillmentType) situationElement, context_id);
		else if(situationElement instanceof ViolationType)
			return violationCode((ViolationType) situationElement, context_id);
		else if(situationElement instanceof TimeType)
			return timeCode((TimeType) situationElement, context_id);
		else if(situationElement instanceof ContractualInfoType)
			return contractualInfoCode((ContractualInfoType) situationElement, context_id);
		else if(situationElement instanceof RelConditionType)
			return relConditionCode((RelConditionType) situationElement);
		else if(situationElement instanceof AndType)
			return andCode((AndType) situationElement, context_id);
		else if(situationElement instanceof OrType)
			return orCode((OrType) situationElement, context_id);
		else if(situationElement instanceof NotType)
			return notCode((NotType) situationElement, context_id);
		else
			return null;
	}

}

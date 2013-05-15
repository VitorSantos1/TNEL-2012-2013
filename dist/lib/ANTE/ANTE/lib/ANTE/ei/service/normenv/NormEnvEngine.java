package ei.service.normenv;

import java.util.ArrayList;
import java.util.Iterator;

import jess.Defrule;
import jess.Deftemplate;
import jess.Fact;
import jess.Filter;
import jess.FilteringIterator;
import jess.Group;
import jess.HasLHS;
import jess.JessException;
import jess.Pattern;
import jess.QueryResult;
import jess.Rete;
import jess.Value;
import jess.ValueVector;
import jess.Variable;

/**
 * The Rete engine for the normative environment.
 * <br>
 * This class includes a set of auxiliary methods for accessing the Jess working memory and rule base.
 * 
 * @author hlc
 */
public class NormEnvEngine extends Rete {
	
	private static final long serialVersionUID = 8703014372246725838L;
	
	/**
	 * Finds the top deftemplate for a deftemplate, just before Jess's root template.
	 * 
	 * @param d	The deftemplate to get the top deftemplate for
	 * @return	The top deftemplate
	 */
	public static Deftemplate findTopDeftemplate(Deftemplate d) {
		Deftemplate topD = d;
		Deftemplate temp = d;
		while(!temp.equals(Deftemplate.getRootTemplate())) {
			topD = temp;
			temp = temp.getParent();
		}
		return topD;
	}
	
	/**
	 * Checks whether a deftemplate extends another deftemplate.
	 * 
	 * @param d	The deftemplate to check
	 * @param t	The extended deftemplate
	 */
	public static boolean deftemplateIs(Deftemplate d, Deftemplate t) {
		while(!d.equals(Deftemplate.getRootTemplate())) {
			if(d.equals(t)) {
				return true;
			}
			d = d.getParent();
		}
		return false;
	}

	/**
	 * Gets the templates of the predefined contract types.
	 * 
	 * @return	The templates of the predefined contract types
	 */
	protected ArrayList<Deftemplate> getPredefinedContractTypesDeftemplates() {
		ArrayList<Deftemplate> pCT = new ArrayList<Deftemplate>();
		
		// get (root) contract deftemplate
		Deftemplate contractDT;
		try {
			contractDT = findDeftemplate("contract");
		} catch(JessException je) {
			return pCT;
		}
		
		// iterate through deftemplates to check which are contract types
		Iterator it = listDeftemplates();
		while(it.hasNext()) {
			Deftemplate d = (Deftemplate) it.next();
			if(!d.equals(contractDT) && deftemplateIs(d, contractDT)) {
				pCT.add(d);
			}
		}
		
		return pCT;
	}
	
	/**
	 * Gets the agents participating in a given contract.
	 * 
	 * @param contract_id	The id for the contract
	 * @return				The contractual agents
	 */
	protected ArrayList<String> getContractualAgents(String contract_id) {
		// get the context definition for this contract
		Fact ctxDef = getContextDef(contract_id);
		if(ctxDef == null) return null;
		
		ArrayList<String> contractual_agents = new ArrayList<String>();
		// get the contractual agents
		try {
			ValueVector vv = ctxDef.getSlotValue("who").listValue(getGlobalContext());
			for(int i=0; i<vv.size(); i++) {
				contractual_agents.add(vv.get(i).symbolValue(getGlobalContext()));
			}
		} catch(JessException je) {
			//je.printStackTrace();
			return null;
		}
		return contractual_agents;
	}
	
	/**
	 * Gets the context definition fact for a contract.
	 * 
	 * @param contract_id	The contract id
	 * @return				The context definition fact
	 */
	protected Fact getContextDef(String contract_id) {
		try {
			QueryResult ctxDef = runQueryStar("MAIN::get-context-def", new ValueVector().add(contract_id));
			if(ctxDef.next())
				// return the context definition
				return ctxDef.get("context-def").factValue(getGlobalContext());
		} catch(JessException je) {
		}
		return null;
	}
	
	/**
	 * Gets the super context for a given context.
	 * 
	 * @param current_context	The current context
	 * @return					The super-context for the parameter, or <code>null</code> if there is none
	 */
	protected String getSuperContext(String current_context) {
		// get context definition
		Fact ctxDef = getContextDef(current_context);
		if(ctxDef != null) {
			// if there is one, return super-context
			try {
				return ctxDef.getSlotValue("super-context").stringValue(getGlobalContext());
			} catch(JessException je) {
				return null;
			}
		} else
			return null;
	}

	/**
	 * Get the context applicability of a norm as a Pattern.
	 * <br>
	 * This method assumes that the first pattern of the norm is a pattern for a context. FIXME
	 * Also, for now there is an underlying assumption that only norms (and not Jess rules that are not norms) have this property.
	 * 
	 * @param norm	The norm
	 * @return		The <code>Pattern</code> indicating the norm context scope, or <code>null</code> if the parameter is not a
	 * 				well-formed norm
	 */
	private Pattern getNormScope(Defrule norm) {
		try {
			// start with group because of the implicit 'and', then get first pattern
			Pattern p = (Pattern) ((Group) norm.getConditionalElements()).getConditionalElement(0); 
			if(findTopDeftemplate(p.getDeftemplate()).getName().equals("MAIN::context"))
				return p;
		} catch(Exception e) {
		}
		return null;   // it is not a well-formed norm!
	}
	
	/**
	 * Checks if a norm is applicable to a contract type.
	 * The deftemplate of the context pattern at the beginning of the norm is compared to the contract-type or any of the deftemplates
	 * that the contract-type deftemplate might extend.
	 * 
	 * @param contract_type
	 * @param norm
	 * @return
	 */
	private boolean isNormApplicableToContractType(String contract_type, Defrule norm) {
		// get the norm scope
		Pattern p = getNormScope(norm);
		if(p == null) return false;   // it was not a well-formed norm!
		
		// get the scope template name
		String scope_type = p.getDeftemplate().getBaseName();
		// check if the scope template name is the contract_type
		if(scope_type.equals(contract_type))
			return true;
		
		// if not, check if the norm is broader (that is, if the contract_type extends the norm scope template)
		Deftemplate t;
		try {
			// get the deftemplate for this contract type
			t = findDeftemplate(contract_type);
		} catch(JessException je) {
			return false;
		}
		// move upwards
		t = t.getParent();
		while(!t.getName().equals(Deftemplate.getRootTemplate().getName())) {
			if(t.getBaseName().equals(scope_type))
				return true;
			t = t.getParent();
		}
		return false;
	}
	
	/**
	 * CAUTION: using an unstable API here (namely the jess.Pattern class)
	 * <p>
	 * Checks if a norm is applicable to a contract.
	 * The norm must be applicable to the contract-type. The 'id' slot in the context pattern at the beginning of the norm must unify
	 * with the contract-id (i.e., either they are the same or the pattern uses a variable).
	 * 
	 * --> TODO: This method needs extensive testing!
	 * 
	 * @param contract_id
	 * @param contract_type
	 * @param norm
	 * @return
	 */
	private boolean isNormApplicableToContract(String contract_id, String contract_type, Defrule norm) {
		// check if the norm is applicable to the contract-type
		if(isNormApplicableToContractType(contract_type, norm)) {
			// get the norm scope
			Pattern p = getNormScope(norm);
			if(p == null) return false;   // it was not a well-formed norm!
			
			// get the 'id' slot
			int idSlotIndex = p.getDeftemplate().getSlotIndex("id");
			try {
				if(p.getNTests(idSlotIndex) > 0) {   // must be true
					Value idSlotPattern = ((jess.Test1) p.getTests(idSlotIndex).next()).getValue();
					// is the context pattern id a variable (unifies with contract_id)?
					if(idSlotPattern instanceof Variable)
						return true;
					// is the context pattern id the same as contract_id?
					if(idSlotPattern.symbolValue(getGlobalContext()).equals(contract_id))
						return true;
				}
			} catch (JessException je) {
				je.printStackTrace();
			}
		}
		return false;
	}

	/**
	 * Gets the predefined norms applicable to a given contract type. Only norms in the INSTITUTIONAL-NORMS module will be considered.
	 * 
	 * --> TODO: Maybe I should add the possibility to get norms applicable to a contract type inside a particular context (not necessarily INSTITUTIONAL-NORMS).
	 *           This would give me a result similar to findAllApplicableNorms, i.e. ArrayList<ArrayList<Defrule>>, since norms can be found at different levels.
	 * 
	 * @param contract_type	The contract type
	 * @return				An <code>ArrayList</code> with the applicable norms
	 */
	protected ArrayList<Defrule> findApplicableNorms(String contract_type) {
		
		ArrayList<Defrule> applicableNorms = new ArrayList<Defrule>();
		FilteringIterator defrulesInInstitutionalNormsModule = new FilteringIterator(listDefrules(), new Filter.ByModule("INSTITUTIONAL-NORMS"));
		HasLHS hasLHS;   // listDefrules actually gets all the HasLHS objects (defrules and defqueries)
		while(defrulesInInstitutionalNormsModule.hasNext()) {
			hasLHS = (HasLHS) defrulesInInstitutionalNormsModule.next();
			if(hasLHS instanceof Defrule) {
				// check if it is a norm applicable to this contract type
				if(isNormApplicableToContractType(contract_type, (Defrule) hasLHS))
					applicableNorms.add((Defrule) hasLHS);
			}
		}
		
		return applicableNorms;
	}
	
	/**
	 * Gets the norms inside a module/context applicable to a given contract.
	 * 
	 * @param contract_id	The contract to which the norms apply
	 * @param contract_type	The contract type
	 * @param context		The module/context where to look for applicable norms
	 * @return				An <code>ArrayList</code> with the applicable norms
	 */
	private ArrayList<Defrule> findApplicableNormsInContext(String contract_id, String contract_type, String context) {
		
		ArrayList<Defrule> applicableNormsInContext = new ArrayList<Defrule>();
		FilteringIterator defrulesInModule = new FilteringIterator(listDefrules(), new Filter.ByModule(context));
		HasLHS hasLHS;	// listDefrules actually gets all the HasLHS objects (defrules and defqueries)
		while(defrulesInModule.hasNext()) {
			hasLHS = (HasLHS) defrulesInModule.next();
			if(hasLHS instanceof Defrule) {
				// check if it is a norm applicable to this contract
				if(isNormApplicableToContract(contract_id, contract_type, (Defrule) hasLHS))
					applicableNormsInContext.add((Defrule) hasLHS);
			}
		}
		
		return applicableNormsInContext;
	}
	
	/**
	 * Gets all the norms applicable to a given contract, not filtering overridden norms.
	 * This includes all norms defined in the contract's module/context, in its super-context, in its super-super-context, and so on.
	 * Each of these levels will correspond to an ArrayList inserted in the returned ArrayList, starting at position 0 (which
	 * corresponds to norms defined inside the contract's module/context).
	 * 
	 * --> TODO: This method needs extensive testing!
	 * 
	 * @param contract_id	The contract to which the norms apply
	 * @return				An <code>ArrayList</code> with all the applicable norms, grouped by context in <code>ArrayList</code>s
	 */
	protected ArrayList<ArrayList<Defrule>> findAllApplicableNorms(String contract_id) {
		// get the context definition for this contract
		Fact ctxDef = getContextDef(contract_id);
		if(ctxDef == null) return null;
		// get the contract type
		String contract_type = ctxDef.getDeftemplate().getBaseName();
		
		// norms will be searched in at least 2 levels: the contract and its super-module (there might be more, though)
		ArrayList<ArrayList<Defrule>> applicableNorms = new ArrayList<ArrayList<Defrule>>(2);
		
		// get norms defined inside this contract's module and in "upper contexts"
		String current_module = contract_id;
		// while current_module exists
		while(current_module != null) {
			// add norms in this context/module
			ArrayList<Defrule> furtherApplicableNorms = findApplicableNormsInContext(contract_id, contract_type, current_module);
			applicableNorms.add(furtherApplicableNorms);
			// go up to super-context if there is one
			current_module = getSuperContext(current_module);
		}
		return applicableNorms;
	}
	
}

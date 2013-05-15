package ei.service.ctr;

import java.util.Vector;

/**
 * This class represents the Model to Map contracts' outcomes so CTR can calculate trust value
 * 
 * @author Sérgio
 *
 */
public class OutcomeGenerator {
	public enum Outcome {Fulfilled, Violated, DeadlineViolated, DeadlineViolatedFulfilled, DeadlineViolatedDeadlineViolated, DeadlineViolatedViolated, ViolatedFulfilled, ViolatedDeadlineViolated, ViolatedViolated};
	//public enum Outcome {Fulfilled, Violated, DeadlineViolated};
	public enum MappingMethod {Binary, FirstEvidence, FourEvidences, FiveEvidences, AllDifferent};
	public OutcomeGenerator () {
		
	}
	
	/**
	 * Gets a mapped value for an outcome
	 * @return label
	 */
	@SuppressWarnings("static-access")
	public Float computeOutcome (OutcomeGenerator.MappingMethod method, Vector<OutcomeGenerator.Outcome> out) {
		if(out.size() == 0)
			return 0.0f;
//		if(out.size()==1)
//			return getSimpleOutcome(out);
		else
		{
			if(method == method.Binary) {
				return getBinaryOutcome(out);
			}
			if(method == method.FirstEvidence) {
				return getFirstEvidenceOutcome(out);
			}
			if(method == method.FourEvidences) {
				return getFourEvidencesOutcome(out);
			}
			if(method == method.FiveEvidences) {
				return getFiveEvidencesOutcome(out);
			}
			if(method == method.AllDifferent) {
				return getAllDifferentOutcome(out);
			}
		}
		
		
		return 0.0f;
	}
	
	private Float getAllDifferentOutcome(Vector<Outcome> out) {
		if(out.size()>1)
		{
			if(out.get(0).equals(Outcome.Violated) && out.get(1).equals(Outcome.Violated))
				return -1.5f;
			if(out.get(0).equals(Outcome.Violated) && out.get(1).equals(Outcome.DeadlineViolated))
				return -1.0f;
			if(out.get(0).equals(Outcome.Violated) && out.get(1).equals(Outcome.Fulfilled))
				return -0.5f;
			if(out.get(0).equals(Outcome.DeadlineViolated) && out.get(1).equals(Outcome.Violated))
				return 0.0f;
			if(out.get(0).equals(Outcome.DeadlineViolated) && out.get(1).equals(Outcome.DeadlineViolated))
				return 0.5f;
			if(out.get(0).equals(Outcome.DeadlineViolated) && out.get(1).equals(Outcome.Fulfilled))
				return 0.75f;
			else
				return 1.0f;
		}
		else
		{
			if(out.get(0).equals(Outcome.Violated))
				return -1.5f;
			if(out.get(0).equals(Outcome.DeadlineViolated))
				return 0.0f;
			else
				return 1.0f;
		}
	}

	private Float getFiveEvidencesOutcome(Vector<Outcome> out) {
		if(out.size()>1)
		{
			if(out.get(0).equals(Outcome.Violated) && out.get(1).equals(Outcome.Violated))
				return -1.5f;
			if(out.get(0).equals(Outcome.Violated) && out.get(1).equals(Outcome.DeadlineViolated))
				return -1.0f;
			if(out.get(0).equals(Outcome.DeadlineViolated) && out.get(1).equals(Outcome.Violated) ||
					out.get(1).equals(Outcome.Fulfilled) && out.get(0).equals(Outcome.Violated))
				return 0.0f;
			if(out.get(0).equals(Outcome.DeadlineViolated))
				return 0.5f;
			else
				return 1.0f;
		}
		else
		{
			if(out.get(0).equals(Outcome.Violated))
				return -1.5f;
			if(out.get(0).equals(Outcome.DeadlineViolated))
				return 0.5f;
			else
				return 1.0f;
		}
	}

	private Float getFourEvidencesOutcome(Vector<Outcome> out) {
		if(out.size() > 1)
		{
			if(out.get(0).equals(Outcome.Violated) && out.get(1).equals(Outcome.Violated))
				return -1.5f;
			if(out.get(0).equals(Outcome.Violated))
				return 0.0f;
			if(out.get(1).equals(Outcome.Violated))
				return 0.5f;
			else
				return 1.0f;
		}
		else
		{
			if(out.get(0).equals(Outcome.Violated))
				return -1.5f;
			if(out.get(0).equals(Outcome.DeadlineViolated))
				return 0.25f;
			else
				return 1.0f;
		}
	}

	private Float getFirstEvidenceOutcome(Vector<Outcome> out) {
		if (out.get(0) != null) {
			if(out.get(0).equals(Outcome.Violated)) {
				return -1.5f;
			}
			if(out.get(0).equals(Outcome.Fulfilled)) {
				return 1.0f;
			} else {
				return 0.0f;
			}
		}
		return 0.0f;
	}

	private Float getBinaryOutcome(Vector<Outcome> out) {
		if (out.get(0) != null) {
			if(out.get(0).equals(Outcome.Violated))
				return -1.5f;
			else 
				return 1.0f;
		}
		return 0.0f;
	}
}
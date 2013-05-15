package ei.onto.synchronization;

import ei.onto.util.Parameters;
import jade.content.*;

/**
 * This class represents the action "send-contract-types". It is empty since this action does not require any more information.
 * 
 * @author hlc
 */
public class StartEpisode implements AgentAction {
	private static final long serialVersionUID = -5775213626217243146L;
	
	private int episodeNumber;
	private Parameters parameters;
	
	public StartEpisode() {
		this.setParameters(new Parameters());
	}
	
	public StartEpisode(int episodeNumber) {
		this();
		this.setEpisodeNumber(episodeNumber);
	}

	public void setParameters(Parameters parameters) {
		this.parameters = parameters;
	}

	public Parameters getParameters() {
		return parameters;
	}

	public void setEpisodeNumber(int episodeNumber) {
		this.episodeNumber = episodeNumber;
	}

	public int getEpisodeNumber() {
		return episodeNumber;
	}

}

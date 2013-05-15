package ei.service.ontology;

import java.util.StringTokenizer;
import java.util.Vector;

/**
 * @author Andreia Malucelli
 */
public class StopWords {
	private static String[] unrelevant = {
			"a",
			"about",
			"all",
			"an",
			"and",
			"any",
			"are",
			"around",
			"as",
			"at",
			"by",
			"can",
			"do",
			"does",
			"for",
			"from",
			"how",
			"if",
			"in",
			"into",
			"is",
			"it",
			"its",
			"not",
			"of",
			"on",
			"or",
			"other",
			"out",
			"so",
			"something",
			"that",
			"the",
			"to",
			"was",
			"what",
			"when",
			"where",
			"which",
			"who",
			"will",
			"with" };
	
	
	/**
	 * Used for testing only.
	 * @param args
	 */
	public static void main(String[] args) {
		String desc1 = "a device that produces electricity";
		String desc2 = "a device that stores energy and produces electric current by chemical action";
		
		desc1 = StopWords.takeOutStopWords(desc1);
		desc2 = StopWords.takeOutStopWords(desc2);
		
		new NGrams(3, 2).compare(desc1, desc2);
	}
	
	public static String takeOutStopWords(String desc) {
		StringTokenizer tokenizer = new StringTokenizer(desc, " ");
		Vector<String> v = new Vector<String>();
		boolean append = true;
		
		while(tokenizer.hasMoreTokens()) {
			String token = tokenizer.nextToken();
			
			// Eliminate '.', ',', ';' at the end of token.
			if(token.endsWith(".") || token.endsWith(",") || token.endsWith(";")) {
				token = (token.subSequence(0, token.length()-1)).toString();
			}
			
			for(int i = 0; i < unrelevant.length; i++) {
				String tmp = unrelevant[i];
				if(token.equalsIgnoreCase(tmp)) {
					//System.out.println(i);
					append = false;
					break;
				}
			}
			if(append) {
				//sb.append(token + " ");
				if(! v.contains(token)) {
					v.add(token);
				}
			} else {
				append = true;
			}
		}
		
		StringBuffer sb = new StringBuffer();
		for(int i = 0; i < v.size(); i++) {
			sb.append((String)v.elementAt(i) + " ");
		}
		
		return sb.toString().trim();
	}
}

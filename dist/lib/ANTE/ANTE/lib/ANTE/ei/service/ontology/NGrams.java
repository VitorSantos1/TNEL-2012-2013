package ei.service.ontology;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.StringTokenizer;
import java.util.Vector;

/**
 * @author Andreia Malucelli
 */
public class NGrams {

	private int numberOfLetters;
	private int spaces;
	
	/**
	 * @param a
	 * @param b
	 */
	public NGrams(int a, int b) {
		numberOfLetters = a;
		spaces = b;
	}
	
	/**
	 * Used for testing only.
	 */
	public static void main(String[] args) {
		
		String s1 = "has_wireless";
		String s2 = "wireless";
		
		System.out.println(new NGrams(3, 2).compare(s1, s2));
	}
  
	private double match(String strMatch1, String strMatch2) {
		
		Vector<String> vectorString = new Vector<String>();
		int matches = 0;
		String strMatch3;

		// if first match-string (strMatch1) is shorter, then it will swap with the second match-string (strMatch2)
		if(strMatch1.length() < strMatch2.length()) {
			strMatch3 = strMatch1;
			strMatch1 = strMatch2;
			strMatch2 = strMatch3;
		}
		
		// exchange unregular characters with ' '
		strMatch1 = normalizeText(strMatch1);
		strMatch2 = normalizeText(strMatch2);
		
		// insert ' ' to front and back of lematch-string to afford an overlapping match
		for(int index = 0; index < spaces; index++) {
			strMatch1 = " " + strMatch1 + " ";
			strMatch2 = " " + strMatch2 + " ";
		}
		
		/*
		System.out.println("strMatch1: '" + strMatch1 + "'");
		System.out.println("strMatch2: '" + strMatch2 + "'");
		*/
		// build a vector with all possible substrings(strMatch1) of <numberOfLetters>-length
		for(int index = 0; index + numberOfLetters <= strMatch1.length(); index++) {
			vectorString.addElement(strMatch1.substring(index,index + numberOfLetters));
		}
		
		//System.out.println(vectorString + " size: " + vectorString.size());
		
		// match every substring with strMatch2, if substring matches with part of strMatch2 increase <matches>
		for(int index = 0 ; index < vectorString.size(); index++) {
			if(strMatch2.indexOf(vectorString.elementAt(index).toString ()) != -1) {
				//System.out.println(vectorString.elementAt(index).toString ());
				matches++;
			}
		}
		
		//System.out.println(matches);
		
		if(vectorString.size() > 0) {
			return (double)matches / vectorString.size();
		}
		return 0.0;
	}
	
	// All characters not included in <letters> will replace by ' '
	private String normalizeText(String str) {
		String letters = "qwertyuioplkjhgfdsazxcvbnmQWERTYUIOPASDFGHJKLZXCVBNM1234567890";
		String strFinal = "";
		int index = 0;

		str = str.toLowerCase();
		for(index = 0; index < str.length() ;index++) {
			strFinal = strFinal + (letters.indexOf(str.charAt(index)) != -1 ? String.valueOf(str.charAt(index)) : " ");
		}
		return strFinal.trim();
	}
	
	@SuppressWarnings("unchecked")
	public double compare(String requested, String proposed) {
		//System.out.println("Requested Term: " + requested);
		//System.out.println("Proposed Term: " + proposed);
		
		StringTokenizer st1 = new StringTokenizer(requested);
		StringTokenizer st2 = new StringTokenizer(proposed);
		int requestedTokens = st1.countTokens();
		//int proposedTokens = st2.countTokens();
		
		double[][] matrix = new double[st1.countTokens()][st2.countTokens()];
		
		int st1count = 0;
		int st2count = 0;
		while(st1.hasMoreTokens()){
			String word1 = st1.nextToken();
			st2 = new StringTokenizer(proposed);
			st2count = 0;
			while(st2.hasMoreTokens()){
				String word2 = st2.nextToken();
				matrix[st1count][st2count] = match(word1, word2);
				
				//System.out.println("Result '" + word1 + "' and '" + word2 + "': " + matrix[st1count][st2count]);
				//gui.setSyntacticArea("Result '" + word1 + "' and '" + word2 + "': " + matrix[st1count][st2count]);
				st2count++;
			}
			st1count++;
		}
		
		double result = 0.0;
		for(int i = 0; i < st1count; i++) {
			Vector<Double> v = new Vector<Double>();
			for(int j = 0; j < st2count; j++) {
				v.add(new Double(matrix[i][j]));
			}
			
			Comparator comparator = new Comparator() {
				public int compare(Object o1, Object o2) {
					double d1 = ((Double)o1).doubleValue();
					double d2 = ((Double)o2).doubleValue();
					if(d1 < d2) {
						return 1;		
					} else if(d1 > d2) {
						return -1;		
					} else {
						return 0;
					}
				}
			};
			
			ArrayList<Object> sortedList = new ArrayList<Object>(v);
			Collections.sort(sortedList, comparator);
			//System.out.println("1st: " + ((Double)sortedList.get(0)).doubleValue());
			result += ((Double)sortedList.get(0)).doubleValue();
		}
		
		// Calculate overall result. 
		double overallResult = result / requestedTokens;
		//System.out.println("RESULT: " + result + " / " + requestedTokens + " = " + overallResult);
		
		return overallResult;
	}
}
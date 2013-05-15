package ei.service.ontology;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.ConnectException;
import java.util.Hashtable;


/**
 * @author Andreia Malucelli
 *
 * This file is used to calculate the similarity between each word
 */

public class SemanticSimilarity {
	
	public static double LCH_MAX = 3.63758615972639;
	private String host = "127.0.0.1";
	private String port = "6180";
	private String wordnetSimilarityFile = "WordNetSimilarity.dat";
	String method = "lch";
	
	public SemanticSimilarity(String host, String port, String wordnetFile) {
		
		this.host = host;
		this.port = port;
		this.wordnetSimilarityFile = wordnetFile;

	}
	
	public SemanticSimilarity() {
	}
	
	private Hashtable<String,Double> loadWordNetSimilarities(String wordnetSimilarityFile) {
		
		try {
			File f = new File(wordnetSimilarityFile);
			if(f.exists()) {
				FileInputStream in = new FileInputStream(f);
				ObjectInputStream os = new ObjectInputStream(in);
				Hashtable<String,Double> wordNetSimilarities = (Hashtable<String,Double>) os.readObject();
				os.close();
				return wordNetSimilarities;
			}
		} catch(Exception e) {
			System.out.println("Exception loading hashtable: " + e);
		}
		return new Hashtable<String,Double>();
	}
	
	
	public double compareWordNet(String word1, String word2) {
		
		double wordNetResult = 0.0;
		
		Hashtable<String,Double> wordNetSimilarities = loadWordNetSimilarities(wordnetSimilarityFile);
		Double d = wordNetSimilarities.get(word1 + " " + word2);
		if(d != null) {
//			System.out.println(word1 + " " + word2 + ": found in file " + wordnetSimilarityFile);
			wordNetResult = d;
		} else {
//			System.out.println(word1 + " " + word2 + ": not found in file " + wordnetSimilarityFile);
		}
		
		if (wordNetResult != 0.0 )
		{
			
			if(wordNetResult == -1.0) // it exists but wordnet doesn't know
				wordNetResult = 0.0;
			
			//System.out.println("OSAg: WordNet-Similarity ['" + word1 + "' - '" + word2 + "']: " + wordNetResult + " which corresponds to: " + (wordNetResult/this.LCH_MAX));
			return wordNetResult/this.LCH_MAX;
			
		}else { //If the name does not exist in the file, we try to find it in wordnet server

			wordNetResult = this.compareAllSenses(method, word1, word2);
			
			try {
			    

				if(wordNetResult == -2.0)
				{
					return wordNetResult;
				}
				else if(wordNetResult == 0.0)
				{
					// We need to put a value different from 0.0, because when we use the get method and the word is not there, the value returned is also 0.0
					// so to know that the value has been searched we but -1.0
					wordNetSimilarities.put(word1 + " " + word2, new Double(-1.0));
					
					
				}else{
					
					wordNetSimilarities.put(word1 + " " + word2, new Double(wordNetResult));
						
				}
				FileOutputStream out = new FileOutputStream(wordnetSimilarityFile);
				ObjectOutputStream s = new ObjectOutputStream(out);
				s.writeObject(wordNetSimilarities);
				s.flush();
				s.close();
				out.close();
			} catch(Exception e) {
				System.out.println(e.getMessage());	
			}
		
		}
		
		//System.out.println("OSAg: WordNet-Similarity ['" + word1 + "' - '" + word2 + "']: " + wordNetResult + " which corresponds to: " + (wordNetResult/this.LCH_MAX));
		return wordNetResult/this.LCH_MAX;
	}
	
	
	//Utility to test wordnet....
	public static void main(String[] args)
	{
		SemanticSimilarity s = new SemanticSimilarity();
		s.compare("lhc", "code", "cipher");
		
	}
	
	/**
	 * Comparison that takes into account all senses.
	 * 
	 * @param methodName the method name
	 * @param word1 the word1
	 * @param word2 the word2
	 * 
	 * @return the double
	 */
	

	public double compareAllSenses(String methodName, String word1, String word2) {	
	
	   double result = 0.0;
	   Object o = compare(methodName, word1, word2);
	   if (o == null) return -2.0;
				
	   if(o instanceof Double)
	   {
			result = ((Double)o).doubleValue();  
	   }
					
		return result;
	}
		
	/**
	 * 
	 * @param methodName (possible names: hso, jcn, lch, res, wup)
	 * @param word1
	 * @param word2
	 * @return	the result from wordnet
	 */
	// Requests semantic similarity from WordNet of two words
	public Object compare(String methodName, String word1, String word2) {
		Object object = null;
		
		try {
			// Build a querystring for the request
	        String data = "method=" + methodName;
	        data += "&word1=" + word1;
	        data += "&word2=" + word2;
	        data += "\n";
	        
	        // Establish a new connection to the WordNet-Server (running on local machine as Perl-Script)
			Socket socket = new Socket( host, Integer.parseInt(port) );
		    PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
		    BufferedReader in = new BufferedReader(new InputStreamReader( socket.getInputStream()));
		    
		    // Send the querystring to the server
		    out.println( data );
		    
		    // Receive the answer, if both words exist
		    String result = "";
		    String temp = null;
		    do 
		    {
		    	temp = in.readLine();
		    	if ( temp != null ) result = temp;
		    } while (  temp != null );
		    in.close();
		    out.close();
		    socket.close();
	        
		    object = result;
	        
	        
	        try {
	        	object = new Double((String)result);
	        } catch(Exception e) {
	        	
	        	System.out.println("error casting ... " + e.getMessage().toString());
	        	return null;
	        }
		} catch ( ConnectException ce ) {
			System.out.println( "Cannot connect to WordNet server !!!");
			return null;
		}
		catch(IOException ioe) {
			  System.out.println("IOException: " + ioe);
			  return null;
		}
		
		return object;
	}
}

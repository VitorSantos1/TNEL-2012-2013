package ei.scripts;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Vector;


public class GeraOntologias {

	private static final int NUMBER_OF_RUNS = 150;
	private static final int NUMBER_OF_BUYERS = 20;
	private static final int NUMBER_OF_SELLERS = 50;
	private static final String FOLDER = "config/experiments/textile/";
	private static final int MAX_PRICE = 10;
	
	private static Vector<Integer> quant;
	private static Vector<Integer> dtime;
	
	private static void init()
	{
		quant = new Vector<Integer>();

		quant.add(180000);
		quant.add(270000);
		quant.add(360000);
		quant.add(450000);
		quant.add(540000);
		quant.add(630000);
		quant.add(720000);
		quant.add(810000);
		quant.add(900000);
		
		dtime = new Vector<Integer>();

		dtime.add(7);
		dtime.add(10);
		dtime.add(13);
		dtime.add(16);
		dtime.add(19);
		dtime.add(22);
		dtime.add(25);
		dtime.add(28);
		dtime.add(31);
	}
	
	static Integer geraLimites(int flag)
	{
		Integer m = null;
		if(flag == 0)
		{
			int n = (int) (Math.random()*(quant.size()));
			
			m = quant.get(n);
			
		}
		else if (flag == 1)
		{
			int n = (int) (Math.random()*(dtime.size()));
			
			m = dtime.get(n);
		}
		
		return m;
	}
	

	static Float geraPrecos()
	{
			float n1 = (int) (Math.random()*(MAX_PRICE))+1;
			
		return n1;
	}

	
	public static String getFirst()
	{
		String s = "<?xml version=\"1.0\"?>\n"+
		"<!DOCTYPE rdf:RDF [\n"+
		    "<!ENTITY domotics1 \"file:/C:/domotics1.owl#\" >\n"+
		    "<!ENTITY owl \"http://www.w3.org/2002/07/owl#\" >\n"+
		    "<!ENTITY xsd \"http://www.w3.org/2001/XMLSchema#\" >\n"+
		    "<!ENTITY owl2xml \"http://www.w3.org/2006/12/owl2-xml#\" >\n"+
		    "<!ENTITY rdfs \"http://www.w3.org/2000/01/rdf-schema#\" >\n"+
		    "<!ENTITY rdf \"http://www.w3.org/1999/02/22-rdf-syntax-ns#\" >\n"+
		    "<!ENTITY O1 \"http://www.semanticweb.org/ontologies/2010/9/O1.owl#\" >\n"+
		"]>\n\n"+
		"<rdf:RDF xmlns=\"http://www.semanticweb.org/ontologies/2010/9/O1.owl#\"\n"+
		     "xml:base=\"http://www.semanticweb.org/ontologies/2010/9/O1.owl\"\n"+
		     "xmlns:rdfs=\"http://www.w3.org/2000/01/rdf-schema#\"\n"+
		     "xmlns:owl2xml=\"http://www.w3.org/2006/12/owl2-xml#\"\n"+
		     "xmlns:owl=\"http://www.w3.org/2002/07/owl#\"\n"+
		     "xmlns:domotics1=\"file:/C:/domotics1.owl#\"\n"+
		     "xmlns:xsd=\"http://www.w3.org/2001/XMLSchema#\"\n"+
		     "xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\"\n"+
		     "xmlns:O1=\"http://www.semanticweb.org/ontologies/2010/9/O1.owl#\">\n"+
		    "<owl:Ontology rdf:about=\"\"/>\n\n"+
		    "<!--\n"+ 
		    "///////////////////////////////////////////////////////////////////////////////////////\n"+
		    "//\n"+
		    "// Data properties\n"+
		    "//\n"+
		    "///////////////////////////////////////////////////////////////////////////////////////\n"+
		     "-->\n\n"+
		    "<!-- http://www.semanticweb.org/ontologies/2010/9/O1.owl#delivery -->\n\n"+
		    "<owl:DatatypeProperty rdf:about=\"#delivery\"/>\n\n"+
		    "<!-- http://www.semanticweb.org/ontologies/2010/9/O1.owl#delivery_pref -->\n"+
		    "<owl:DatatypeProperty rdf:about=\"#delivery_pref\">\n"+
		        "<rdfs:subPropertyOf rdf:resource=\"#delivery\"/>\n"+
		    "</owl:DatatypeProperty>\n\n"+
		    "<!-- http://www.semanticweb.org/ontologies/2010/9/O1.owl#price -->\n\n"+
		    "<owl:DatatypeProperty rdf:about=\"#price\"/>\n"+
		    "<!-- http://www.semanticweb.org/ontologies/2010/9/O1.owl#price_pref -->\n\n"+
		    "<owl:DatatypeProperty rdf:about=\"#price_pref\">\n"+
		        "<rdfs:subPropertyOf rdf:resource=\"#price\"/>\n"+
		    "</owl:DatatypeProperty>\n\n"+
		    
			
		    "<!-- http://www.semanticweb.org/ontologies/2010/9/O1.owl#quantity -->\n\n"+

		    "<owl:DatatypeProperty rdf:about=\"#quantity\"/>\n\n"+
		    
		    "<!-- http://www.semanticweb.org/ontologies/2010/9/O1.owl#quantity_pref -->\n\n"+

		    "<owl:DatatypeProperty rdf:about=\"#quantity_pref\">\n"+
		        "<rdfs:subPropertyOf rdf:resource=\"#quantity\"/>\n"+
		    "</owl:DatatypeProperty>\n\n"+

		    "<!--\n"+ 
		    "///////////////////////////////////////////////////////////////////////////////////////\n"+
		    "//\n"+
		    "// Classes\n"+
		    "//\n"+
		    "///////////////////////////////////////////////////////////////////////////////////////\n"+
		     "-->\n\n"+
		    "<!-- http://www.semanticweb.org/ontologies/2010/9/O1.owl#Chiffon -->\n"+
		    "<owl:Class rdf:about=\"#Chiffon\">\n"+
		        "<rdfs:subClassOf rdf:resource=\"#Textile\"/>\n"+
		    "</owl:Class>\n\n"+
		    "<!-- http://www.semanticweb.org/ontologies/2010/9/O1.owl#Cotton -->\n"+

		    "<owl:Class rdf:about=\"#Cotton\">\n"+
		        "<rdfs:subClassOf rdf:resource=\"#Textile\"/>\n"+
		    "</owl:Class>\n\n"+
		    "<!-- http://www.semanticweb.org/ontologies/2010/9/O1.owl#Textile -->\n"+
		    "<owl:Class rdf:about=\"#Textile\"/>\n\n"+
		    "<!-- http://www.semanticweb.org/ontologies/2010/9/O1.owl#Voile -->\n"+
		    "<owl:Class rdf:about=\"#Voile\">\n"+
		        "<rdfs:subClassOf rdf:resource=\"#Textile\"/>\n"+
		    "</owl:Class>\n\n";

		return s;
	}

	public static String getIndividuals(int i)
	{
		String s;
		Integer quantity = geraLimites(0);
		Integer dtime = geraLimites(1);
		Float price = geraPrecos();
		switch(i)
		{
		case 1:
			s =
			    "<!--\n"+ 
			    "///////////////////////////////////////////////////////////////////////////////////////\n"+
			    "//\n"+
			    "// Individuals\n"+
			    "//\n"+
			    "///////////////////////////////////////////////////////////////////////////////////////\n"+
			    "-->\n\n"+
			    "<!-- http://www.semanticweb.org/ontologies/2010/9/O1.owl#chiffon -->\n"+
			    "<Chiffon rdf:about=\"#chiffon\">\n"+
				"<rdf:type rdf:resource=\"#Chiffon\"/>\n"+
			        "<delivery rdf:datatype=\"&xsd;int\">7</delivery>\n"+
			        "<delivery rdf:datatype=\"&xsd;int\">31</delivery>\n"+
			        "<delivery_pref rdf:datatype=\"&xsd;int\">"+dtime+"</delivery_pref>\n"+
					"<price rdf:datatype=\"&xsd;float\">1.0</price>\n"+
			        "<price rdf:datatype=\"&xsd;float\">10.0</price>\n"+
			        "<price_pref rdf:datatype=\"&xsd;float\">"+price+"</price_pref>\n"+
					"<quantity rdf:datatype=\"&xsd;int\">180000</quantity>\n"+
			        "<quantity rdf:datatype=\"&xsd;int\">900000</quantity>\n"+
			        "<quantity_pref rdf:datatype=\"&xsd;int\">"+quantity+"</quantity_pref>\n"+
			    "</Chiffon>\n\n";
			return s;
		case 2:
			s =
			    "<!--\n"+ 
			    "///////////////////////////////////////////////////////////////////////////////////////\n"+
			    "//\n"+
			    "// Individuals\n"+
			    "//\n"+
			    "///////////////////////////////////////////////////////////////////////////////////////\n"+
			    "-->\n\n"+
			    "<!-- http://www.semanticweb.org/ontologies/2010/9/O1.owl#cotton -->\n"+
			    "<Cotton rdf:about=\"#cotton\">\n"+
				"<rdf:type rdf:resource=\"#Cotton\"/>\n"+
				"<delivery rdf:datatype=\"&xsd;int\">7</delivery>\n"+
		        "<delivery rdf:datatype=\"&xsd;int\">31</delivery>\n"+
		        "<delivery_pref rdf:datatype=\"&xsd;int\">"+dtime+"</delivery_pref>\n"+
					"<price rdf:datatype=\"&xsd;float\">1.0</price>\n"+
			        "<price rdf:datatype=\"&xsd;float\">10.0</price>\n"+
			        "<price_pref rdf:datatype=\"&xsd;float\">"+price+"</price_pref>\n"+
			        "<quantity rdf:datatype=\"&xsd;int\">180000</quantity>\n"+
			        "<quantity rdf:datatype=\"&xsd;int\">900000</quantity>\n"+
			        "<quantity_pref rdf:datatype=\"&xsd;int\">"+quantity+"</quantity_pref>\n"+
			    "</Cotton>\n\n";
			return s;
		case 3:
			s =
			    "<!--\n"+ 
			    "///////////////////////////////////////////////////////////////////////////////////////\n"+
			    "//\n"+
			    "// Individuals\n"+
			    "//\n"+
			    "///////////////////////////////////////////////////////////////////////////////////////\n"+
			    "-->\n\n"+
			    "<!-- http://www.semanticweb.org/ontologies/2010/9/O1.owl#voile -->\n"+
			    "<Voile rdf:about=\"#voile\">\n"+
				"<rdf:type rdf:resource=\"#Voile\"/>\n"+
					"<delivery rdf:datatype=\"&xsd;int\">7</delivery>\n"+
					"<delivery rdf:datatype=\"&xsd;int\">31</delivery>\n"+
					"<delivery_pref rdf:datatype=\"&xsd;int\">"+dtime+"</delivery_pref>\n"+
					"<price rdf:datatype=\"&xsd;float\">1.0</price>\n"+
			        "<price rdf:datatype=\"&xsd;float\">10.0</price>\n"+
			        "<price_pref rdf:datatype=\"&xsd;float\">"+price+"</price_pref>\n"+
			        "<quantity rdf:datatype=\"&xsd;int\">180000</quantity>\n"+
			        "<quantity rdf:datatype=\"&xsd;int\">900000</quantity>\n"+
			        "<quantity_pref rdf:datatype=\"&xsd;int\">"+quantity+"</quantity_pref>\n"+	
			    "</Voile>\n\n";
			return s;
			
		}
		return "";
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		init();
		FileWriter fstream;
		try {
			
			for(int r = 0; r < NUMBER_OF_RUNS; r++) {

				try {
					File f = new File(FOLDER + "RUN_" + r );
					f.mkdir();
				} catch(Exception e) {
					e.printStackTrace();
				}

				try{
					File f = new File(FOLDER + "RUN_" + r + "/owl");
					f.mkdir();
				} catch(Exception e) {
					e.printStackTrace();
				} 
				
				for(int i = 0; i < NUMBER_OF_SELLERS; i++)
				{
					fstream = new FileWriter(FOLDER + "RUN_" + r + "/owl/Supplier"+(i+1)+".owl");
					String s = getFirst()+getIndividuals(1)+getIndividuals(2)+getIndividuals(3)+"</rdf:RDF>\n";

					BufferedWriter out = new BufferedWriter(fstream);
					out.write(s);
					out.close();
				}
				
				for(int i = 0; i < NUMBER_OF_BUYERS; i++)
				{
					fstream = new FileWriter(FOLDER + "RUN_" + r + "/owl/Requester"+(i+1)+".owl");
					String s = getFirst();
					int n = (int) ((Math.random()*3)+1);
					
					s = s+getIndividuals(n);

					s=s+"</rdf:RDF>\n";

					BufferedWriter out = new BufferedWriter(fstream);
					out.write(s);
					out.close();
				}

			}
			
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

}

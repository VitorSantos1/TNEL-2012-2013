package ei.agent.enterpriseagent.ontology;

import java.io.File;
import java.util.Vector;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.w3c.dom.Element;

/**
 * @author pbn, hlc
 */
public class XMLBusinessReader {
	
	public static void loadBusiness(File f, Vector<Competence> competence, Vector<Need> need) throws XMLBusinessReaderException {
		DocumentBuilderFactory factory;
		DocumentBuilder builder;
		Document rootElement;
		
		try {
			factory = DocumentBuilderFactory.newInstance();		
			factory.setValidating(true);
			builder = factory.newDocumentBuilder();
			rootElement = builder.parse(f);
			rootElement.getDocumentElement().normalize();	
			//System.out.println("Root element :" + rootElement.getDocumentElement().getNodeName());			
		} catch (Exception e) {
			 e.printStackTrace();
			 
			throw new XMLBusinessReaderException("invalid file");
		}

		NodeList needsList = ((Element) rootElement.getElementsByTagName("business").item(0)).getElementsByTagName("needs");
		for (int i = 0; i < needsList.getLength(); i++) { 
			Vector<Need> n = loadNeeds( ((Element)needsList.item(i)).getElementsByTagName("need"));
			for(int c=0; c<n.size(); c++) {
				need.add(n.get(c));
			}
		}
		NodeList competencesList = ((Element) rootElement.getElementsByTagName("business").item(0)).getElementsByTagName("competences");
		for (int i = 0; i < competencesList.getLength(); i++) { 
			Vector<Competence> c = loadCompetences( ((Element)competencesList.item(i)).getElementsByTagName("competence"));
			for(int j=0; j<c.size(); j++) {
				competence.add(c.get(j));
			}				
		}
	}
	
	private static Vector<Need> loadNeeds(NodeList business) {
		Vector<Need> needs = new Vector<Need>();
		for (int i = 0; i < business.getLength(); i++) {
			Node businessNode = business.item(i);
			Element businessElement = (Element) businessNode;

			Need need = new Need(businessElement.getAttribute("name"));
			need.setType(businessElement.getAttribute("type"));
			
			if ( !businessElement.getAttribute("productionVolume").equals("") ) {
				need.setProductionVolume( Integer.parseInt(businessElement.getAttribute("productionVolume")) );
			}

			setAttributes(need, businessElement.getElementsByTagName("attribute"));

			needs.add(need);
		}
		return needs;
	}
	
	private static Vector<Competence> loadCompetences(NodeList business) {
		Vector<Competence> competences = new Vector<Competence>();
		for (int i = 0; i < business.getLength(); i++) {
			Node businessNode = business.item(i);
			Element businessElement = (Element) businessNode;

			Competence competence = new Competence(businessElement.getAttribute("name"));
			competence.setType(businessElement.getAttribute("type"));

			if ( !businessElement.getAttribute("stock").equals("") ) {
				competence.setStock(Integer.parseInt(businessElement.getAttribute("stock")));
			}

			setAttributes(competence, businessElement.getElementsByTagName("attribute"));

			competences.add(competence);
		}
		return competences;
	}
	
	private static void setAttributes(Item item, NodeList attributesList) {
		
		for (int t = 0; t < attributesList.getLength(); t++) {
			Node attributeNode = attributesList.item(t);
			Element attributeElement = (Element) attributeNode;

			Attribute attribute = new Attribute(attributeElement.getAttribute("name"));
			String attributeType = normalizeType(attributeElement.getAttribute("type"));

			//discrete domain
			if(attributeType.equals("string") || attributeType.equals("boolean")) {   // FIXME: should be for every discrete domain
				NodeList valuesList = attributeElement.getElementsByTagName("value");
				for (int x = 0; x < valuesList.getLength(); x++) {
					Node valueNode = valuesList.item(x);
					attribute.getDiscreteDomain().add(new String(((Element) valueNode).getTextContent()));
				}						
				attribute.setPreferredValue(attributeElement.getElementsByTagName("prefvalue").item(0).getTextContent());
			}
			//continuous domain
			if(attributeType.equals("integer") || attributeType.equals("float")) {   // FIXME: should be for every continuous domain
				attribute.setContinuousDomainMin(String.valueOf(attributeElement.getElementsByTagName("minvalue").item(0).getTextContent()));
				attribute.setContinuousDomainMax(String.valueOf(attributeElement.getElementsByTagName("maxvalue").item(0).getTextContent()));
				attribute.setPreferredValue(attributeElement.getElementsByTagName("prefvalue").item(0).getTextContent());
			}

			attribute.setType(attributeType);
			item.addAttribute(attribute);
		}
	}
	
	// normalize a type 
	private static String normalizeType(String type) {
		type = type.toLowerCase();
		if(type.startsWith("string")) {
			return "string";
		} else if(type.startsWith("float")) {
			return "float";
		} else if(type.startsWith("int")) {
			return "integer";
		} else if(type.startsWith("bool")) {
			return "boolean";
		}
		return "";
	}
	
}

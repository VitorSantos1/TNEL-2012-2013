package ei.contract;

import ei.contract.xml.Contract;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 * This class wraps an XML Contract and includes auxiliary methods for handling it.
 * It also includes several methods for marshaling/unmarshaling the wrapped Contract object.
 * 
 * @author hlc
 */
public class ContractWrapper {
	
	private Contract contract = null;
	private String xsd_file;
	
	/**
	 * Constructor setting the XML schema definition file (eventually null).
	 */
	public ContractWrapper(String xsd_file) {
		this.xsd_file = xsd_file;
	}
	
	/**
	 * Constructor that sets the Contract wrapped by this object.
	 * 
	 * @param contract	The Contract to be wrapped
	 * @param xsd_file	The Contract xml schema definition
	 */
	public ContractWrapper(Contract contract, String xsd_file) {
		this.contract = contract;
		this.xsd_file = xsd_file;
	}
	
	/**
	 * Equality between contracts. This method overrides Object.equals()
	 */
	public boolean equals(Object obj) {
		// TODO - for now I am just comparing the contracts' dates...
		if(((ContractWrapper) obj).getDate().equals(this.getDate()))
			return true;
		else
			return false;
	}
	
	/**
	 * Gets the wrapped Contract.
	 * 
	 * @return	The wrapped <code>Contract</code>
	 */
	public Contract getContract() {
		return contract;
	}
	
	public void setId(String id) {
		contract.getHeader().setId(id);
	}
	
	public String getId() {
		return contract.getHeader().getId();
	}
	
	private String getDate() {
		return contract.getHeader().getWhen().toString();
	}
	
	public String getType() {
		return contract.getHeader().getType();
	}
	
	public String getSuper() {
		return contract.getHeader().getSuper();
	}
	
	/**
	 * Creates a marshaller.
	 * 
	 * @param withSchemaValidation	Specifies whether schema validation should be performed when marshaling with the returned marshaller
	 * @return						The marshaller
	 */
	private Marshaller createMarshaller(boolean withSchemaValidation) {
		try {
			// create marshaller
			JAXBContext jaxbContext = JAXBContext.newInstance("ei.contract.xml");
			Marshaller marshaller = jaxbContext.createMarshaller();
			// is schema validation required?
			if(withSchemaValidation) {
				// set schema
				try {
					Schema schema = SchemaFactory.newInstance(javax.xml.XMLConstants.W3C_XML_SCHEMA_NS_URI).newSchema(new java.io.File(xsd_file));
					marshaller.setSchema(schema);
				} catch(SAXException saxe) {
					saxe.printStackTrace();
					return null;
				}
				// set no-namespace-schema-location property
				try {
					marshaller.setProperty(javax.xml.bind.Marshaller.JAXB_NO_NAMESPACE_SCHEMA_LOCATION, xsd_file);
				} catch(javax.xml.bind.PropertyException pe) {
					pe.printStackTrace();
				}
			}
			return marshaller;
		} catch(javax.xml.bind.JAXBException jaxbe) {
			jaxbe.printStackTrace();
			return null;
		}
	}
	
	/**
	 * Creates an unmarshaller.
	 * 
	 * @param withSchemaValidation	Specifies whether schema validation should be performed when unmarshaling with the returned unmarshaller
	 * @return						The unmarshaller
	 */
	private Unmarshaller createUnmarshaller(boolean withSchemaValidation) {
		try {
			// create unmarshaller
			JAXBContext jaxbContext = JAXBContext.newInstance("ei.contract.xml");
			Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
			// is schema validation required?
			if(withSchemaValidation) {
				// set schema
				try {
					Schema schema = SchemaFactory.newInstance(javax.xml.XMLConstants.W3C_XML_SCHEMA_NS_URI).newSchema(new java.io.File(xsd_file));
					unmarshaller.setSchema(schema);
				} catch(SAXException saxe) {
					saxe.printStackTrace();
				}
			}
			return unmarshaller;
		} catch(JAXBException jaxbe) {
			jaxbe.printStackTrace();
			return null;
		}
	}
	
	/**
	 * Marshals the XML Contract to a String.
	 * 
	 * @param withSchemaValidation	Specifies whether schema validation should be performed
	 * @return						The XML Contract as a String
	 */
	public String marshal(boolean withSchemaValidation) {
		Marshaller marshaller = createMarshaller(withSchemaValidation);
		// marshal
		try {
			StringWriter sw = new StringWriter();
			marshaller.marshal(contract, sw);
			return sw.toString();
		} catch(javax.xml.bind.JAXBException jaxbe) {
			jaxbe.printStackTrace();
			return null;
		}
	}
	
	/**
	 * Marshals the XML Contract to a Document.
	 * 
	 * @param withSchemaValidation	Specifies whether schema validation should be performed
	 * @return						The XML Contract as a Document
	 */
	public Document marshalToDocument(boolean withSchemaValidation) {
		Marshaller marshaller = createMarshaller(withSchemaValidation);
		// marshal
		try {
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			dbf.setNamespaceAware(true);
			DocumentBuilder db = dbf.newDocumentBuilder();
			Document doc = db.newDocument();
			marshaller.marshal(contract, doc);
			return doc;
		} catch(JAXBException jaxbe) {
			jaxbe.printStackTrace();
			return null;
		} catch(ParserConfigurationException pce) {
			pce.printStackTrace();
			return null;
		}
	}
	
	/**
	 * Saves (marshals) the XML Contract to a file.
	 * 
	 * @param filename				The name of the file to save to
	 * @param withSchemaValidation	Specifies whether schema validation should be performed
	 */
	public void save(String filename, boolean withSchemaValidation){
		Marshaller marshaller = createMarshaller(withSchemaValidation);
		// set formatted-output property
		try {
			marshaller.setProperty(javax.xml.bind.Marshaller.JAXB_FORMATTED_OUTPUT, new Boolean(true));
		} catch(javax.xml.bind.PropertyException pe) {
			pe.printStackTrace();
		}
		// marshal
		try {
			FileOutputStream fos = new FileOutputStream(filename);
			marshaller.marshal(contract, fos);
			fos.close();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		} catch(JAXBException jaxbe) {
			jaxbe.printStackTrace();
		}
	}
	
	/**
	 * Unmarshals the XML Contract from a String.
	 * 
	 * @param s						The String from which to unmarshal
	 * @param withSchemaValidation	Specifies whether schema validation should be performed
	 */
	public void unmarshal(String s, boolean withSchemaValidation) {
		Unmarshaller unmarshaller  = createUnmarshaller(withSchemaValidation);
		// unmarshal
		try {
			contract = (Contract) unmarshaller.unmarshal(new StringReader(s));
		} catch(JAXBException jaxbe) {
			jaxbe.printStackTrace();
		}
	}
	
}

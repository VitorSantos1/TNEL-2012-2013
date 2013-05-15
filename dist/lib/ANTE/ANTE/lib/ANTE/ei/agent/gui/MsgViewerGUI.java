
package ei.agent.gui;

import javax.swing.JFrame;

import javax.swing.*;

import jade.content.ContentElement;
import jade.content.lang.Codec;
import jade.content.onto.OntologyException;
import jade.core.Agent;
import jade.lang.acl.*;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Point;
import javax.swing.SwingConstants;
import javax.swing.JTextField;

/**
 * A GUI to display ACLMessage information
 */
public class MsgViewerGUI extends JFrame {

	private static final long serialVersionUID = -3354743911593140811L;

	private javax.swing.JPanel jContentPane = null;

	private JLabel jLabel = null;
	private JLabel jLabel1 = null;
	private JLabel jLabel2 = null;
	private JTextField sender_field = null;
	private JTextArea content_field = null;
	private JTextField performative_field = null;
	private ACLMessage msg;
	
	private JLabel jLabel3 = null;
	private JTextField convid_field = null;
	private JLabel jLabel4 = null;
	private JLabel jLabel5 = null;
	private JLabel jLabel6 = null;
	private JTextField onto_field = null;
	private JTextField proto_field = null;
	private JTextField lang_field = null;
	private Agent owner;
	private Component caller;

	public MsgViewerGUI(ACLMessage msg, Agent owner, Component caller) {
		super();
		
		this.msg = msg;
		this.owner = owner;
		this.caller = caller;

		initialize();
	}
	
	/**
	 * This method initializes this
	 * 
	 * @return void
	 */
	private void initialize() {
		this.setResizable(false);
		this.setSize(315, 312);
		this.setContentPane(getJContentPane());
		this.setTitle("Message Viewer");
		this.setLocationRelativeTo(caller);
		
		sender_field.setText(msg.getSender().getLocalName());
		try {
			// extract content
			ContentElement ce = owner.getContentManager().extractContent(msg);
			content_field.setText("" + ce);
		} catch(Codec.CodecException cex) {
			//cex.printStackTrace();
			// this msg viewer is being used for different kinds of messages: getContent() if not using a class-based ontology
			content_field.setText(msg.getContent());
		} catch(OntologyException oe) {
			//oe.printStackTrace();
			// this msg viewer is being used for different kinds of messages: getContent() if not using a class-based ontology
			content_field.setText(msg.getContent());
		}
		convid_field.setText(msg.getConversationId());
		onto_field.setText(msg.getOntology());
		proto_field.setText(msg.getProtocol());
		lang_field.setText(msg.getLanguage());
		if(msg.getPerformative() == ACLMessage.ACCEPT_PROPOSAL){
			performative_field.setText("ACCEPT_PROPOSAL");
		}
		else if(msg.getPerformative() == ACLMessage.AGREE){
			performative_field.setText("AGREE");
		}
		else if(msg.getPerformative() == ACLMessage.CFP){
			performative_field.setText("CFP");
		}
		else if(msg.getPerformative() == ACLMessage.FAILURE){
			performative_field.setText("FAILURE");
		}
		else if(msg.getPerformative() == ACLMessage.INFORM){
			performative_field.setText("INFORM");
		}
		else if(msg.getPerformative() == ACLMessage.PROPOSE){
			performative_field.setText("PROPOSE");
		}
		else if(msg.getPerformative() == ACLMessage.REQUEST){
			performative_field.setText("REQUEST");
		}
		else if(msg.getPerformative() == ACLMessage.REFUSE){
			performative_field.setText("REFUSE");
		}
		else if(msg.getPerformative() == ACLMessage.REJECT_PROPOSAL){
			performative_field.setText("REJECT_PROPOSAL");
		}
		else if(msg.getPerformative() == ACLMessage.NOT_UNDERSTOOD){
			performative_field.setText("NOT_UNDERSTOOD");
		}
	}
	/**
	 * This method initializes jContentPane
	 * 
	 * @return javax.swing.JPanel
	 */
	private javax.swing.JPanel getJContentPane() {
		if(jContentPane == null) {
			jLabel6 = new JLabel();
			jLabel5 = new JLabel();
			jLabel4 = new JLabel();
			jLabel3 = new JLabel();
			jLabel2 = new JLabel();
			jLabel1 = new JLabel();
			jLabel = new JLabel();
			jContentPane = new javax.swing.JPanel();
			jContentPane.setLayout(null);
			jLabel.setText("Sender:");
			jLabel.setPreferredSize(new Dimension(100, 20));
			jLabel.setHorizontalAlignment(SwingConstants.RIGHT);
			jLabel.setSize(new Dimension(100, 20));
			jLabel.setLocation(new Point(5, 5));
			jLabel1.setText("Content:");
			jLabel1.setHorizontalAlignment(SwingConstants.LEFT);
			jLabel1.setSize(new Dimension(100, 20));
			jLabel1.setLocation(new Point(5, 105));
			jLabel2.setText("Performative:");
			jLabel2.setPreferredSize(new Dimension(100, 20));
			jLabel2.setHorizontalAlignment(SwingConstants.RIGHT);
			jLabel2.setSize(new Dimension(100, 20));
			jLabel2.setLocation(new Point(5, 30));
			jLabel3.setText("Conversation ID:");
			jLabel3.setPreferredSize(new Dimension(100, 20));
			jLabel3.setHorizontalAlignment(SwingConstants.RIGHT);
			jLabel3.setSize(new Dimension(100, 20));
			jLabel3.setLocation(new Point(5, 80));
			jLabel4.setText("Ontology:");
			jLabel4.setPreferredSize(new Dimension(100, 20));
			jLabel4.setHorizontalAlignment(SwingConstants.RIGHT);
			jLabel4.setSize(new Dimension(100, 20));
			jLabel4.setLocation(new Point(5, 260));
			jLabel5.setText("Protocol:");
			jLabel5.setPreferredSize(new Dimension(100, 20));
			jLabel5.setHorizontalAlignment(SwingConstants.RIGHT);
			jLabel5.setSize(new Dimension(100, 20));
			jLabel5.setLocation(new Point(5, 55));
			jLabel6.setText("Language:");
			jLabel6.setPreferredSize(new Dimension(100, 20));
			jLabel6.setHorizontalAlignment(SwingConstants.RIGHT);
			jLabel6.setSize(new Dimension(100, 20));
			jLabel6.setLocation(new Point(5, 235));
			jContentPane.add(jLabel, null);
			jContentPane.add(jLabel1, null);
			jContentPane.add(jLabel2, null);
			jContentPane.add(getSender_field(), null);
			
			jContentPane.add(getContent_field(), null);
			
			jContentPane.add(getPerformative_field(), null);
			jContentPane.add(jLabel3, null);
			jContentPane.add(getConvid_field(), null);
			jContentPane.add(jLabel4, null);
			jContentPane.add(jLabel5, null);
			jContentPane.add(jLabel6, null);
			jContentPane.add(getOnto_field(), null);
			jContentPane.add(getProto_field(), null);
			jContentPane.add(getLang_field(), null);
		}
		return jContentPane;
	}
	/**
	 * This method initializes jTextField	
	 * 	
	 * @return javax.swing.JTextField	
	 */    
	private JTextField getSender_field() {
		if (sender_field == null) {
			sender_field = new JTextField();
			sender_field.setEnabled(true);
			sender_field.setLocation(new Point(105, 5));
			sender_field.setSize(new Dimension(200, 20));
			sender_field.setPreferredSize(new Dimension(200, 20));
			sender_field.setEditable(false);
		}
		return sender_field;
	}
	/**
	 * This method initializes jTextField1	
	 * 	
	 * @return javax.swing.JTextField	
	 */    
	private JTextArea getContent_field() {
		if (content_field == null) {
			content_field = new JTextArea();
			content_field.setSize(300, 100);
			content_field.setPreferredSize(new Dimension(300, 100));
			content_field.setLocation(new Point(5, 130));
			content_field.setLineWrap(true);
			content_field.setEditable(false);
		}
		return content_field;
	}
	/**
	 * This method initializes jTextField2	
	 * 	
	 * @return javax.swing.JTextField	
	 */    
	private JTextField getPerformative_field() {
		if (performative_field == null) {
			performative_field = new JTextField();
			performative_field.setSize(200, 20);
			performative_field.setEnabled(true);
			performative_field.setPreferredSize(new Dimension(200, 20));
			performative_field.setLocation(new Point(105, 30));
			performative_field.setEditable(false);
		}
		return performative_field;
	}
	/**
	 * This method initializes jTextField	
	 * 	
	 * @return javax.swing.JTextField	
	 */    
	private JTextField getConvid_field() {
		if (convid_field == null) {
			convid_field = new JTextField();
			convid_field.setEditable(false);
			convid_field.setSize(new Dimension(200, 20));
			convid_field.setPreferredSize(new Dimension(200, 20));
			convid_field.setLocation(new Point(105, 80));
		}
		return convid_field;
	}
	/**
	 * This method initializes jTextField	
	 * 	
	 * @return javax.swing.JTextField	
	 */    
	private JTextField getOnto_field() {
		if (onto_field == null) {
			onto_field = new JTextField();
			onto_field.setEditable(false);
			onto_field.setSize(new Dimension(200, 20));
			onto_field.setPreferredSize(new Dimension(200, 20));
			onto_field.setLocation(new Point(105, 260));
		}
		return onto_field;
	}
	/**
	 * This method initializes jTextField1	
	 * 	
	 * @return javax.swing.JTextField	
	 */    
	private JTextField getProto_field() {
		if (proto_field == null) {
			proto_field = new JTextField();
			proto_field.setEditable(false);
			proto_field.setSize(new Dimension(200, 20));
			proto_field.setPreferredSize(new Dimension(200, 20));
			proto_field.setLocation(new Point(105, 55));
		}
		return proto_field;
	}
	/**
	 * This method initializes jTextField2	
	 * 	
	 * @return javax.swing.JTextField	
	 */    
	private JTextField getLang_field() {
		if (lang_field == null) {
			lang_field = new JTextField();
			lang_field.setEditable(false);
			lang_field.setSize(new Dimension(200, 20));
			lang_field.setPreferredSize(new Dimension(200, 20));
			lang_field.setLocation(new Point(105, 235));
		}
		return lang_field;
	}
}  //  @jve:decl-index=0:visual-constraint="10,10"

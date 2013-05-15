package ei.agent.gui;

import jade.core.AID;
import jade.lang.acl.ACLMessage;
import jade.proto.SubscriptionInitiator;
import jade.core.Agent;
import jade.domain.AMSService;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.AMSAgentDescription;
import jade.domain.FIPAAgentManagement.DFAgentDescription;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JButton;
import javax.swing.JScrollPane;

import java.awt.Color;
import java.awt.Rectangle;
import java.util.TimerTask;
import java.util.Vector;
import javax.swing.JList;
import javax.swing.JComboBox;
import javax.swing.JTextField;

import ei.ElectronicInstitution;

public class MessagesGUI extends JPanel {

	private static final long serialVersionUID = 1L;
	
	private JButton sendMessageButton = null;
	private JLabel label;
	private DefaultListModel listModel;  //  @jve:decl-index=0:visual-constraint="5,12"
	private JList list;
	private JScrollPane scrollpane;
	private Vector<ACLMessage> aclmsg;
	private JLabel ontologyLabel = null;
	private JLabel protocolLabel = null;
	private JLabel languageLabel = null;
	private JLabel contentLabel = null;
	private JLabel performativeLabel = null;
	private JLabel toLabel = null;
	private JLabel infoLabel = null;
	private JLabel messageLabel = null;
	private JComboBox ontologyComboBox = null;
	private JComboBox protocolComboBox = null;
	private JComboBox languageComboBox = null;
	private JComboBox performativeComboBox = null;
	private JComboBox toComboBox = null;
	private JTextField contentTextField = null;
	private Agent owner;
	
	/**
	 * This is the default constructor
	 */
	public MessagesGUI(Agent owner) {
		super();
		
		this.owner = owner;
		
		initialize();
		
		owner.addBehaviour(new SubscribeDFInit(owner));
	}

	/**
	 * Returns 'this' object
	 * 
	 * @return	A reference to this object
	 */
	private MessagesGUI getThis() {
		return this;
	}

	protected Agent getAgent() {
		return owner;
	}
	
	
	/**
	 * Fill the textfields and comboboxes according to the received parameters
	 * 
	 * @param to	the receiver
	 * @param perf	the performative
	 * @param ont	the ontology
	 * @param proto	the protocol
	 */
	public void fillAll(String to, String perf, String ont, String proto){
		ontologyComboBox.setSelectedItem(ont);
		protocolComboBox.setSelectedItem(proto);
		performativeComboBox.setSelectedItem(perf);
		toComboBox.setSelectedItem(to);
	}
	
	/**
	 * This method initializes this
	 * 
	 * @return void
	 */
	private void initialize() {
		aclmsg = new Vector<ACLMessage>();
		label = new JLabel();
		listModel = new DefaultListModel();
		scrollpane = new JScrollPane(getJList());
		scrollpane.setBounds(new Rectangle(10, 30, 470, 104));
		scrollpane.setViewportView(getJList());
		label.setText("Received Messages:");
		label.setBounds(new Rectangle(10, 10, 117, 16));
		this.setLayout(null);
		
		messageLabel = new JLabel();
		messageLabel.setText("Message:");
		messageLabel.setBounds(new Rectangle(10, 145, 62, 16));
		infoLabel = new JLabel();
		infoLabel.setText(" ");
		infoLabel.setBounds(new Rectangle(10, 285, 53, 16));
		toLabel = new JLabel();
		toLabel.setText("To:");
		toLabel.setBounds(new Rectangle(10, 175, 25, 16));
		performativeLabel = new JLabel();
		performativeLabel.setText("Performative:");
		performativeLabel.setBounds(new Rectangle(10, 200, 87, 16));
		contentLabel = new JLabel();
		contentLabel.setText("Content:");
		contentLabel.setBounds(new Rectangle(10, 253, 64, 17));
		languageLabel = new JLabel();
		languageLabel.setText("Language:");
		languageLabel.setBounds(new Rectangle(250, 174, 69, 18));
		protocolLabel = new JLabel();
		protocolLabel.setText("Protocol:");
		protocolLabel.setBounds(new Rectangle(250, 199, 53, 16));
		ontologyLabel = new JLabel();
		ontologyLabel.setText("Ontology:");
		ontologyLabel.setBounds(new Rectangle(10, 223, 58, 18));
		
		this.setSize(487, 318);
		this.add(getOntologyComboBox(), null);
		this.add(label, null);
		this.add(scrollpane, null);
		this.add(messageLabel, null);
		this.add(infoLabel, null);
		this.add(toLabel, null);
		this.add(performativeLabel, null);
		this.add(contentLabel, null);
		this.add(languageLabel, null);
		this.add(protocolLabel, null);
		this.add(ontologyLabel, null);
		this.add(getContentTextField(), null);
		this.add(getToComboBox(), null);
		this.add(getPerformativeComboBox(), null);
		this.add(getLanguageComboBox(), null);
		this.add(getProtocolComboBox(), null);
		this.add(getSendMessageButton(), null);
		
	}

	/**
	 * @author hlc
	 * <p>
	 * Inner class that allows the normative environment to subscribe the DF for updates on existing agents and their services (roles).
	 * <br>
	 * Implemented as a subscription initiator role.
	 */
	protected class SubscribeDFInit extends SubscriptionInitiator {
		
		private static final long serialVersionUID = -1903894536392366373L;

		/**
		 * Creates a <code>SubscribeDFInit</code> instance
		 *
		 * @param agent The agent that adds the behaviour.
		 */
		SubscribeDFInit(Agent agent) {
			super(agent, DFService.createSubscriptionMessage(agent, agent.getDefaultDF(), new DFAgentDescription(), null));
		}
		
		/**
		 * Handles an inform message from the DF.
		 */
		protected void handleInform(ACLMessage inform) {
			// decode the message
			try {
				DFAgentDescription[] dfds = DFService.decodeNotification(inform.getContent());
				
				boolean exists;
				
				for(int i=0;i<dfds.length;i++){
					exists = false;
					if(!dfds[i].getAllServices().hasNext()){
						toComboBox.removeItem(dfds[i].getName().getLocalName());
					}
					else{
						for(int j=0;j<toComboBox.getItemCount();j++){
							if(toComboBox.getItemAt(j).equals(dfds[i].getName().getName())){
								exists = true;
							}
						}
						if(exists == false){
							toComboBox.removeItem(dfds[i].getName().getLocalName());
							toComboBox.addItem(dfds[i].getName().getLocalName());
						}
					}
				}
				
				reOrder();
			} catch (FIPAException fe) {
				fe.printStackTrace();
			}
		}
		
	}
	
	//reorder the agents name alfabetically on toComboBox
	private void reOrder(){
		String[] str = new String[toComboBox.getItemCount()];
		for(int i=0;i<toComboBox.getItemCount();i++){
			str[i] = toComboBox.getItemAt(i).toString();
		}
		java.util.Arrays.sort(str);
		toComboBox.removeAllItems();
		for(int i=0;i<str.length;i++){
			toComboBox.addItem(str[i]);
		}
	}
	
	/**
	 * Searches the AMS for all agents on the platform
	 * 
	 * @return	a vector with the agents on the AMS
	 */
	private Vector<String> SearchAMS(){
		
		Vector<String> list = new Vector<String>();
		AMSAgentDescription aad = new AMSAgentDescription();
		try{
			AMSAgentDescription[] result = AMSService.search(owner,aad);
			for(int i=0;i<result.length;i++){
				list.add(result[i].getName().getLocalName());
			}
		}
		catch(FIPAException fe){
			fe.printStackTrace();
		}
		
		return list;
	}
	
	/**
	 * ends the timer
	 */
	public class RemindTask extends TimerTask {
	 	
 		java.util.Timer timer;
 	
 		public RemindTask(java.util.Timer _timer){
 			timer = _timer;
 		}
 	
        public void run() {
        	infoLabel.setForeground(Color.LIGHT_GRAY);
        	infoLabel.setText(" ");
            timer.cancel(); //Terminate the timer thread
        }
    }
	
	/**
	 * Creates a message that disappears after the designated amount of seconds
	 * 
	 * @param seconds	the number of seconds for the timer to count
	 */
	public void newLabel(int seconds, String text){
		infoLabel.setText(text);
		infoLabel.setForeground(Color.BLACK);
		java.util.Timer timer = new java.util.Timer();
		timer.schedule(new RemindTask(timer), seconds*1000);
	}
	
	/**
	 * This method initializes sendMessageButton	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getSendMessageButton() {
		if (sendMessageButton == null) {
			sendMessageButton = new JButton();
			sendMessageButton.setText("Send Message");
			sendMessageButton.setBounds(new Rectangle(360, 280, 121, 27));
			sendMessageButton.addMouseListener(new java.awt.event.MouseAdapter() {
				public void mouseReleased(java.awt.event.MouseEvent e) {
					if(!(toComboBox.getSelectedItem().equals(null) || contentTextField.getText().equals(null))){
						ACLMessage msg = new ACLMessage(ACLMessage.getInteger(performativeComboBox.getSelectedItem().toString()));
						msg.addReceiver(new AID(toComboBox.getSelectedItem().toString(),false));
						msg.setContent(contentTextField.getText());
						msg.setOntology(ontologyComboBox.getSelectedItem().toString());
						msg.setProtocol(protocolComboBox.getSelectedItem().toString());
						msg.setLanguage(languageComboBox.getSelectedItem().toString());
						owner.send(msg);
						newLabel(3,"Sent!");
					}
					else{
						newLabel(3,"Failed!");
					}
				}
			});
		}
		return sendMessageButton;
	}

	/**
	 * Adds a message to the list
	 * 
	 * @param msg	the message to add
	 */
	public void addMsg(ACLMessage msg){
		aclmsg.addElement(msg);
		if(msg.hasByteSequenceContent()){
			listModel.addElement(msg.getSender().getLocalName()+" --> OBJECT CONTENT");
		}
		else{
			listModel.addElement(msg.getSender().getLocalName()+" --> "+msg.getContent());
		}
	}
	
	/**
	 * Inserts a new message into the list
	 * 
	 * @param rec		the message to insert 
	 */
	public void insertMsg(ACLMessage rec){
		aclmsg.add(rec);
		if(rec.hasByteSequenceContent()){
			listModel.addElement(rec.getSender().getName()+" --> OBJECT CONTENT");
		}
		else{
			listModel.addElement(rec.getSender().getName()+" --> "+rec.getContent());
		}
	}
	
	private JList getJList() {
		if (list == null) {
			list = new JList(listModel);
			list.setBorder(BorderFactory.createLoweredBevelBorder());
			list.setVisible(true);
			list.addMouseListener(new java.awt.event.MouseAdapter() { 
				public void mouseReleased(java.awt.event.MouseEvent e) {    
					if(e.getClickCount() == 2){
						if(!list.isSelectionEmpty()){
							MsgViewerGUI tmp = new MsgViewerGUI((ACLMessage) aclmsg.get(list.getSelectedIndex()), owner, getThis());
							tmp.setVisible(true);
						}
					}
				}
			});
		}
		return list;
	}

	/**
	 * This method initializes ontologyComboBox	
	 * 	
	 * @return javax.swing.JComboBox	
	 */
	private JComboBox getOntologyComboBox() {
		if (ontologyComboBox == null) {
			ontologyComboBox = new JComboBox();
			ontologyComboBox.setEditable(false);
			ontologyComboBox.setBounds(new Rectangle(100, 223, 139, 19));

			String[] aux = ElectronicInstitution.getAllOntologies();
			for(int i=0;i<aux.length;i++){
				ontologyComboBox.addItem(aux[i]);
			}
		}
		return ontologyComboBox;
	}

	/**
	 * This method initializes protocolComboBox	
	 * 	
	 * @return javax.swing.JComboBox	
	 */
	private JComboBox getProtocolComboBox() {
		if (protocolComboBox == null) {
			protocolComboBox = new JComboBox();
			protocolComboBox.setBounds(new Rectangle(341, 197, 139, 19));
			
			protocolComboBox.addItem("FIPA-Brokering");
			protocolComboBox.addItem("FIPA-Contract-Net");
			protocolComboBox.addItem("FIPA-Dutch-Auction");
			protocolComboBox.addItem("FIPA-English-Auction");
			protocolComboBox.addItem("FIPA-Iterated-Contract-Net");
			protocolComboBox.addItem("FIPA-Propose");
			protocolComboBox.addItem("FIPA-Query");
			protocolComboBox.addItem("FIPA-Recruiting");
			protocolComboBox.addItem("FIPA-Request");
			protocolComboBox.addItem("FIPA-Request-When");
			protocolComboBox.addItem("FIPA-Subscribe");
			protocolComboBox.addItem("qf-negotiation");
			protocolComboBox.setSelectedItem("FIPA-Request");
		}
		return protocolComboBox;
	}

	/**
	 * This method initializes languageComboBox	
	 * 	
	 * @return javax.swing.JComboBox	
	 */
	private JComboBox getLanguageComboBox() {
		if (languageComboBox == null) {
			languageComboBox = new JComboBox();
			languageComboBox.setBounds(new Rectangle(341, 172, 139, 19));

			languageComboBox.addItem("FIPA-SL");
			languageComboBox.addItem("");
		}
		return languageComboBox;
	}

	/**
	 * This method initializes performativeComboBox	
	 * 	
	 * @return javax.swing.JComboBox	
	 */
	private JComboBox getPerformativeComboBox() {
		if (performativeComboBox == null) {
			performativeComboBox = new JComboBox();
			performativeComboBox.setBounds(new Rectangle(100, 198, 139, 19));

			for(int i=0;i<ACLMessage.getAllPerformativeNames().length;i++){
				performativeComboBox.addItem(ACLMessage.getPerformative(i));
			}
			performativeComboBox.setSelectedItem("REQUEST");
			
		}
		return performativeComboBox;
	}

	/**
	 * This method initializes toComboBox	
	 * 	
	 * @return javax.swing.JComboBox	
	 */
	private JComboBox getToComboBox() {
		if (toComboBox == null) {
			toComboBox = new JComboBox();
			toComboBox.setBounds(new Rectangle(100, 172, 139, 19));

			//search ams (all agents)
			Vector<String> list = SearchAMS();
			for(int i=0;i<list.size();i++){
				toComboBox.addItem(list.get(i));
			}
		}
		return toComboBox;
	}

	/**
	 * This method initializes contentTextField	
	 * 	
	 * @return javax.swing.JTextField	
	 */
	private JTextField getContentTextField() {
		if (contentTextField == null) {
			contentTextField = new JTextField();
			contentTextField.setBounds(new Rectangle(100, 248, 382, 23));
		}
		return contentTextField;
	}
	
}  //  @jve:decl-index=0:visual-constraint="14,0"

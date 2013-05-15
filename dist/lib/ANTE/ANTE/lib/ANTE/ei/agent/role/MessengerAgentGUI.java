package ei.agent.role;

import java.awt.Rectangle;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;

import jade.core.AID;
import javax.swing.JButton;
import java.awt.Color;
import java.util.Enumeration;
import java.util.Vector;

import javax.swing.SwingConstants;

/**
 * GUI for the Messenger Agent
 */
public class MessengerAgentGUI extends JFrame {
	private static final long serialVersionUID = -559927840085234240L;

	private javax.swing.JPanel jContentPane = null;
	private MessengerAgent owner;
	private JScrollPane messagesScrollPane = null;
	private JTable messagesTable = null;
	private DefaultTableModel messageTableModel = null;

	private JButton killButton = null;
	
	/**
	 * This is the default constructor
	 */
	public MessengerAgentGUI(MessengerAgent owner) {
		super();
		this.owner = owner;
		initialize();
	}
	
	/**
	 * This method initializes this
	 * @return void
	 */
	private void initialize() {
		this.setResizable(false);
		this.setDefaultCloseOperation(javax.swing.WindowConstants.HIDE_ON_CLOSE);
		this.setSize(450, 232);
		this.setContentPane(getJContentPane());
		this.setTitle(owner.getLocalName()+" -- Messenger Agent");
		this.setLocationRelativeTo(null);	
		
		//updates messages 
		refreshMessages();		
	}
	
	/**
	 * This method initializes jContentPane
	 * @return javax.swing.JPanel
	 */
	private javax.swing.JPanel getJContentPane() {
		if(jContentPane == null) {
			jContentPane = new javax.swing.JPanel();
			jContentPane.setLayout(null);
			jContentPane.add(getBalancesScrollPane(),null);
			jContentPane.add(getKillButton(), null);
		}
		return jContentPane;
	}
	
	/**
	 * This method initializes the messages ScrollPane
	 * @return javax.swing.JScrollPane	
	 */
	private JScrollPane getBalancesScrollPane() {
		if (messagesScrollPane == null) {
			messagesScrollPane = new JScrollPane();
			messagesScrollPane.setBounds(new Rectangle(3, 5, 440, 173));
			messagesScrollPane.setViewportView(getBalancesTable());
		}
		return messagesScrollPane;
	}	
	
	private void refreshMessages() {
		Enumeration<String> msgs = owner.messages.keys();
		while(msgs.hasMoreElements()) {
			String context = msgs.nextElement();
			Vector<Object> t = owner.messages.get(context);
			addMessageRow((AID) t.get(0),  (AID) t.get(1), context, (String) t.get(2));
		}
	}

	/**
	 * Adds a new message to the GUI
	 * @param from				the aid for the agent who sent the message
	 * @param to				the aid for the agent who received the message
	 * @param context			the context
	 * @param msg				the message sent
	 */
	protected void addMessageRow(AID from, AID to, String context, String msg){
		((DefaultTableModel) messagesTable.getModel()).addRow(new String [] {from.getLocalName(), to.getLocalName(), context, msg});
	}
	
	/**
	 * This method initializes the messages table	
	 * @return javax.swing.JTable	
	 */
	private JTable getBalancesTable() {
		if (messagesTable == null) { 
			messagesTable = new JTable();
			messageTableModel =  new DefaultTableModel(null, new String [] {"From", "To", "Context", "Message"}) {  
				private static final long serialVersionUID = 1L;
				public boolean isCellEditable(int row, int col) {  
					return false;  
				}  
			};
			messagesTable.setModel(messageTableModel);
			messagesTable.setAutoCreateRowSorter(true);
			messagesTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		}
		return messagesTable;
	}
	
	/**
	 * This method initializes killButton	
	 * @return javax.swing.JButton	
	 */
	private JButton getKillButton() {
		if (killButton == null) {
			killButton = new JButton();
			killButton.setBounds(new Rectangle(396, 179, 46, 18));
			killButton.setHorizontalAlignment(SwingConstants.CENTER);
			killButton.setText("X");
			killButton.setForeground(Color.red);
			killButton.addMouseListener(new java.awt.event.MouseAdapter() { 
				public void mouseReleased(java.awt.event.MouseEvent e) {
					Object[] options = { "OK", "CANCEL" };
					int answer = JOptionPane.showOptionDialog(null, "Agent " + owner.getLocalName() + " will be killed. Click OK to continue",
							"Kill Warning", JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE, null, options, options[0]);
					if (answer == 0) {
						owner.doDelete();
						dispose();
					}
				}
			});
		}
		return killButton;
	}
} 

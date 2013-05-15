
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
 * GUI for the Delivery Tracker Agent
 */
public class DeliveryTrackerAgentGUI extends JFrame {

	private static final long serialVersionUID = -559927840085234240L;

	private javax.swing.JPanel jContentPane = null;
	private DeliveryTrackerAgent owner;
	private JScrollPane transactionsScrollPane = null;
	private JTable transactionsTable = null;
	private DefaultTableModel transactionTableModel = null;

	private JButton killButton = null;
	
	/**
	 * This is the default constructor
	 */
	public DeliveryTrackerAgentGUI(DeliveryTrackerAgent owner) {
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
		this.setTitle(owner.getLocalName()+" -- Delivery Tracker Agent");
		this.setLocationRelativeTo(null);	
		
		
		//updates transactions
		refreshTransactions();				
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
	 * This method initializes the transactions ScrollPane
	 * @return javax.swing.JScrollPane	
	 */
	private JScrollPane getBalancesScrollPane() {
		if (transactionsScrollPane == null) {
			transactionsScrollPane = new JScrollPane();
			transactionsScrollPane.setBounds(new Rectangle(3, 5, 440, 173));
			transactionsScrollPane.setViewportView(getBalancesTable());
		}
		return transactionsScrollPane;
	}
	
	private void refreshTransactions() {
		Enumeration<String> trans = owner.transactions.keys();
		while(trans.hasMoreElements()) {
			String context = trans.nextElement();
			Vector<Object> t = owner.transactions.get(context);
			addTransactionRow( (AID) t.get(0),  (AID) t.get(1), context, (String) t.get(2), (Integer) t.get(3));
		}
	}
	
	/**
	 * Adds a new transaction to the GUI
	 * @param from				the aid for the agent who sent the items
	 * @param to				the aid for the agent who received the items
	 * @param context			the context
	 * @param item				the item being transactioned
	 * @param quantity			the quantity transactioned
	 */
	public void addTransactionRow(AID from, AID to, String context, String item, Integer quantity){
		((DefaultTableModel) transactionsTable.getModel()).addRow(new String [] {from.getLocalName(), to.getLocalName(), context, item, quantity.toString()});
	}
	
	/**
	 * This method initializes the transactions table	
	 * @return javax.swing.JTable	
	 */
	private JTable getBalancesTable() {
		if (transactionsTable == null) {
			transactionsTable = new JTable();
			transactionTableModel =  new DefaultTableModel(null, new String [] {"From", "To", "Context", "Item", "Quantity"}) {  
				private static final long serialVersionUID = 1L;
				public boolean isCellEditable(int row, int col) {  
					return false;  
				}  
			};
			transactionsTable.setModel(transactionTableModel);
			transactionsTable.setAutoCreateRowSorter(true);
			transactionsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		}
		return transactionsTable;
	}
	
	/**
	 * This method initializes killButton	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getKillButton() {
		if (killButton == null) {
			killButton = new JButton();
			killButton.setBounds(new Rectangle(398, 180, 43, 16));
			killButton.setHorizontalAlignment(SwingConstants.CENTER);
			killButton.setText("X");
			killButton.setForeground(Color.red);
			killButton.addMouseListener(new java.awt.event.MouseAdapter() { 
				public void mouseReleased(java.awt.event.MouseEvent e) {
					Object[] options = { "OK", "CANCEL" };
					int answer = JOptionPane.showOptionDialog(null,"Agent " + owner.getLocalName() + " will be killed. Click OK to continue",
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

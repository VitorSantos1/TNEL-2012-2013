
package ei.agent.role;

import java.awt.Rectangle;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;

import jade.core.AID;
import javax.swing.JButton;
import java.awt.Color;
import java.text.DecimalFormat;
import java.util.Enumeration;
import java.util.Vector;
import javax.swing.SwingConstants;
import javax.swing.JPopupMenu;
import javax.swing.JMenuItem;

import java.awt.event.MouseListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.Point;

/**
 * GUI for the Bank Agent
 */
public class BankAgentGUI extends JFrame {

	private static final long serialVersionUID = -559927840085234240L;

	private javax.swing.JPanel jContentPane = null;
	private BankAgent owner;
	private JScrollPane balancesScrollPane = null;
	private JTable balancesTable = null;
	private DefaultTableModel balancesTableModel = null;

	private JButton killButton = null;
	private JPopupMenu popup = null;
	
	/**
	 * This is the default constructor
	 */
	public BankAgentGUI(BankAgent owner) {
		super();

		this.owner = owner;
		initialize();
	}
	
	/**
	 * This method initializes this
	 * 
	 * @return void
	 */
	private void initialize() {
		this.setResizable(false);
		this.setDefaultCloseOperation(javax.swing.WindowConstants.HIDE_ON_CLOSE);
		this.setSize(315, 234);
		this.setContentPane(getJContentPane());
		this.setTitle(owner.getLocalName()+" -- Bank Agent");
		this.setLocationRelativeTo(null);		

		//updates agent balances
		refreshBalances();		
	}
	
	/**
	 * This method initializes jContentPane
	 * @return JPanel
	 */
	private JPanel getJContentPane() {
		if(jContentPane == null) {
			jContentPane = new javax.swing.JPanel();
			jContentPane.setLayout(null);
			jContentPane.add(getBalancesScrollPane(),null);
			jContentPane.add(getKillButton(), null);

			popup = new JPopupMenu();
			JMenuItem menuItem = new JMenuItem("Agent Movements");
			JMenuItem menuItem2 = new JMenuItem("Edit Balance");
			menuItem.addActionListener(
					new ActionListener(){
						public void actionPerformed(ActionEvent ev){
							//open movements GUI
							if(balancesTable.getSelectedRow() != -1){
								String ag = (String) balancesTableModel.getValueAt(balancesTable.getSelectedRow(),0);
								//remember that there is a row sorter active that may influence the selected element...
								MovementGUI mv = new MovementGUI(ag);
								Vector<BankMovement> vec = owner.getMovements();
								//add every movement where the clicked agent was involved to the table
								for(int i=0;i<vec.size();i++){
									BankMovement bm = vec.get(i);
									if(ag.equals(bm.getFrom().getLocalName()) || ag.equals(bm.getTo().getLocalName())){
										mv.addMovement(bm.getFrom(), bm.getTo(), bm.getAmount());
									}
								}
								mv.setVisible(true);
							}
						}
					}   
			);
			menuItem2.addActionListener(
					new ActionListener(){
						public void actionPerformed(ActionEvent ev){
							//opens edit cell GUI
							String ag = (String) balancesTableModel.getValueAt(balancesTable.getSelectedRow(),0);
							Double value = owner.getAccounts().get(new AID(ag,false));
							EditAccountGUI eaGUI = new EditAccountGUI(getMyself(),owner,ag,value);
							eaGUI.setVisible(true);
						}
					}   
			);
			popup.add(menuItem);
			popup.add(menuItem2);
			MouseListener popupListener = new PopupListener();

			balancesTable.addMouseListener(popupListener);
		}
		return jContentPane;
	}
	
	/**
	 * This method initializes the balances ScrollPane
	 * @return JScrollPane	
	 */
	private JScrollPane getBalancesScrollPane() {
		if (balancesScrollPane == null) {
			balancesScrollPane = new JScrollPane();
			balancesScrollPane.setBounds(new Rectangle(3, 5, 302, 173));
			balancesScrollPane.setViewportView(getBalancesTable());
		}
		return balancesScrollPane;
	}
	
	/**
	 * Listener for the popup menu - shows the menu when the user right clicks any row on the table
	 */
	class PopupListener extends MouseAdapter {
	    public void mousePressed(MouseEvent e) {
	        maybeShowPopup(e);
	    }

	    public void mouseReleased(MouseEvent e) {
	        maybeShowPopup(e);
	    }

	    private void maybeShowPopup(MouseEvent e) {
	        if (e.isPopupTrigger()) {
				// get the coordinates of the mouse click
				Point p = e.getPoint();
				// get the row index that contains that coordinate
				int rowNumber = balancesTable.rowAtPoint( p );
				// Get the ListSelectionModel of the JTable
				ListSelectionModel model = balancesTable.getSelectionModel();
				// set the selected interval of rows. Using the "rowNumber"
				// variable for the beginning and end selects only that one row.
				model.setSelectionInterval( rowNumber, rowNumber );
	            popup.show(e.getComponent(), e.getX(), e.getY());
	        }
	    }
	}
	
	private BankAgentGUI getMyself(){
		return this;
	}
	
	private void refreshBalances() {
		Enumeration<AID> accounts = owner.accounts.keys();
		while(accounts.hasMoreElements()) {
			AID agent = accounts.nextElement();
			Double balance = owner.accounts.get(agent);
			updateBalanceRow(agent, balance);
		}
	}
	
	/**
	 * Updates the agents balances on the GUI
	 * @param aid				the agent aid
	 * @param balance			the new balance for the agent
	 */
	public void updateBalanceRow(AID aid, Double balance){
		DecimalFormat df = new DecimalFormat("##,##.##");

		DefaultTableModel tableModel = (DefaultTableModel) balancesTable.getModel();
		boolean isContainsAgentBalance = false;
		for(int i=0; i<balancesTableModel.getRowCount();i++){
			if(balancesTableModel.getValueAt(i, 0).toString().equals(aid.getLocalName().toString())){
				tableModel.setValueAt(df.format(balance), i, 1);
				isContainsAgentBalance = true;	
			}
		}
		
		if(!isContainsAgentBalance){
			tableModel.addRow(new String [] {aid.getLocalName(), df.format(balance)});
		} 
	}
	
	/**
	 * This method initializes the balances table	
	 * 	
	 * @return javax.swing.JTable	
	 */
	private JTable getBalancesTable() {
		if (balancesTable == null) {
			balancesTable = new JTable();
			balancesTableModel =  new DefaultTableModel(null, new String [] {"AID", "Balance"}) {  
				private static final long serialVersionUID = 1L;
				public boolean isCellEditable(int row, int col) {  
					return false;  
				}  
			};  			
			balancesTable.setModel(balancesTableModel);
			balancesTable.setAutoCreateRowSorter(true);
			balancesTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);				
			balancesTable.addMouseListener(new java.awt.event.MouseAdapter() {
				public void mouseClicked(java.awt.event.MouseEvent e) {
					if(e.getClickCount() == 2){
						String ag = (String) balancesTableModel.getValueAt(balancesTable.getSelectedRow(),0);
						//remember that there is a row sorter active that may influence the selected element...
						MovementGUI mv = new MovementGUI(ag);
						Vector<BankMovement> vec = owner.getMovements();
						//add every movement where the clicked agent was involved to the table
						for(int i=0;i<vec.size();i++){
							BankMovement bm = vec.get(i);
							if(ag.equals(bm.getFrom().getLocalName()) || ag.equals(bm.getTo().getLocalName())){
								mv.addMovement(bm.getFrom(), bm.getTo(), bm.getAmount());
							}
						}
						mv.setVisible(true);
						
					}
				}
			});
		}
		return balancesTable;
	}

	/**
	 * This method initializes killButton	
	 * @return JButton	
	 */
	private JButton getKillButton() {
		if (killButton == null) {
			killButton = new JButton();
			killButton.setBounds(new Rectangle(256, 180, 45, 17));
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

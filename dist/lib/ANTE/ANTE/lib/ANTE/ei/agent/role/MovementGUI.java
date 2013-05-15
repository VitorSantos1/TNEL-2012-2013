
package ei.agent.role;

import java.awt.Rectangle;
import java.text.DecimalFormat;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;

import jade.core.AID;

/**
 * GUI that shows every movement where a given agent was involved
 */
public class MovementGUI extends JFrame {

	private static final long serialVersionUID = -559927840085234240L;

	private javax.swing.JPanel jContentPane = null;
	private JScrollPane movementsScrollPane = null;
	private JTable movementsTable = null;
	private DefaultTableModel movementsTableModel = null;
	private String agent;
	
	/**
	 * This is the default constructor
	 */
	public MovementGUI(String agent) {
		super();
		this.agent = agent;
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
		this.setTitle("Movements for agent: "+agent);
	}
	
	/**
	 * This method initializes jContentPane
	 * @return javax.swing.JPanel
	 */
	private javax.swing.JPanel getJContentPane() {
		if(jContentPane == null) {
			jContentPane = new javax.swing.JPanel();
			jContentPane.setLayout(null);
			jContentPane.add(getMovementsScrollPane(),null);
		}
		return jContentPane;
	}
	
	/**
	 * This method initializes the messages ScrollPane
	 * @return javax.swing.JScrollPane	
	 */
	private JScrollPane getMovementsScrollPane() {
		if (movementsScrollPane == null) {
			movementsScrollPane = new JScrollPane();
			movementsScrollPane.setBounds(new Rectangle(3, 5, 440, 173));
			movementsScrollPane.setViewportView(getMovementsTable());
		}
		return movementsScrollPane;
	}
	
	/**
	 * Adds a new movement to the GUI
	 * @param from				the aid for the agent who initiated the movement
	 * @param to				the aid for the agent who received the amount moved
	 * @param amount			the amount
	 */
	protected void addMovement(AID from, AID to, Double amount){
		DecimalFormat df = new DecimalFormat("##,##.##");
		((DefaultTableModel) movementsTable.getModel()).addRow(new String [] {from.getLocalName(), to.getLocalName(), df.format(amount)});
	}
	/**
	 * This method initializes the messages table	
	 * @return javax.swing.JTable	
	 */
	private JTable getMovementsTable() {
		if (movementsTable == null) {
			movementsTable = new JTable();
			movementsTableModel =  new DefaultTableModel(null, new String [] {"From", "To", "Amount"}) {  
				private static final long serialVersionUID = 1L;
				public boolean isCellEditable(int row, int col) {  
					return false;  
				}  
			};  		
			movementsTable.setModel(movementsTableModel);
			movementsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			movementsTable.setAutoCreateRowSorter(true);
		}
		return movementsTable;
	}	
}

	package ei.service.negotiation.qfnegotiation;

import java.awt.Color;
import java.awt.ComponentOrientation;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;

import ei.onto.negotiation.qfnegotiation.ProposalEvaluation;
import ei.proto.negotiation.qfnegotiation.QFNegotiationInit;
import ei.proto.negotiation.qfnegotiation.QFNegotiationParallelInits;

/**
 * GUI for the negotiation mediator
 * @author pbn, hlc
 */
public class QFNegotiationMediatorGUI extends JFrame {

	private static final long serialVersionUID = -559927840085234240L;

	private JPanel jContentPane = null;
	private JScrollPane negotiationsScrollPane = null;
	
	private JTable negotiationsTable = null;
	private DefaultTableModel negotiationsTableModel = null;
	private JTable negotiationDetailsTable = null;
	private DefaultTableModel negotiationDetailsTableModel = null; 	

	private JScrollPane negotiationDetailsTableScrollPane = null;	
	
	private JButton killButton = null;
	
	private JLabel roundsLabel = null;
	private JTextField roundsTextField = null;
	
	private QFNegotiationMediator owner;
	
	private Hashtable<String,NegotiationsChart>  negotiationChartHashTable = new Hashtable<String,NegotiationsChart>(); // negotiationIdNeedType
	
	
	/**
	 * This is the default constructor
	 */
	public QFNegotiationMediatorGUI(QFNegotiationMediator negotiationMediator) {
		super();
		
		this.owner = negotiationMediator;
		
		initialize();
	}
	
	/**
	 * This method initializes this
	 * 
	 * @return void
	 */
	private void initialize() {
		jContentPane = new javax.swing.JPanel();
		this.setResizable(false);
		this.setDefaultCloseOperation(javax.swing.WindowConstants.HIDE_ON_CLOSE);
		this.setSize(540, 320);
		this.setLayout(null);
		this.setTitle(owner.getLocalName()+" -- Negotiation Mediator");
		this.setLocationRelativeTo(null);

		roundsLabel = new JLabel();
		roundsLabel.setText("Rounds:");
		roundsLabel.setBounds(5, 9, 49, 22);
		jContentPane.add(roundsLabel, null);
		roundsTextField = new JTextField();
		roundsTextField.setHorizontalAlignment(JTextField.RIGHT);
		roundsTextField.setBounds(55, 9, 25, 22);
		roundsTextField.addKeyListener(new KeyListener() {
			@Override
			public void keyTyped(KeyEvent e) {
			}
			
			@Override
			public void keyReleased(KeyEvent e) {
				owner.getConfigurationArguments().setProperty(QFNegotiationParameters.NEGOTIATION_ROUNDS, roundsTextField.getText());
			}
			
			@Override
			public void keyPressed(KeyEvent e) {
			}
		});
		if(owner.getConfigurationArguments().getProperty(QFNegotiationParameters.NEGOTIATION_ROUNDS) != null) {
			roundsTextField.setText("" + owner.getConfigurationArguments().getProperty(QFNegotiationParameters.NEGOTIATION_ROUNDS));
		} else {
			roundsTextField.setText("" + QFNegotiationParameters.NEGOTIATION_ROUNDS_DEFAULT);
		}
		jContentPane.add(roundsTextField, null);

		negotiationsScrollPane = new JScrollPane();
		negotiationsScrollPane.setBounds(new Rectangle(5, 40, 520, 115));
		negotiationsScrollPane.setViewportView(getNegotiationsTable());
		jContentPane.add(negotiationsScrollPane,null);

		negotiationDetailsTableScrollPane = new JScrollPane();
		negotiationDetailsTableScrollPane.setBounds(new Rectangle(5, 175, 520, 80)); 
		negotiationDetailsTableScrollPane.setViewportView(getNegotiationDetailsTable());
		jContentPane.add(negotiationDetailsTableScrollPane,null);

		jContentPane.add(getKillButton(),null);

		jContentPane.setLayout(null);
		
		this.setContentPane(jContentPane);
			
		refreshNegotiations();
		
		addWindowListener(new WindowListener(){  
			public void windowActivated(WindowEvent arg0) {}
			public void windowClosed(WindowEvent arg0) {}
			public void windowClosing(WindowEvent arg0) { 
				try {
					Integer.parseInt(owner.getConfigurationArguments().getProperty(QFNegotiationParameters.NEGOTIATION_ROUNDS));
				}catch (NumberFormatException nfe) {
					roundsTextField.setText("" + QFNegotiationParameters.NEGOTIATION_ROUNDS_DEFAULT);
				}
			}
			public void windowDeactivated(WindowEvent arg0) { }
			public void windowDeiconified(WindowEvent arg0) { }
			public void windowIconified(WindowEvent arg0) {}
			public void windowOpened(WindowEvent arg0) {}  
		});
		
	}
	
	private void refreshNegotiations() {
		Iterator<QFNegotiationParallelInits> qNegotiations = owner.getNeedsNegotiations().values().iterator();
		while(qNegotiations.hasNext()) {
			QFNegotiationParallelInits negotiation = qNegotiations.next();
			String state = owner.checkAgentsPerNeedStatus(negotiation.getAgentsPerNeed())?"successful":"FAILURE";   
			addNegotiationRow(negotiation.negotiationsID, state);					
		}
		
		if (negotiationsTable.getRowCount()!= -1 && negotiationsTable.getRowCount() > 0) {
			negotiationsTable.setRowSelectionInterval(negotiationsTable.getRowCount()-1, negotiationsTable.getRowCount()-1);
		    negotiationsScrollPane.getViewport().setViewPosition(new Point((int) negotiationsScrollPane.getViewport().getViewPosition().getX(), (int)negotiationsScrollPane.getViewport().getViewPosition().getY()+200)); 
		}			
	}
	
	/**
	 * Updates or adds this negotiation to GUI
	 * @param id			the negotiation id
	 * @param state			the state of the negotiation
	 */
	protected void addNegotiationRow(String id, String state){
		DefaultTableModel tableModel = (DefaultTableModel) negotiationsTable.getModel();
		tableModel.addRow(new String [] {id, state});
		
		if (owner.getConfigurationArguments().containsKey("maxEntries_needsNegotiation") && 
				(negotiationsTable.getRowCount() > Integer.parseInt(owner.getConfigurationArguments().getProperty("maxEntries_needsNegotiation"))) ) {
			
			tableModel.removeRow(0);
		}		
		
		if (negotiationsTable.getRowCount()!= -1) {
			negotiationsTable.setRowSelectionInterval(negotiationsTable.getRowCount()-1, negotiationsTable.getRowCount()-1);
		}				
	}
	
	/**
	 * This method initializes the negotiations table	
	 * @return javax.swing.JTable	
	 */
	private JTable getNegotiationsTable() {
		if (negotiationsTable == null) {
			negotiationsTable = new JTable();
			negotiationsTableModel =  new DefaultTableModel(null, new String [] {"Negotiation", "State"}) {  
				private static final long serialVersionUID = 1L;
				public boolean isCellEditable(int row, int col) {  
					return false;  
				}  
			};  			
			negotiationsTable.setModel(negotiationsTableModel);
			negotiationsTable.getColumnModel().getColumn(0).setPreferredWidth(260);
			negotiationsTable.getTableHeader().setBackground(Color.CYAN);
			negotiationsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);				
			negotiationsTable.getSelectionModel().addListSelectionListener(new ListenerNegotiationsInfoTable());
		}
		return negotiationsTable;
	}
	private class ListenerNegotiationsInfoTable implements ListSelectionListener {
		public void valueChanged(ListSelectionEvent e) {
			if (!e.getValueIsAdjusting()) {
				if(negotiationsTable.getSelectedRow() != -1){
					negotiationDetailsTableModel =  new DefaultTableModel(null, new String [] {"Need","Agents Negotiating","Current Winner","Round","Utility"}) {  
						private static final long serialVersionUID = 1L;
						public boolean isCellEditable(int row, int col) {  
							return false;  
						}  
					};
					negotiationDetailsTable.setModel(negotiationDetailsTableModel);
					negotiationDetailsTable.getColumnModel().getColumn(3).setPreferredWidth(20);
					negotiationDetailsTable.getColumnModel().getColumn(4).setPreferredWidth(35);					
					negotiationDetailsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);	
					
					DefaultTableModel tableModel = (DefaultTableModel) negotiationDetailsTable.getModel();
					QFNegotiationParallelInits qNegotiationParallelInits = owner.getNeedsNegotiations().get((String) negotiationsTableModel.getValueAt(negotiationsTable.getSelectedRow(), 0));
					if (qNegotiationParallelInits != null) {
						synchronized (qNegotiationParallelInits) {
							for (QFNegotiationInit qNegotiation : qNegotiationParallelInits.getQFNegotiationInitList()) {
								Vector<String> rowData = new Vector<String>();
								
								NegotiationRoundProposalEvaluations proposalsLastRound = qNegotiation.getReceivedProposalsPerRound().lastElement();
								rowData.add(qNegotiation.getNeed().getType());
								rowData.add(String.valueOf(proposalsLastRound.getProposalEvaluations().size()));
								rowData.add(proposalsLastRound.getBestProposalEvaluation().getIssuer().getLocalName());
								rowData.add(String.valueOf(proposalsLastRound.getAtRound())+"/"+proposalsLastRound.getAtRound());
								rowData.add(String.valueOf(proposalsLastRound.getBestProposalEvaluation().getEvaluation()));

								tableModel.addRow(rowData);
							}
						}
						
					}
				}							
			}
		}
	}		
	
	private JTable getNegotiationDetailsTable() {
		if (negotiationDetailsTable == null) {
			negotiationDetailsTable = new JTable();
			negotiationDetailsTable.getTableHeader().setBackground(Color.CYAN);
			negotiationDetailsTable.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
			negotiationDetailsTable.addMouseListener(new java.awt.event.MouseAdapter() { 
				public void mouseReleased(java.awt.event.MouseEvent e) {    
					if(e.getClickCount() == 2){
						QFNegotiationParallelInits qFNegotiationParallelInits = owner.getNeedsNegotiations().get((String) negotiationsTableModel.getValueAt(negotiationsTable.getSelectedRow(), 0));
						String negotiationID = qFNegotiationParallelInits.negotiationsID;
						String txt = "";
						if (qFNegotiationParallelInits.isUseTrustInPreselection() && qFNegotiationParallelInits.isUseTrustInProposalEvaluation()) {
							txt = ": TopN and Prop Evaluation";
						} else if (qFNegotiationParallelInits.isUseTrustInPreselection()) {
							txt = ": Top N";
						} else if (qFNegotiationParallelInits.isUseTrustInProposalEvaluation()) {
							txt = ": Prop Evaluation";
						}

						String negotiationIdNeedType = negotiationsTableModel.getValueAt(negotiationsTable.getSelectedRow(), 0) +"_"+negotiationDetailsTable.getValueAt(negotiationDetailsTable.getSelectedRow(), 0);
						if (!negotiationChartHashTable.containsKey(negotiationIdNeedType)) {
							negotiationChartHashTable.put(negotiationIdNeedType,new NegotiationsChart(QFNegotiationMediatorGUI.this, negotiationID, (String) negotiationDetailsTable.getValueAt(negotiationDetailsTable.getSelectedRow(), 0), txt));
						}
						NegotiationsChart chart = negotiationChartHashTable.get(negotiationIdNeedType);
						if (!chart.isVisible()) {
							for (QFNegotiationInit qNegotiation : qFNegotiationParallelInits.getQFNegotiationInitList()) {
								if (qNegotiation.getNeed().getType().equals((String) negotiationDetailsTable.getValueAt(negotiationDetailsTable.getSelectedRow(), 0))) {
									Vector<NegotiationRoundProposalEvaluations> proposalsByNeed = qNegotiation.getReceivedProposalsPerRound();
									java.util.List<String> agents = new ArrayList<String>();
									for (int i=0; i<proposalsByNeed.firstElement().getProposalEvaluations().size(); i++) {
										agents.add(proposalsByNeed.firstElement().getProposalEvaluations().get(i).getIssuer().getLocalName());
									}
									Collections.sort(agents);
									chart.populateAgent(agents);
									chart.setNumberOfRounds(((NegotiationRoundProposalEvaluations)proposalsByNeed.lastElement()).getAtRound());
									for(NegotiationRoundProposalEvaluations negotiationProposalEvaluations: proposalsByNeed) {
										for (int i=0; i<negotiationProposalEvaluations.getProposalEvaluations().size();i++) {
											ProposalEvaluation proposalEvaluation = negotiationProposalEvaluations.getProposalEvaluations().get(i);
											chart.addValue(proposalEvaluation.getIssuer().getLocalName(),negotiationProposalEvaluations.getAtRound(),proposalEvaluation.getEvaluation());
										}
									}
								}
							}
						}
						chart.showGui();						
					}
				}
			});
		}
		return negotiationDetailsTable;
	}
	
	/**
	 * Initializes the button to kill the agent
	 */    
	private JButton getKillButton() {
		if ( killButton == null ) {
			killButton = new JButton();
			killButton.setText("X");
			killButton.setBounds(new Rectangle(470, 265, 55, 18));
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

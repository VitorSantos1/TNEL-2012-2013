package ei.agent.enterpriseagent.gui;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableRowSorter;

import ei.service.ctr.OutcomeGenerator;
import ei.service.negotiation.qfnegotiation.InterestedAgent;

import java.awt.Rectangle;
import java.text.DecimalFormat;
import java.util.Vector;


public class TrustedAgentsGUI extends JFrame {
	private static final long serialVersionUID = 1L;

	private JPanel jContentPane = null;
	
	private JPanel trustworthinessLessPanel;
	private JPanel trustworthinessAwarePanel;
	
	private JScrollPane trustworthinessLessScrollPane = null;
	private JScrollPane trustworthinessAwareScrollPane;
	
	private JTable lessTable = null;
	private JTable awareTable = null;	
	
	private DecimalFormat df;	

	private JLabel topNLabel;
	private JLabel mappingLabel;

	private Vector<InterestedAgent> agents;
	private int topNumberOfAgents;
	private OutcomeGenerator.MappingMethod mapMethod;

	/**
	 * This is the default constructor
	 */
	public TrustedAgentsGUI(int topNumberOfAgents, Vector<InterestedAgent> tA, OutcomeGenerator.MappingMethod mapMethod) {
		super("Preview: Trustworthiness");
		this.agents = tA;
		this.topNumberOfAgents = topNumberOfAgents;
		this.mapMethod = mapMethod;
		
		df = new DecimalFormat("###.####");
		initialize();
	}

	/**
	 * This method initializes this
	 * 
	 * @return void
	 */
	private void initialize() {
		this.setSize(450, 247);
		jContentPane = new JPanel();
		jContentPane.setLayout(null);
		
		topNLabel = new JLabel();
		topNLabel.setBounds(20, 9, 120, 22);
		if (topNumberOfAgents == -1) { 
			topNLabel.setText("Top N: " + "All");
		} else if (topNumberOfAgents == -2) { 
				topNLabel.setText("Top N: " + "TNH");
		} else {
			topNLabel.setText("Top N: " + topNumberOfAgents);
		}
		jContentPane.add(topNLabel,null);
		mappingLabel = new JLabel();
		mappingLabel.setBounds(20, 29, 180, 22);
		mappingLabel.setText("Mapping: " + mapMethod);
		jContentPane.add(mappingLabel,null);
		
		jContentPane.add(getTrustworthinessLessPanel(), null);		
		jContentPane.add(getTrustworthinessAwarePanel(), null);
		
		this.setContentPane(jContentPane);
		this.setResizable(false);
		this.setLocationRelativeTo(null);		
	}
	
	private JPanel getTrustworthinessLessPanel() {
		if(trustworthinessLessPanel == null) {
			trustworthinessLessPanel = new JPanel();
			trustworthinessLessPanel.setLayout(null);
			trustworthinessLessPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createLineBorder(
					new java.awt.Color(102, 102, 102)), "Situation-less", javax.swing.border.TitledBorder.CENTER, javax.swing.border.TitledBorder.DEFAULT_POSITION) );
			trustworthinessLessPanel.setBounds(14, 60, 204, 140);
			
			trustworthinessLessScrollPane = new JScrollPane();
			trustworthinessLessScrollPane.setBounds(new Rectangle(5, 18, 195, 110));
			trustworthinessLessScrollPane.setViewportView(trustworthinessLessTable());
			
			trustworthinessLessPanel.add(trustworthinessLessScrollPane, null);
		}
		return trustworthinessLessPanel;
	}
	private JPanel getTrustworthinessAwarePanel() {
		if(trustworthinessAwarePanel == null) {
			trustworthinessAwarePanel = new JPanel();
			trustworthinessAwarePanel.setLayout(null);
			trustworthinessAwarePanel.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createLineBorder(
					new java.awt.Color(102, 102, 102)), "Situation-aware", javax.swing.border.TitledBorder.CENTER, javax.swing.border.TitledBorder.DEFAULT_POSITION) );
			trustworthinessAwarePanel.setBounds(224, 60, 204, 140);
			
			trustworthinessAwareScrollPane = new JScrollPane();
			trustworthinessAwareScrollPane.setBounds(new Rectangle(5, 18, 195, 110));
			trustworthinessAwareScrollPane.setViewportView(trustworthinessAwareTable());
			
			trustworthinessAwarePanel.add(trustworthinessAwareScrollPane, null);
		}
		return trustworthinessAwarePanel;
	}	

	private JTable trustworthinessLessTable() {
		if (lessTable == null) {
			lessTable = new JTable();

			Object[][] data = new Object[agents.size()][2];
			for(int i=0; i<agents.size(); i++) {
				data[i][0] = agents.get(i).getAgent().getLocalName();
				data[i][1] = df.format(agents.get(i).getCtrEvaluation());
			}
			lessTable.setModel(new AgentTrustWorthinessModel(data));
		    TableRowSorter<AgentTrustWorthinessModel> sorter = new TableRowSorter<AgentTrustWorthinessModel>((AgentTrustWorthinessModel) lessTable.getModel());
		    sorter.toggleSortOrder(1);
		    sorter.toggleSortOrder(1);		    
		    lessTable.setRowSorter(sorter);
		    lessTable.getColumnModel().getColumn(0).setPreferredWidth(100);		    
			lessTable.repaint();			
		}
		return lessTable;
	}
	
	private JTable trustworthinessAwareTable() {
		if ( awareTable == null) {
			awareTable = new JTable();

			Object[][] data = new Object[agents.size()][2];
			for(int i=0; i<agents.size(); i++) {
				data[i][0] = agents.get(i).getAgent().getLocalName();
				data[i][1] = df.format(agents.get(i).getContextualEvaluation());
			}
			awareTable.setModel(new AgentTrustWorthinessModel(data));
		    TableRowSorter<AgentTrustWorthinessModel> sorter = new TableRowSorter<AgentTrustWorthinessModel>((AgentTrustWorthinessModel) awareTable.getModel());
		    sorter.toggleSortOrder(1);
		    sorter.toggleSortOrder(1);		    
		    awareTable.setRowSorter(sorter);
			awareTable.getColumnModel().getColumn(0).setPreferredWidth(100);		    			
			awareTable.repaint();		
		}
		return awareTable;
	}	
	
	
	private class AgentTrustWorthinessModel extends AbstractTableModel {
		private static final long serialVersionUID = 1L;
		private String[] columnNames = {"Agent", "Value"};
		private Object[][] data;
		
		public AgentTrustWorthinessModel(Object[][] data){
			this.data = data;
		}

		public int getColumnCount() {
			return columnNames.length;
		}

		public int getRowCount() {
			return data.length;
		}

		public String getColumnName(int col) {
			return columnNames[col];
		}

		public Object getValueAt(int row, int col) {
			return data[row][col];
		}

		public Class<?> getColumnClass(int c) {
			return getValueAt(0, c).getClass();
		}

		/*
		 * Don't need to implement this method unless your table's
		 * data can change.
		 */
		public void setValueAt(Object value, int row, int col) {
			data[row][col] = value;
			fireTableCellUpdated(row, col);
		}
	}
}  

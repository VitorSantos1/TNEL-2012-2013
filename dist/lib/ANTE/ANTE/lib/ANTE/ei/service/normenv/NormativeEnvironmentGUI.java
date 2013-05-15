package ei.service.normenv;

import jade.core.AID;
import jade.core.Agent;
import jade.util.leap.List;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.Insets;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;

import java.util.Collections;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.LegendItem;
import org.jfree.chart.LegendItemCollection;
import org.jfree.chart.LegendItemSource;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.NumberTickUnit;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.title.LegendTitle;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.HorizontalAlignment;
import org.jfree.ui.RectangleEdge;
import org.jfree.ui.RectangleInsets;

import ei.gui.InfoViewerGUI;
import ei.onto.normenv.management.ContractType;
import ei.onto.normenv.report.DeadlineViolation;
import ei.onto.normenv.report.Fulfillment;
import ei.onto.normenv.report.LivelineViolation;
import ei.onto.normenv.report.Obligation;
import ei.onto.normenv.report.Report;
import ei.onto.normenv.report.Violation;
import ei.proto.normenv.management.NEManagementInit_SendContractTypes;

public class NormativeEnvironmentGUI extends JFrame {
	private static final long serialVersionUID = 4805199905745004135L;

	private JLabel contractTypeLabel = null;
	private JButton normEnvButton = null;
	private JButton contractTypeNormsButton = null;
	private JList obligationJList = null;
	private DefaultListModel obligationJListModel = null;	

	private JComboBox contractTypeComboBox = null;
	private JScrollPane obligationIdListJScroll = null;	
	private Vector<String> contractTypeToolTip;

	private ArrayList<JCheckBox> obligationOutcomeCheckBox = new ArrayList<JCheckBox>();

	private JPanel jContentPane;
	private JPanel oblLinesPanel = null;	
	private JPanel mainPanel; 

	private JPanel chartPanel = null;
	private ChartPanel chartFrame;	
	private JFreeChart chartJFreeChart;	

	private XYSeriesCollection datasetXYSeries;
	private ValueAxis domainaxis;
	private NumberAxis rangeAxisContract;

	private NormativeEnvironment owner;

	private final String D_VIOLVIOL = "DViolViol";
	private final String L_VIOLVIOL = "LViolViol";
	private final String D_VIOLFULF = "DViolFulf";
	private final String L_VIOLFULF = "LViolFulf";
	private final String FULF = "Fulf";
	//labels for checkbox items
	private final String VIOL = "Viol";	
	private final String FULF_OUT = "Fulf (out)";
	private final String FULF_IN = "Fulf (in)";	
	private final String FULF_IN_OUT = "Fulf (in+out)";

	private XYPlot plot = null;

	/**
	 * The default constructor.
	 * @param owner 	The parent for this object
	 */
	public NormativeEnvironmentGUI(NormativeEnvironment owner) {
		this.owner = owner;

		initialize();

		owner.addBehaviour(new FillContractTypes(owner, owner.getAID()));
	}

	/**
	 * Initializes this object
	 */
	public void initialize() {
		this.setSize(900, 650);
		this.setResizable(false);
		this.setLocationRelativeTo(null);
		this.setTitle("Normative Environment GUI");

		jContentPane = new JPanel();
		jContentPane.setLayout(null);

		jContentPane.add(getMainPanel(), null);

		mainPanel.add(getViewNormEnv(), null);
		mainPanel.setBounds(new Rectangle(1, 1, 900, 650));		

		chartPanel = getChartEvolutionPanel();
		chartPanel.setBounds(new Rectangle(5, 60, 780, 480));

		mainPanel.add(getContractTypeNormsButton());

		mainPanel.add(getCheckBoxPanel());

		mainPanel.add(chartPanel);

		mainPanel.add(getJPaneList());

		this.setContentPane(jContentPane);
	}

	private JPanel getCheckBoxPanel() {
		if (oblLinesPanel == null) {
			oblLinesPanel = new JPanel();
			oblLinesPanel.setBounds(new Rectangle(785, 85, 90, 98));

			JPanel panel = new JPanel();
			panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
			panel.setBounds(new Rectangle(785, 120, 90, 98));
			ArrayList<String> oblOutcome = new ArrayList<String>();
			for (int t=2;t<ObligationOutcome.values().length;t++){
				oblOutcome.add(String.valueOf(ObligationOutcome.values()[t]));
			}

			boolean isFulf_out = false, isFulf_in = false;			
			if (oblOutcome.contains(D_VIOLVIOL) && oblOutcome.contains(L_VIOLVIOL)) {
				JCheckBox cb = new JCheckBox(VIOL);		
				cb.setSelected(true);
				cb.setForeground(new Color(238,0,0));				
				cb.addItemListener(new ObligationOutcomeCheckBoxItemListener());
				obligationOutcomeCheckBox.add(cb);		
				panel.add(cb);
			} 
			if (oblOutcome.contains(D_VIOLFULF) && oblOutcome.contains(L_VIOLFULF)) {
				JCheckBox cb = new JCheckBox(FULF_OUT);	
				cb.setSelected(true);
				cb.setForeground(new Color(139,0,0));
				cb.addItemListener(new ObligationOutcomeCheckBoxItemListener());
				panel.add(cb);
				obligationOutcomeCheckBox.add(cb);
				isFulf_out = true;
			}
			if (oblOutcome.contains(FULF)) {
				JCheckBox cb = new JCheckBox(FULF_IN);
				cb.setSelected(true);	
				cb.setForeground(new Color(47, 79, 79));
				cb.addItemListener(new ObligationOutcomeCheckBoxItemListener());
				panel.add(cb);
				obligationOutcomeCheckBox.add(cb);
				isFulf_in = true;
			}

			if (isFulf_in && isFulf_out) {
				JCheckBox cb = new JCheckBox(FULF_IN_OUT);	
				cb.setSelected(true);
				cb.setForeground(new Color(84, 139, 84));				
				cb.addItemListener(new ObligationOutcomeCheckBoxItemListener());
				panel.add(cb);
				obligationOutcomeCheckBox.add(cb);
			}
			oblLinesPanel.add(panel);		
		}
		return oblLinesPanel;
	}

	private JScrollPane getJPaneList() {
		if (obligationJList == null) {
			obligationJListModel = new DefaultListModel();
			obligationJList = new JList(obligationJListModel);
			obligationJList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
			obligationJList.addListSelectionListener(new ListSelectionListener() {
				public void valueChanged(ListSelectionEvent lse) {
					if( lse.getSource() == obligationJList && !lse.getValueIsAdjusting() ) {
						new DoUpdateValues(1);						
					}
				}
			});
			obligationIdListJScroll = new JScrollPane(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
			obligationIdListJScroll.setBounds(new Rectangle(785, 260, 90, 80));
			obligationIdListJScroll.setViewportView(obligationJList);
			obligationIdListJScroll.setBorder(BorderFactory.createEtchedBorder(1));
		}
		return obligationIdListJScroll;
	}	

	public class ObligationOutcomeCheckBoxItemListener implements ItemListener{
		public void itemStateChanged(ItemEvent e) {
			if ( e.getStateChange() == ItemEvent.SELECTED  || e.getStateChange() == ItemEvent.DESELECTED) {
				new DoUpdateValues(0.5);
			}
		}
	}
	private JPanel getMainPanel() {
		if(mainPanel == null) {
			mainPanel = new JPanel();
			mainPanel.setLayout(null);
			mainPanel.setBounds(5, 20, 580, 490);

			contractTypeToolTip = new Vector<String>();
			contractTypeLabel = new JLabel();
			contractTypeLabel.setText("Contract Type:");
			contractTypeLabel.setName("ctLabel");
			contractTypeLabel.setBounds(9, 20, 90, 22); 
			mainPanel.add(contractTypeLabel,null);

			contractTypeComboBox = new JComboBox();
			contractTypeComboBox.setBounds(98, 20, 135, 22);
			contractTypeComboBox.addItem("empty");
			contractTypeComboBox.setBackground(Color.white);
			java.awt.event.ActionListener actionListener = new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent actionEvent) {
					int si = contractTypeComboBox.getSelectedIndex();
					if(si != -1) {
						if(contractTypeToolTip.size() > 0) {	
							contractTypeComboBox.setToolTipText(contractTypeToolTip.get(si).toString());
						}

					}
				}
			};
			contractTypeComboBox.addItemListener(new ItemListener() {
				public void itemStateChanged(ItemEvent evt) {
					if (evt.getStateChange() == ItemEvent.SELECTED) {
						new DoUpdateValues(0.5);
					}
				}
			});
			contractTypeComboBox.addActionListener(actionListener);

			mainPanel.add(contractTypeComboBox,null);
		}

		return mainPanel;
	}

	/**
	 * This method initializes View	
	 * @return javax.swing.JButton	
	 */
	private JButton getViewNormEnv() {
		if (normEnvButton == null) {
			normEnvButton = new JButton();
			normEnvButton.setBounds(800, 510, 80, 22);
			normEnvButton.setText("<html><center>JessEngine</center></html>");
			normEnvButton.setMargin(new Insets(1,1,1,1));
			normEnvButton.addMouseListener(new java.awt.event.MouseAdapter() { 
				public void mouseReleased(java.awt.event.MouseEvent e) {
					owner.getNormEnvJessGui().setVisible(true); 
				}
			});
		}
		return normEnvButton;
	}	

	private JButton getContractTypeNormsButton(){
		if (contractTypeNormsButton == null) {
			contractTypeNormsButton = new JButton();
			contractTypeNormsButton.setText("Norms");
			contractTypeNormsButton.setEnabled(true);
			contractTypeNormsButton.setMargin(new Insets(0,0,0,0));
			contractTypeNormsButton.setBounds(240, 20, 90, 22); //.setLocation(245, 198);
			contractTypeNormsButton.setName("normsButton");
			contractTypeNormsButton.addMouseListener(new java.awt.event.MouseAdapter(){
				public void mouseReleased(java.awt.event.MouseEvent e) {
					if(contractTypeComboBox.getSelectedIndex() != -1){
						InfoViewerGUI infoGui = new InfoViewerGUI("Applicable norms for " + contractTypeComboBox.getSelectedItem().toString(),
								new jade.util.leap.ArrayList(owner.normEnvBeh.getApplicableNorms(contractTypeComboBox.getSelectedItem().toString())));
						infoGui.setVisible(true);

					}
				}
			});
		}
		return contractTypeNormsButton;
	}	
	/**
	 * Fills the contract type combobox
	 * @param list	the list of contract types
	 */
	protected void setContractTypes(List list) {
		contractTypeComboBox.removeAllItems();
		contractTypeToolTip.removeAllElements();
		for(int i=0;i<list.size();i++) {
			contractTypeComboBox.addItem(((ContractType) list.get(i)).getName());
			contractTypeToolTip.add(((ContractType) list.get(i)).getComment());
		}
		contractTypeComboBox.setSelectedIndex(0);
	}

	protected synchronized void updateValues() {
		String contractType = (String)contractTypeComboBox.getItemAt(contractTypeComboBox.getSelectedIndex());

		// get contracts of selected type
		ArrayList<String> contracts = owner.contractsByType.get(contractType);

		if (contracts != null) {
			Hashtable<String, Integer> oblNoOccurs = new Hashtable<String, Integer>();
			//[contractIndex] (obligationId -> a value for this contract)
			ArrayList<Hashtable<String, ObligationOutcome>> infoToPlot = new ArrayList<Hashtable<String,ObligationOutcome>>();

			DefaultListModel listModel = ((DefaultListModel)(obligationJList.getModel()));	

			for(int i=0; i<contracts.size(); i++) {
				// get reports of a contract
				ArrayList<Report> reports = owner.reportsByContract.get(contracts.get(i));
				//obligationName -> isFulfilled
				Hashtable<String, ObligationOutcome> obligations = new Hashtable<String, ObligationOutcome>();
				// iterate through reports
				for(int j=0; j<reports.size(); j++) {
					Report report = reports.get(j);
					if(report instanceof Obligation) {
						String obligationId = ((Obligation) report).getId();
						// check if it is the first time this obligation appears
						if(oblNoOccurs.get(obligationId) == null) {
							// just found out a new obligation id!
							oblNoOccurs.put(obligationId, new Integer(0));
						}
					} else if(report instanceof DeadlineViolation) {
						String obligationId = ((DeadlineViolation) report).getObligation().getId();
						obligations.put(obligationId, ObligationOutcome.DViol);
					} else if(report instanceof LivelineViolation) {
						String obligationId = ((LivelineViolation) report).getObligation().getId();
						obligations.put(obligationId, ObligationOutcome.LViol);
					} else if(report instanceof Fulfillment) {   // fulfillment increments
						String obligationId = ((Fulfillment) report).getObligation().getId();
						if(obligations.get(obligationId) == null) {
							obligations.put(obligationId, ObligationOutcome.Fulf);
						} else {
							switch(obligations.get(obligationId)) { // check if there was a dviol before
							case DViol:
								obligations.put(obligationId, ObligationOutcome.DViolFulf);
								break;
							case LViol:
								obligations.put(obligationId, ObligationOutcome.LViolFulf);
								break;
							}
						}
						if (!listModel.contains(obligationId)) {
							listModel.addElement(obligationId);
						}
					} else if(report instanceof Violation) {   // violation decrements
						String obligationId = ((Violation) report).getObligation().getId();
						if(obligations.get(obligationId) == null) {
							obligations.put(obligationId, ObligationOutcome.DViol);
						} else {
							switch(obligations.get(obligationId)) { // check if there was a dviol before
							case DViol:
								obligations.put(obligationId, ObligationOutcome.DViolViol);								
								break;
							case LViol:
								obligations.put(obligationId, ObligationOutcome.LViolViol);
								break;
							}
						}
						if (!listModel.contains(obligationId)) {
							listModel.addElement(obligationId);
						}
					}
				}
				infoToPlot.add(obligations);
			}

			if (obligationJList.isSelectionEmpty() 
					|| ((obligationJList.getMaxSelectionIndex()+1) < listModel.size() && (obligationJList.getMaxSelectionIndex()+listModel.size() < listModel.size()))
					) {
				obligationJList.setSelectionInterval(0, listModel.size()-1);
			}
			
			datasetXYSeries.removeAllSeries();
			//adds line in dataSet
			ArrayList<String> lines = new ArrayList<String>();
			for(int x = 0; x<infoToPlot.size(); x++) {
				for(int i = 0; i < obligationJList.getSelectedValues().length; i++) {
					String selectedObligationId = obligationJList.getSelectedValues()[i].toString();
					Enumeration<String> keys = infoToPlot.get(x).keys();   
					while (keys.hasMoreElements()) {
						String obligationId = keys.nextElement();
						if (selectedObligationId.equals(obligationId)) {
							ObligationOutcome outcome = infoToPlot.get(x).get(obligationId);
							switch(outcome) {
							case LViolViol:
							case DViolViol:
								for(JCheckBox cb:obligationOutcomeCheckBox){
									String line = obligationId+cb.getText();
									if (cb.isSelected())  {		
										if (!lines.contains(line)) {
											lines.add(line);
										}
									}
								}
								break;
							case LViolFulf:
							case DViolFulf:
								for(JCheckBox cb:obligationOutcomeCheckBox){
									String line = obligationId+cb.getText();
									if (cb.isSelected())  {		
										if (!lines.contains(line)) {
											lines.add(line);
										}
									}
									//Fulf (in+out)
									if (cb.isSelected() && cb.getText().equals(FULF_IN_OUT)) {		
										if (!lines.contains(line)) {
											lines.add(line);
										}
									}							
								}

								break;
							case Fulf:
								for(JCheckBox cb:obligationOutcomeCheckBox){
									String line = obligationId+cb.getText();
									if (cb.isSelected())  {		
										if (!lines.contains(line)) {
											lines.add(line);
										}
									}
									//Fulf (in+out)
									if (cb.isSelected() && cb.getText().equals(FULF_IN_OUT)) {		
										if (!lines.contains(line)) {
											lines.add(line);
										}
									}							
								}
								break;
							}
						}
					}
				}
			}


			Collections.sort(lines);
			int serieIndex = 0;
			for (String line: lines) {
				addLineToDataset(line);
				pickCustomizedLines(serieIndex, line);
				lineShapes(serieIndex, line);
				serieIndex++;
			}
			pickCustomizedLegend();

			//update value to plot...
			//computes line's performances
			Hashtable<String, Double> oblPerformanceLines = new Hashtable<String, Double>();	
			for(int x = 0; x<infoToPlot.size(); x++) {
				Enumeration<String> keys = infoToPlot.get(x).keys();   
				while (keys.hasMoreElements()) {
					String line ="";					
					String obligationId = keys.nextElement();

					oblNoOccurs.put(obligationId, oblNoOccurs.get(obligationId)+1);

					ObligationOutcome outcome = infoToPlot.get(x).get(obligationId);
					switch(outcome) {
					case LViolViol:
					case DViolViol:
						line = obligationId+VIOL;		
						if (!oblPerformanceLines.containsKey(line)) {
							oblPerformanceLines.put(line, 1.0);
						} else {
							oblPerformanceLines.put(line, oblPerformanceLines.get(line)+1.0);
						}
						addValueToDataset(line, (x+1), oblPerformanceLines.get(line)/oblNoOccurs.get(obligationId));
						break;
					case LViolFulf:
					case DViolFulf:
						line = obligationId+FULF_OUT;		
						if (!oblPerformanceLines.containsKey(line)) {
							oblPerformanceLines.put(line, 1.0);
						} else {
							oblPerformanceLines.put(line, oblPerformanceLines.get(line)+1.0);
						}
						addValueToDataset(line, (x+1), oblPerformanceLines.get(line)/oblNoOccurs.get(obligationId));
						//Fulf (in+out)
						line = obligationId+FULF_IN_OUT;		
						if (!oblPerformanceLines.containsKey(line)) {
							oblPerformanceLines.put(line, 1.0);
						} else {
							oblPerformanceLines.put(line, oblPerformanceLines.get(line)+1.0);
						}
						addValueToDataset(line, (x+1), oblPerformanceLines.get(line)/oblNoOccurs.get(obligationId));
						break;
					case Fulf:
						line = obligationId+FULF_IN;
						if (!oblPerformanceLines.containsKey(line)) {
							oblPerformanceLines.put(line, 1.0);
						} else {
							oblPerformanceLines.put(line, oblPerformanceLines.get(line)+1.0);
						}
						addValueToDataset(line, (x+1), oblPerformanceLines.get(line)/oblNoOccurs.get(obligationId));	
						//Fulf (in+out)
						line = obligationId+FULF_IN_OUT;		
						if (!oblPerformanceLines.containsKey(line)) {
							oblPerformanceLines.put(line, 1.0);
						} else {
							oblPerformanceLines.put(line, oblPerformanceLines.get(line)+1.0);
						}
						addValueToDataset(line, (x+1), oblPerformanceLines.get(line)/oblNoOccurs.get(obligationId));
						break;
					}
				} 
			}

			domainaxis.setTickLabelFont(new Font(Font.SANS_SERIF, Font.PLAIN, pickSizeFont(infoToPlot.size())));
			rangeAxisContract.setTickUnit(pickNumberTickUnit(infoToPlot.size()));

			//Updating and refreshing the graph per second
			try {
				Thread.sleep(200);
			} catch (InterruptedException e) {
				//e.printStackTrace();
			}				
		} else {
			datasetXYSeries.removeAllSeries();	
			obligationJListModel.clear();
		}
	}

	public class FillContractTypes extends NEManagementInit_SendContractTypes {
		private static final long serialVersionUID = 1L;

		public FillContractTypes(Agent agent, AID normenv) {
			super(agent, normenv);
		}

		public int onEnd() {
			setContractTypes(getContractTypes());
			return 0;
		}
	}

	private JPanel getChartEvolutionPanel() {
		datasetXYSeries = new XYSeriesCollection();
		chartPanel = new JPanel();

		chartJFreeChart = ChartFactory.createXYLineChart("Contract Enactments", "Contract","% Obligations",datasetXYSeries,PlotOrientation.VERTICAL,false,true,false);
		chartJFreeChart.setBackgroundPaint(this.getBackground());
		chartJFreeChart.setTitle(new org.jfree.chart.title.TextTitle("Contract Enactments",new java.awt.Font("SansSerif", java.awt.Font.BOLD, 18)));

		// get a reference to the plot for further customisation...
		plot = (XYPlot) chartJFreeChart.getPlot();
		plot.setBackgroundPaint(Color.white);
		plot.setAxisOffset(new RectangleInsets(4.0, 4.0, 4.0, 4.0));
		plot.setDomainGridlinePaint(Color.lightGray);
		plot.setRangeGridlinePaint(Color.lightGray);

		XYLineAndShapeRenderer renderer = (XYLineAndShapeRenderer) plot.getRenderer();  
		renderer.setBaseShapesVisible(true);
		renderer.setDrawOutlines(true);
		renderer.setUseFillPaint(true);

		NumberAxis rangeAxisEvaluation = (NumberAxis) plot.getRangeAxis();  
		rangeAxisEvaluation.setRange(0.0, 1.0);
		rangeAxisEvaluation.setTickUnit( new NumberTickUnit(0.1) );  	

		rangeAxisContract = (NumberAxis) plot.getDomainAxis();
		rangeAxisContract.setTickUnit(new NumberTickUnit(1.0));

		domainaxis = plot.getDomainAxis();

		chartFrame = new ChartPanel(chartJFreeChart);
		chartFrame.setVisible(true);
		chartFrame.setMouseZoomable(true);

		chartPanel.setLayout(new BorderLayout());
		chartPanel.add(chartFrame);

		return chartPanel;
	}

	/**
	 * add a line for chart
	 * @param line
	 */
	private void addLineToDataset(String line){
		XYSeries xyLine = new XYSeries(line);
		xyLine.setDescription(line);		
		datasetXYSeries.addSeries(xyLine);
	}	

	/**
	 * 
	 * @param line  line
	 * @param x    contracts
	 * @param y    evaluation
	 */
	private void addValueToDataset(String line, int x, double y){
		for(int i=0;i<datasetXYSeries.getSeriesCount();i++){
			if(datasetXYSeries.getSeries(i).getDescription().equals(line)){
				datasetXYSeries.getSeries(i).add(x,y); 
			}
		}
	}


	private int pickSizeFont (int nContracts) {
		int size = 11;
		if (nContracts < 50) {
			size = 11;
		} else if (nContracts >= 50 && nContracts < 100) {
			size = 10;
		} else if (nContracts > 100) {
			size = 9;
		}
		return size;
	}	

	private NumberTickUnit pickNumberTickUnit (int contracts) {
		if (contracts > 30 && contracts <= 60) {
			return new NumberTickUnit(2.0f);
		} else if (contracts > 60 && contracts <= 120) {
			return new NumberTickUnit(4.0f);
		} else if (contracts > 120 && contracts <= 180 ) {
			return new NumberTickUnit(10.0f); 
		} else if (contracts > 180 && contracts <= 300 ) {
			return new NumberTickUnit(20.0f); 
		} else if (contracts > 300) {
			return new NumberTickUnit(30.0f); 
		}
		return new NumberTickUnit(1.0f);
	}

	private void pickCustomizedLines (int serie, String line) {
		XYLineAndShapeRenderer renderer = (XYLineAndShapeRenderer) plot.getRenderer(); 	

		Color color = Color.BLACK;
		if (line.endsWith(FULF_IN)) {
			renderer.setSeriesStroke(serie, new BasicStroke(1.3f, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_ROUND,1.0f, new float[] { 6.0f, 6.0f}, 0.0f));
			color = new Color(47, 79, 79); //0 139 69
		} else if (line.endsWith(FULF_OUT)) {
			renderer.setSeriesStroke(serie, new BasicStroke(1.3f, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_ROUND,1.0f, new float[] { 10.0f, 6.0f}, 0.0f));
			color = new Color(139,0,0);
		} else if (line.endsWith(FULF_IN_OUT)) {
			renderer.setSeriesStroke(serie, new BasicStroke(1.5f, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_ROUND,1.0f, new float[] { 5.0f, 3.0f}, 0.0f));			
			color = new Color(84, 139, 84); // 50 205 50
		} else if (line.endsWith(VIOL)) {
			renderer.setSeriesStroke(serie, new BasicStroke(1.3f, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_ROUND,1.0f, new float[] { 2.0f, 6.0f}, 0.0f));
			color = new Color(238,0,0);
		}
		renderer.setSeriesPaint(serie, color);
	}
	private void pickCustomizedLegend() {
		chartJFreeChart.removeLegend();
		LegendItemCollection legendItemsOld = plot.getLegendItems();
		final LegendItemCollection legendItemNews = new LegendItemCollection();

		for (int x=0; x < obligationJListModel.size(); x++) {
			for(int i = 0; i< legendItemsOld.getItemCount(); i++){
				if(legendItemsOld.get(i).getLabel().toString().startsWith(obligationJListModel.get(x).toString())){
					legendItemNews.add(legendItemsOld.get(i));
				}
			}
			legendItemNews.add(new LegendItem("","","","",new Rectangle2D.Float(-1, -1, (chartPanel.getWidth()-120), 1),Color.white));
		}

		LegendItemSource source = new LegendItemSource() {
			LegendItemCollection legendIC = new LegendItemCollection(); 
			{
				legendIC.addAll(legendItemNews);
			}
			public LegendItemCollection getLegendItems() {
				return legendIC;
			}
		};
		chartJFreeChart.addLegend(new LegendTitle(source));	
		LegendTitle legend = chartJFreeChart.getLegend();
		legend.setPosition(RectangleEdge.BOTTOM);
		legend.setBackgroundPaint(Color.white);
		legend.setHorizontalAlignment(HorizontalAlignment.CENTER);
		legend.setBorder(1, 1, 1, 1);
	}	

	private void lineShapes (int serieIndex, String line) {
		XYLineAndShapeRenderer renderer = (XYLineAndShapeRenderer) plot.getRenderer();		
		ArrayList<Shape>  shapes = shapes();
		for (int i=0; i < obligationJListModel.size(); i++) {
			if (line.startsWith(obligationJListModel.get(i).toString())) {
				renderer.setSeriesShape(serieIndex, shapes.get(i));
			}
		}
	}

	private ArrayList<Shape> shapes() {
		ArrayList<Shape>  shapes = new ArrayList<Shape>();

		Polygon triangle = new Polygon();
		triangle.addPoint(-2, -2);
		triangle.addPoint(4, -4);
		triangle.addPoint(0, 4);
		Polygon triangle2 = new Polygon();
		triangle2.addPoint(4, 4);
		triangle2.addPoint(-2, 4);
		triangle2.addPoint(0, -2);
		Polygon triangle3 = new Polygon();
		triangle3.addPoint(5, 5);
		triangle3.addPoint(-1, 5);
		triangle3.addPoint(0, -1);
		Polygon triangle4 = new Polygon();
		triangle4.addPoint(-1, -1);
		triangle4.addPoint(5, -5);
		triangle4.addPoint(0, 5);

		shapes.add(new Ellipse2D.Double(-3, -3, 6, 6));
		shapes.add(new Rectangle2D.Double(-4, -4, 8, 8));
		shapes.add(new Cross4());
		shapes.add(triangle);
		shapes.add(new Cross());
		shapes.add(new Rod());
		shapes.add(new Ellipse2D.Double(-5, -2, 10, 4));
		shapes.add(triangle2);
		shapes.add(new Ellipse2D.Double(-2, -5, 4, 10));
		shapes.add(new Cross3());
		shapes.add(new Rectangle2D.Float(-4, -1, 8, 3));
		shapes.add(triangle3);
		shapes.add(new Rectangle2D.Float(-1, -4, 3, 8));
		shapes.add(new Cross2());
		shapes.add(triangle4);
		shapes.add(new Ellipse2D.Double(-4, -4, 8, 8));
		shapes.add(new Rectangle2D.Double(-4, -4, 8, 8));
		shapes.add(new Rod2());


		return shapes;
	}	

	final class Cross extends Polygon {
		private static final long serialVersionUID = 1L;
		public Cross() {
			super.addPoint(-6, 2);
			super.addPoint(-2, 2);
			super.addPoint(-2, 6);
			super.addPoint(2, 6);
			super.addPoint(2, 2);
			super.addPoint(6, 2);
			//
			super.addPoint(6, -2);
			super.addPoint(2, -2);
			super.addPoint(2, -6);
			super.addPoint(-2, -6);
			super.addPoint(-2, -2);
			super.addPoint(-6, -2);
		}
	}
	final class Cross2 extends Polygon {
		private static final long serialVersionUID = 1L;
		public Cross2() {
			super.addPoint(-6, 1);
			super.addPoint(-1, 1);
			super.addPoint(-1, 6);
			super.addPoint(1, 6);
			super.addPoint(1, 1);
			super.addPoint(6, 1);
			//
			super.addPoint(6, -1);
			super.addPoint(1, -1);
			super.addPoint(1, -6);
			super.addPoint(-1, -6);
			super.addPoint(-1, -1);
			super.addPoint(-6, -1);
		}
	}
	final class Cross3 extends Polygon {
		private static final long serialVersionUID = -6388098456486450575L;
		public Cross3() {
			super.addPoint(-6, 6);
			super.addPoint(-4, 6);
			super.addPoint(0, 1);
			super.addPoint(4, 6);
			super.addPoint(6, 6);
			//
			super.addPoint(1, 0);
			super.addPoint(6, -6);
			super.addPoint(4, -6);
			super.addPoint(0, -1);
			//
			super.addPoint(-4, -6);
			super.addPoint(-6, -6);
			super.addPoint(-1, 0);
		}
	}
	final class Cross4 extends Polygon {
		private static final long serialVersionUID = -6388098456486450575L;
		public Cross4() {
			super.addPoint(-6, 6);
			super.addPoint(-6, 6);
			super.addPoint(0, 2);
			super.addPoint(6, 6);
			super.addPoint(6, 6);
			//
			super.addPoint(1, 0);
			super.addPoint(6, -6);
			super.addPoint(6, -6);
			super.addPoint(0, -2);
			//
			super.addPoint(-6, -6);
			super.addPoint(-6, -6);
			super.addPoint(-2, 0);
		}
	}
	final class Rod extends Polygon {
		private static final long serialVersionUID = 1L;
		public Rod() {
			super.addPoint(-2, 6);
			super.addPoint(-2, -6);
			super.addPoint(2, -6);
			super.addPoint(2, 6);
		}
	}	
	final class Rod2 extends Polygon {
		private static final long serialVersionUID = 1L;
		public Rod2() {
			super.addPoint(-4, 6);
			super.addPoint(-4, -6);
			super.addPoint(4, -6);
			super.addPoint(4, 6);
		}
	}	

	protected class DoUpdateValues {
		private Timer timer;
		public DoUpdateValues(double seconds) {
			timer = new Timer();
			timer.schedule(new RemindTask(), (long) (seconds*1000));
		}
		class RemindTask extends TimerTask {
			public void run() {
				timer.cancel();
				updateValues();
			}
		}
	}	

	private enum ObligationOutcome {
		LViol, DViol, LViolViol, DViolViol, LViolFulf, DViolFulf, Fulf;
	}	
}
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package tnel.sinalpha;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.ComponentOrientation;
import java.awt.Font;
import java.awt.Rectangle;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.text.DecimalFormat;
import java.util.Collections;
import java.util.Enumeration;
import java.util.TreeSet;
import java.util.Vector;

import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartMouseEvent;
import org.jfree.chart.ChartMouseListener;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.NumberTickUnit;
import org.jfree.chart.entity.CategoryItemEntity;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.LineAndShapeRenderer;
import org.jfree.data.category.DefaultCategoryDataset;

import ei.onto.ctr.ObligationEvidence;
import ei.service.ctr.ContractEnactment;
import ei.service.ctr.EAgentInfo;
import ei.service.ctr.EvidenceInfo;
import ei.service.ctr.OutcomeGenerator;
import ei.service.ctr.OutcomeGenerator.MappingMethod;
import ei.service.ctr.OutcomeGenerator.Outcome;

/**
 *
 * @author vitorsantos
 */
public class SinalphaCTRGui extends JFrame {

    private static final long serialVersionUID = 1L;
    private JLabel mappingLabel = null;
    private JLabel mappingMeaningLabel = null;
    private JPanel ctrPanel;
    private JPanel mainPanel = new JPanel();
    private JScrollPane contractInfoJScrollPane = null;
    private JScrollPane contractsJScrollPane;
    private JScrollPane obligationsJScrollPane;
    private JTable contractInfoTable = null;
    private JTable contractsTable = null;
    private JTable obligationsTable = null;
    private DefaultTableModel contractInfoTableModel = null;
    private JComboBox mappingsJComboBox = null;
    private JPanel chartPanel = null;
    private ChartPanel chartFrame;
    private JFreeChart chartJFreeChart;
    private DefaultCategoryDataset dataset;
    private CategoryAxis domainaxis;
    private Vector<Double> values = null;
    private DecimalFormat df;
    private SinalphaCTR owner;
    private NumberAxis rangeAxisConstracts;

    /**
     * The default constructor
     */
    public SinalphaCTRGui(SinalphaCTR ctr) {
        super();
        this.owner = ctr;

        df = new DecimalFormat("###.####");

        initialize();
    }

    /**
     * Initializes this object
     */
    private void initialize() {
        ctrPanel = new javax.swing.JPanel();
        ctrPanel.setLayout(null);

        contractInfoJScrollPane = new JScrollPane();
        contractInfoJScrollPane.setBounds(new Rectangle(9, 60, 300, 100));
        contractInfoJScrollPane.setViewportView(getContractInfoTable());
        ctrPanel.add(contractInfoJScrollPane);

        contractsJScrollPane = new JScrollPane();
        contractsJScrollPane.setBounds(new Rectangle(9, 165, 300, 100));
        contractsJScrollPane.setViewportView(getContractsTable());
        ctrPanel.add(contractsJScrollPane);

        obligationsJScrollPane = new JScrollPane();
        obligationsJScrollPane.setBounds(new Rectangle(9, 270, 300, 50));
        obligationsJScrollPane.setViewportView(getObligationsTable());
        ctrPanel.add(obligationsJScrollPane);

        mappingLabel = new JLabel();
        mappingLabel.setBounds(9, 9, 100, 22);
        mappingLabel.setText("Mapping Method: ");
        ctrPanel.add(mappingLabel, null);

        getMappingsJComboBox();
        mappingsJComboBox.setBounds(new Rectangle(115, 9, 160, 25));
        ctrPanel.add(mappingsJComboBox);

        mappingMeaningLabel = new JLabel();
        mappingMeaningLabel.setBounds(new Rectangle(9, 35, 300, 22));
        ctrPanel.add(mappingMeaningLabel);

        chartPanel = getChartEvolutionPanel();
        chartPanel.setBounds(new Rectangle(315, 9, 710, 330));
        ctrPanel.add(chartPanel);

        refreshTrustValues(OutcomeGenerator.MappingMethod.values()[mappingsJComboBox.getSelectedIndex()]);

        this.setResizable(false);
        this.setDefaultCloseOperation(javax.swing.WindowConstants.HIDE_ON_CLOSE);
        this.setSize(1040, 400);
        mainPanel.setLayout(new BorderLayout());
        mainPanel.setLocation(0, 0);
        this.setContentPane(mainPanel);
        mainPanel.add(ctrPanel, null);
        this.setTitle("Computational Trust");
        this.setResizable(false);
        this.setLocationRelativeTo(null);

        addWindowListener(new WindowListener() {
            public void windowActivated(WindowEvent arg0) {
            }

            public void windowClosed(WindowEvent arg0) {
            }

            public void windowClosing(WindowEvent arg0) {
            }

            public void windowDeactivated(WindowEvent arg0) {
            }

            public void windowDeiconified(WindowEvent arg0) {
            }

            public void windowIconified(WindowEvent arg0) {
            }

            public void windowOpened(WindowEvent arg0) {
                OutcomeGenerator oG = new OutcomeGenerator();
                Vector<Outcome> oV = new Vector<Outcome>();
                oV.add(OutcomeGenerator.Outcome.Fulfilled);
                Float fValue = oG.computeOutcome(OutcomeGenerator.MappingMethod.values()[0], oV);
                oV = new Vector<Outcome>();
                oV.add(OutcomeGenerator.Outcome.DeadlineViolated);
                oV.add(OutcomeGenerator.Outcome.Fulfilled);
                Float fdFValue = oG.computeOutcome(OutcomeGenerator.MappingMethod.values()[0], oV);
                oV = new Vector<Outcome>();
                oV.add(OutcomeGenerator.Outcome.DeadlineViolated);
                oV.add(OutcomeGenerator.Outcome.Violated);
                Float fdVValue = oG.computeOutcome(OutcomeGenerator.MappingMethod.values()[0], oV);
                oV = new Vector<Outcome>();
                oV.add(OutcomeGenerator.Outcome.Violated);
                oV.add(OutcomeGenerator.Outcome.Fulfilled);
                Float vFValue = oG.computeOutcome(OutcomeGenerator.MappingMethod.values()[0], oV);
                oV = new Vector<Outcome>();
                oV.add(OutcomeGenerator.Outcome.Violated);
                oV.add(OutcomeGenerator.Outcome.Violated);
                Float vVValue = oG.computeOutcome(OutcomeGenerator.MappingMethod.values()[0], oV);
                mappingsJComboBox.setToolTipText("F: " + fValue + "     FdF: " + fdFValue + "     FdV: " + fdVValue + "     VF: " + vFValue + "     VV: " + vVValue);

                if (contractInfoTable.getRowCount() > 0) {
                    for (int i = 0; i < contractInfoTable.getRowCount(); i++) {
                        if (Integer.parseInt((String) contractInfoTable.getValueAt(i, 1)) > 0) {
                            contractInfoTable.setRowSelectionInterval(i, i);
                            return;
                        }
                    }
                }

            }
        });
    }

    private void getMappingsJComboBox() {
        Vector<String> vec = new Vector<String>();
        for (int i = 0; i < OutcomeGenerator.MappingMethod.values().length; i++) {
            vec.add(OutcomeGenerator.MappingMethod.values()[i].name());
        }
        mappingsJComboBox = new JComboBox(vec);
        comboBoxItemListener actionListener = new comboBoxItemListener();
        mappingsJComboBox.addItemListener(actionListener);
    }

    private class comboBoxItemListener implements ItemListener {
        // This method is called only if a new item has been selected.

        public void itemStateChanged(ItemEvent evt) {
            JComboBox mappings = (JComboBox) evt.getSource();
            if (evt.getStateChange() == ItemEvent.SELECTED) {
                if (contractInfoTable.getSelectedRow() != -1) {
                    OutcomeGenerator oG = new OutcomeGenerator();
                    Vector<Outcome> oV = new Vector<Outcome>();
                    oV.add(OutcomeGenerator.Outcome.Fulfilled);
                    Float fValue = oG.computeOutcome(OutcomeGenerator.MappingMethod.values()[mappings.getSelectedIndex()], oV);

                    oV = new Vector<Outcome>();
                    oV.add(OutcomeGenerator.Outcome.DeadlineViolated);
                    oV.add(OutcomeGenerator.Outcome.Fulfilled);
                    Float fdFValue = oG.computeOutcome(OutcomeGenerator.MappingMethod.values()[mappings.getSelectedIndex()], oV);

                    oV = new Vector<Outcome>();
                    oV.add(OutcomeGenerator.Outcome.DeadlineViolated);
                    oV.add(OutcomeGenerator.Outcome.Violated);
                    Float fdVValue = oG.computeOutcome(OutcomeGenerator.MappingMethod.values()[mappings.getSelectedIndex()], oV);

                    oV = new Vector<Outcome>();
                    oV.add(OutcomeGenerator.Outcome.Violated);
                    oV.add(OutcomeGenerator.Outcome.Fulfilled);
                    Float vFValue = oG.computeOutcome(OutcomeGenerator.MappingMethod.values()[mappings.getSelectedIndex()], oV);

                    oV = new Vector<Outcome>();
                    oV.add(OutcomeGenerator.Outcome.Violated);
                    oV.add(OutcomeGenerator.Outcome.Violated);
                    Float vVValue = oG.computeOutcome(OutcomeGenerator.MappingMethod.values()[mappings.getSelectedIndex()], oV);

                    computeEvidences(MappingMethod.values()[mappings.getSelectedIndex()]);

                    refreshTrustValues(MappingMethod.values()[mappings.getSelectedIndex()]);

                    updateTrustValues2((String) contractInfoTable.getValueAt(contractInfoTable.getSelectedRow(), 0));

                    mappingsJComboBox.setToolTipText("F: " + fValue + "     FdF: " + fdFValue + "     FdV: " + fdVValue + "     VF: " + vFValue + "     VV: " + vVValue);
                    //mappingMeaningLabel.setText("F: "+fValue+"     FdF: "+fdFValue+"     FdV: "+fdVValue+"     VF: "+vFValue+"     VV: "+vVValue);
                } else {
                    mappingMeaningLabel.setText("Please select an option!");
                    mappingMeaningLabel.setForeground(Color.red);
                }
            }
        }
    }

    private void refreshTrustValues(MappingMethod mapMethod) {
        owner.setMapMet(mapMethod);

        Enumeration<String> keys = owner.getCTRRecords().keys();
        keys = Collections.enumeration(new TreeSet<String>(Collections.list(keys))); //assortment 	+

        while (keys.hasMoreElements()) {
            String redordKeys = keys.nextElement();
            owner.getCTRRecords().get(redordKeys).refresh(mapMethod);
            ((DefaultTableModel) contractInfoTable.getModel()).addRow(new String[]{redordKeys.toString(),
                        String.valueOf(owner.getCTRRecords().get(redordKeys).getNContracts()), df.format(owner.getCTRRecords().get(redordKeys).getCTREval(false, null, ""))});
        }
    }

    protected void addValuesRow(MappingMethod mapMethod, String agent, int nContracts, double value) {
        DefaultTableModel tableModel = (DefaultTableModel) contractInfoTable.getModel();

        boolean isContainsAgentBalance = false;
        for (int i = 0; i < contractInfoTableModel.getRowCount(); i++) {
            if (contractInfoTableModel.getValueAt(i, 0).toString().equals(agent)) {
                tableModel.setValueAt(nContracts, i, 1);
                tableModel.setValueAt(df.format(value), i, 2);

                //updates chart only when cell value change of the selected row 
                if (contractInfoTable.getSelectedRow() != -1
                        && contractInfoTableModel.getValueAt(i, 0).toString().equals(contractInfoTableModel.getValueAt(contractInfoTable.getSelectedRow(), 0).toString())) {

                    computeEvidences(MappingMethod.values()[mappingsJComboBox.getSelectedIndex()]);
                    updateDatasetTrustValues((String) contractInfoTable.getValueAt(contractInfoTable.getSelectedRow(), 0));

                    int tNumberOfContracts = Integer.parseInt(contractInfoTable.getValueAt(contractInfoTable.getSelectedRow(), 1).toString());
                    domainaxis.setTickLabelFont(new Font("TIMES", Font.BOLD, pickSizeFont(tNumberOfContracts)));
                    rangeAxisConstracts.setTickUnit(pickNumberTickUnit(tNumberOfContracts));
                }
                isContainsAgentBalance = true;
            }
        }

        if (!isContainsAgentBalance) {
            tableModel.addRow(new String[]{agent, String.valueOf(nContracts), df.format(value)});
        }
    }

    private JTable getContractInfoTable() {
        if (contractInfoTable == null) {
            contractInfoTable = new JTable();
            contractInfoTableModel = new DefaultTableModel(null, new String[]{"Name", "N. of Contracts", "Trustworthiness"}) {
                private static final long serialVersionUID = 1L;

                public boolean isCellEditable(int row, int col) {
                    return false;
                }
            };
            contractInfoTable.setModel(contractInfoTableModel);
            contractInfoTable.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
            contractInfoTable.getTableHeader().setBackground(Color.cyan);
            contractInfoTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            contractInfoTable.getSelectionModel().addListSelectionListener(new ListenerContractInfoTable());
        }
        return contractInfoTable;
    }

    private class ListenerContractInfoTable implements ListSelectionListener {

        public void valueChanged(ListSelectionEvent e) {
            if (!e.getValueIsAdjusting()) {
                DefaultTableModel contractTableModel = new DefaultTableModel(null, new String[]{"Contracts"}) {
                    private static final long serialVersionUID = 1L;

                    public boolean isCellEditable(int row, int col) {
                        return false;
                    }
                };
                contractsTable.setModel(contractTableModel);
                contractsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

                Vector<ContractEnactment> contractHistoric = owner.getCTRRecords().get(contractInfoTable.getValueAt(contractInfoTable.getSelectedRow(), 0)).getContractHistoric();
                for (int i = 0; i < contractHistoric.size(); i++) {
                    ((DefaultTableModel) contractsTable.getModel()).addRow(new String[]{contractHistoric.get(i).getName()});
                }

                computeEvidences(MappingMethod.values()[mappingsJComboBox.getSelectedIndex()]);
                updateDatasetTrustValues((String) contractInfoTable.getValueAt(contractInfoTable.getSelectedRow(), 0));

                if (contractsTable.getRowCount() > 0) {
                    contractsTable.setRowSelectionInterval(0, 0);
                }
            }
        }
    }

    private JTable getContractsTable() {
        if (contractsTable == null) {
            contractsTable = new JTable();
            DefaultTableModel contractTableModel = new DefaultTableModel(null, new String[]{"Contracts"});
            contractsTable.setModel(contractTableModel);
            contractsTable.getTableHeader().setBackground(Color.cyan);
            contractsTable.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
            contractsTable.getSelectionModel().addListSelectionListener(new ListenerContractsTable());
        }
        return contractsTable;
    }

    /**
     * updates obligation and outcome into table
     */
    private class ListenerContractsTable implements ListSelectionListener {

        public void valueChanged(ListSelectionEvent e) {
            if (!e.getValueIsAdjusting()) {
                if (contractInfoTable.getSelectedRow() != -1) {
                    DefaultTableModel obligationTableModel = new DefaultTableModel(null, new String[]{"Obligation", "Outcome"}) {
                        private static final long serialVersionUID = 1L;

                        public boolean isCellEditable(int row, int col) {
                            return false;
                        }
                    };
                    obligationsTable.setModel(obligationTableModel);
                    obligationsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

                    Vector<ContractEnactment> contractHistoric = owner.getCTRRecords().get(contractInfoTable.getValueAt(contractInfoTable.getSelectedRow(), 0)).getContractHistoric();
                    if (contractsTable.getSelectedRow() != -1) {
                        String contractID = (String) contractsTable.getValueAt(contractsTable.getSelectedRow(), 0);
                        Vector<ObligationEvidence> obligations = new Vector<ObligationEvidence>();
                        Vector<Outcome> outcomes = null;
                        for (ContractEnactment cE : contractHistoric) {
                            if (cE.getName().equalsIgnoreCase(contractID)) {
                                obligations = cE.getObligations();
                                outcomes = cE.getState();
                            }
                        }

                        for (int i = 0; i < obligations.size(); i++) {
                            Outcome out = outcomes.get(i);
                            String outcome = out.name();
                            if (out == Outcome.DeadlineViolated) {
                                outcome = "Fulfilled with Delay";
                            }
                            ((DefaultTableModel) obligationsTable.getModel()).addRow(new String[]{obligations.get(i).getFact().split(" ")[0], outcome});
                        }
                    }
                }
            }
        }
    }

    private JTable getObligationsTable() {
        if (obligationsTable == null) {
            obligationsTable = new JTable();
            obligationsTable.getTableHeader().setBackground(Color.cyan);
            obligationsTable.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
            obligationsTable.setEnabled(false);
        }
        return obligationsTable;
    }

    private void updateDatasetTrustValues(String agent) {
        EAgentInfo eAI = owner.getCTRRecords().get(agent);
        Vector<EvidenceInfo> evidences = null;
        if (eAI != null) {
            evidences = eAI.getReports();
        }
        values = new Vector<Double>();
        if (evidences != null) {
            for (EvidenceInfo eI : evidences) {
                values.add(eI.getTrustValue());
            }
        }

        dataset.clear();
        for (int i = 0; i < values.size(); i++) {
            dataset.addValue(values.get(i), (String) contractInfoTable.getValueAt(contractInfoTable.getSelectedRow(), 0), String.valueOf((i) + 1));
        }
    }

    private void updateTrustValues2(String agent) { /* martelada!!! */
        EAgentInfo eAI = owner.getCTRRecords().get(agent);
        Vector<EvidenceInfo> evidences = null;
        if (eAI != null) {
            evidences = eAI.getReports();
        }
        values = new Vector<Double>();
        if (evidences != null) {
            for (EvidenceInfo eI : evidences) {
                values.add(eI.getTrustValue());
            }
        }

        dataset.clear();
        for (int i = 0; i < values.size() - 1; i++) {
            dataset.addValue(values.get(i), (String) contractInfoTable.getValueAt(contractInfoTable.getSelectedRow(), 0), String.valueOf((i) + 1));
        }
    }

    private void computeEvidences(MappingMethod mapMet) {
        for (int j = 0; j < contractInfoTable.getRowCount(); j++) {
            // Clear EvidencesInfo
            owner.getCTRRecords().get(contractInfoTable.getValueAt(j, 0)).setReports(new Vector<EvidenceInfo>());

            // Get Contracts
            Vector<ContractEnactment> contractsAux = owner.getCTRRecords().get(contractInfoTable.getValueAt(j, 0)).getContractHistoric();
            owner.getCTRRecords().get(contractInfoTable.getValueAt(j, 0)).setContractHistoric(new Vector<ContractEnactment>());

            Vector<ContractEnactment> contracts = new Vector<ContractEnactment>();
            // For each one 
            for (int i = 0; i < contractsAux.size(); i++) {
                contracts.add(contractsAux.get(i));
                owner.getCTRRecords().get(contractInfoTable.getValueAt(j, 0)).setContractHistoric(contracts);
                owner.getCTRRecords().get(contractInfoTable.getValueAt(j, 0)).refresh(mapMet);
            }
        }
    }

    private JPanel getChartEvolutionPanel() {
        dataset = new DefaultCategoryDataset();
        chartPanel = new JPanel();

        chartJFreeChart = ChartFactory.createLineChart("", "Contracts", "Trustworthiness", dataset, PlotOrientation.VERTICAL, true, true, false);
//		chartJFreeChart.clearSubtitles();
        chartJFreeChart.getCategoryPlot().getRangeAxis().setRange(0.0, 1.0);
        chartJFreeChart.setBackgroundPaint(this.getBackground());
        //CUSTOMISATION OF THE CHART...
        //chartJFreeChart.setBackgroundPaint(Color.white);

        // get a reference to the plot for further customisation...
        CategoryPlot plot = chartJFreeChart.getCategoryPlot();
        LineAndShapeRenderer renderer = (LineAndShapeRenderer) plot.getRenderer();
        renderer.setBaseShapesVisible(true);

        NumberAxis rangeAxisEvaluation = (NumberAxis) plot.getRangeAxis();
        rangeAxisEvaluation.setRange(0.0, 1.0);
        rangeAxisEvaluation.setTickUnit(new NumberTickUnit(0.1));

        rangeAxisConstracts = (NumberAxis) plot.getRangeAxis();//configureRangeAxes();//DomainAxis();
        //rangeAxisConstracts.setAutoRange(false);
//		rangeAxisConstracts...setAutoRange(true);
//		if (contractInfoTable.getSelectedRow() != -1){
//			rangeAxisConstracts.setRange(0.0, Integer.parseInt((String)contractInfoTable.getValueAt(contractInfoTable.getSelectedRow(), 0)));
//		}
        rangeAxisConstracts.setTickMarkInsideLength(5);

        domainaxis = plot.getDomainAxis();
        //domainaxis.setCategoryMargin(30);
//		domainaxis.setLabelInsets(new RectangleInsets(UnitType.ABSOLUTE, 0, 0, 0, 0));

        chartFrame = new ChartPanel(chartJFreeChart);
        //chartFrame.setPreferredSize(new Dimension(320,300));
        chartFrame.setVisible(true);
        chartFrame.setMouseZoomable(true);

        chartPanel.setLayout(new BorderLayout());
        chartPanel.add(chartFrame);

        chartFrame.addChartMouseListener(new ChartMouseListener() {
            public void chartMouseClicked(ChartMouseEvent evt) {
                if (evt.getTrigger().getModifiers() == 16) {
                    if (evt.getTrigger().getClickCount() == 2) {
                        CategoryItemEntity entity = (CategoryItemEntity) evt.getEntity();
                        if (entity == null) {
                            return;
                        } else {
                            int contractIndex = entity.getCategoryIndex();
                            contractsTable.setRowSelectionInterval(contractIndex, contractIndex);
                        }
                    } else {
                        return;
                    }
                } else {
                    return;
                }
            }

            public void chartMouseMoved(ChartMouseEvent arg0) {
            }
        });
        return chartPanel;
    }

    private int pickSizeFont(int nContracts) {
        int size = 6;
        if (nContracts < 30) {
            size = 9;
        } else if (nContracts >= 30 && nContracts <= 40) {
            size = 8;
        } else if (nContracts >= 40 && nContracts <= 55) {
            size = 7;
        } else if (nContracts >= 55 && nContracts <= 80) {
            size = 6;
        }
        return size;
    }

    private NumberTickUnit pickNumberTickUnit(int nContracts) {
        if (nContracts > 30 && nContracts <= 60) {
            return new NumberTickUnit(4.0f);
        } else if (nContracts > 60 && nContracts <= 120) {
            return new NumberTickUnit(8.0f);
        } else if (nContracts > 120 && nContracts <= 180) {
            return new NumberTickUnit(10.0f);
        } else if (nContracts > 180 && nContracts <= 300) {
            return new NumberTickUnit(20.0f);
        } else if (nContracts > 300) {
            return new NumberTickUnit(30.0f);
        }
        return new NumberTickUnit(1.0f);
    }
}

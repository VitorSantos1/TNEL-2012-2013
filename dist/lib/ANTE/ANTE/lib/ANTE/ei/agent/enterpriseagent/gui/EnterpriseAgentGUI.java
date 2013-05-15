package ei.agent.enterpriseagent.gui;

import ei.ElectronicInstitution;
import ei.agent.enterpriseagent.EnterpriseAgent;
import ei.agent.enterpriseagent.ontology.Attribute;
import ei.agent.enterpriseagent.ontology.Competence;
import ei.agent.enterpriseagent.ontology.Item;
import ei.agent.enterpriseagent.ontology.Need;
import ei.agent.gui.MessagesGUI; 
import ei.agent.gui.MsgViewerGUI;
import ei.contract.ContractWrapper;
import ei.gui.InfoViewerGUI;
import ei.onto.ctr.SendTopAgents;
import ei.onto.negotiation.qfnegotiation.CompetenceCall;
import ei.onto.normenv.management.ContractType;
import ei.onto.normenv.report.Report;
import ei.onto.ontologymapping.AttributeMapping;
import ei.onto.ontologymapping.ItemMapping;
import ei.proto.ctr.CTRInit_SendTopAgents;
import ei.proto.normenv.management.NEManagementInit_SendApplicableNorms;
import ei.proto.normenv.management.NEManagementInit_SendContractTypes;
import ei.service.ctr.OutcomeGenerator;
import ei.service.ctr.OutcomeGenerator.MappingMethod;
import ei.service.negotiation.qfnegotiation.InterestedAgent;
import ei.service.negotiation.qfnegotiation.QFNegotiationParameters;
import ei.util.LimitedSizeLinkedHashMap;

import jade.content.ContentElement;
import jade.content.lang.Codec;
import jade.content.lang.Codec.CodecException;
import jade.content.onto.OntologyException;
import jade.content.onto.UngroundedException;
import jade.core.AID;
import jade.core.Agent;
import jade.lang.acl.ACLMessage;
import jade.util.leap.List;

import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Insets;
import java.awt.Rectangle;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.TreeSet;
import java.util.Vector;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.DefaultListModel;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JScrollPane;
import javax.swing.JButton;
import javax.swing.JTree;

import java.awt.ComponentOrientation;
import javax.swing.JTextField;
import javax.swing.JLabel;
import javax.swing.ListSelectionModel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeSelectionModel;

import java.awt.Color;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.io.IOException;
import javax.swing.SwingConstants;

/**
 * A GUI for an Enterprise Agent. It includes the following panels:
 * <ul>
 * <li>a supply tab, showing the competences of this agent
 * <li>a request tab, showing the needs of this agent
 * <li>a negotiations tab, showing the agent's negotiations
 * <li>a contracts tab, showing the established contracts
 * <li>an ontologies tab, showing mapped ontologies
 * </ul>
 */
public class EnterpriseAgentGUI extends JFrame {
	private static final long serialVersionUID = 2933239584767729747L;
	
	//competences Panel
	private JPanel businessPanel = null;
	private JTree competenceItemJTree = null;	
	private JScrollPane supplyItemTreeViewJScrollPane = null;
	
	//needs Panel 
	private JPanel protocolPanel = null;
	private JPanel needsSubPanel = null;	
	private JPanel trustSubPanel = null;	
	private JPanel negotiationSubPanel; 
	private JLabel topNLabel = null;	
	private JLabel trustUsageLabel = null;
	private JLabel mappingLabel = null;
	private JLabel contractTypeLabel = null;
	private JLabel trustUsageLabel2;
	private JButton trustedAgentsButton = null;
	private JButton startNegotiationButton = null;
	private JComboBox topNComboBox = null;
	private JComboBox mappingMethodComboBox = null;
	private JComboBox contractTypeComboBox= null;
	private JCheckBox useContextualFitnessCheckBox = null;
	private JCheckBox useTrustInPreselectionCheckBox = null;
	private JCheckBox useTrustInProposalEvaluationCheckBox  = null;
	private JCheckBox autoModeCheckBox = null;
//	private JCheckBox useTrustInContractDraftingCheckBox = null;
	private JScrollPane needTreeViewJScrollPane = null;
	private JTree needJTree = null;
	private TrustedAgentsGUI trustedAgentsGUI;
	private Vector<String> contractTypeToolTip;
	private JButton contractTypeNormsButton = null;
	private enum NeedComboBoxOptions {Essential, Yes, No};
	
	//contracts Panel
	private JPanel contractsPanel;
	private JLabel contractsLabel = null;
	private JTable contractsTable = null;
	private JTable contractReportsTable = null;
	private JScrollPane contractsScrollPane1 = null;
	private JScrollPane contractReportsScrollPane1 = null;
	private DefaultTableModel contractsTableModel = null;
	
	//negotiations Panel 
	private JPanel negotiationsPanel;
	private JLabel negotiationsLabel = null;
	private JTable messagesTable = null;
	private JTable messageContentsTable = null;
	private JScrollPane negotiationsScrollPane1 = null;
	private JScrollPane negotiationMessagesScrollPane1 = null;
	private DefaultTableModel messagesTableModel = null;

	//ontologies Panel
	private JPanel ontologiesPanel;
	private JLabel attributesLabel = null;
	private JLabel ownClassNameLabel = null;
	private JLabel foreignClassNameLabel = null;
	private JLabel selectLabel = null;
	private JTextField foreignClassNameTextField = null;
	private JTextField ownClassNameTextField = null;
	private JTable attributesTable = null;
	private JScrollPane attributesJScrollPane = null;
	private JScrollPane mappingClassesPanel = null;	
	private JList mappingClassesList = null;
	private DefaultListModel ontologyMappingsList = new DefaultListModel();
	
	//parameters Panel
	private JPanel parametersPanel;	
	private JTable parametersTable = null;	
	private JScrollPane parametersJScrollPane = null;
	private MessagesGUI messagesGUIPanel = null;

	private JButton killButton = null;

	private JTabbedPane jTabbedPane = null;
	private JPanel mainPanel = new JPanel();
	
	protected EnterpriseAgent owner;
	
	/**
	 * The default constructor
	 */
	public EnterpriseAgentGUI(EnterpriseAgent owner) {
		super();
		this.owner = owner;
		
		initialize();
	}

	/**
	 * Initializes this object
	 */
	private void initialize() {
		this.setResizable(false);
		this.setDefaultCloseOperation(javax.swing.WindowConstants.HIDE_ON_CLOSE);
		this.setSize(608, 400);
		this.setTitle(owner.getLocalName()+" -- Enterprise Agent");
		this.setLocationRelativeTo(null);
		
		mainPanel.setLayout(null);
		mainPanel.add(getJTabbedPane(),null);
		mainPanel.add(getKillButton(), null);
		this.setContentPane(mainPanel);
	}
	
	// business panel
	private JPanel getBusinessPanel() {
		if (businessPanel == null) { 
			businessPanel = new javax.swing.JPanel();
			businessPanel.setLayout(new FlowLayout(FlowLayout.LEADING));
			businessPanel.add(getKillButton(), null);
			
			competenceItemJTree = new JTree(createNodeCompetences(owner.getCompetences()));
			competenceItemJTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
			supplyItemTreeViewJScrollPane = new JScrollPane(competenceItemJTree);
			supplyItemTreeViewJScrollPane.setPreferredSize(new Dimension(285,302));
			businessPanel.add(supplyItemTreeViewJScrollPane, null);

			needJTree = new JTree(createNodeNeeds(owner.getNeeds()));
			needJTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
			needTreeViewJScrollPane = new JScrollPane(needJTree);
			needTreeViewJScrollPane.setPreferredSize(new Dimension(285,302));
			businessPanel.add(needTreeViewJScrollPane, null);
	        
		}
		return businessPanel;
	}
	
	// protocol panel
	private JPanel getProtocolPanel() {
		if (protocolPanel == null && (owner.getNeeds() != null && owner.getNeeds().size() > 0) ) { 
			protocolPanel = new JPanel();
			protocolPanel.setLayout(null);
			protocolPanel.setSize(600, 580);
	        protocolPanel.add(getNeedsSubPanel(), null);
	        protocolPanel.add(getTrustSubPanel(), null);		
	        protocolPanel.add(getNegotiationSubPanel(), null);
			// request contract types
			AID normenv = owner.fetchAgent(ElectronicInstitution.SRV_NORMATIVE_ENVIRONMENT, false);
			if(normenv != null) {
				owner.addBehaviour(new FillContractTypes(owner, normenv));
			} else {
				owner.logErr("Error: Normative Environment Agent not found");
			}
			
		}			
		return protocolPanel;
	}

	// negotiation messages panel	
	private JPanel getNegotiationMessagesPanel() {
		if (negotiationsPanel == null) {
			negotiationsPanel = new JPanel();
			negotiationsPanel.setLayout(null);
			negotiationsLabel = new JLabel();
			negotiationsLabel.setBounds(15, 12, 113, 17);
			negotiationsLabel.setText("Negotiations:");
			negotiationsPanel.add(negotiationsLabel, null);
			negotiationMessagesScrollPane1 = new JScrollPane();
			negotiationMessagesScrollPane1.setBounds(new Rectangle(15, 110, 565, 195));
			negotiationMessagesScrollPane1.setViewportView(getMessageContentsTable());
			negotiationsPanel.add(negotiationMessagesScrollPane1, null);
			negotiationsScrollPane1 = new JScrollPane();
			negotiationsScrollPane1.setBounds(new Rectangle(15, 35, 565, 70));
			negotiationsScrollPane1.setViewportView(getMessagesTable());			
			negotiationsPanel.add(negotiationsScrollPane1, null);
			
			refreshMessagesTable();
		}
		return negotiationsPanel;
	}

	// contracts panel	
	private JPanel getContractsPanel() {
		if (contractsPanel == null) {
			contractsPanel = new JPanel();
			contractsLabel = new JLabel();
			contractsPanel.setLayout(null);
			contractsLabel.setBounds(16, 12, 113, 17);
			contractsLabel.setText("Contracts:");
			contractsPanel.add(contractsLabel, null);
			contractsScrollPane1 = new JScrollPane();
			contractsScrollPane1.setBounds(new Rectangle(15, 35, 565, 70));
			contractsScrollPane1.setViewportView(getContractsTable());
			contractsPanel.add(contractsScrollPane1, null);
			contractReportsScrollPane1 = new JScrollPane(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED ,JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
			contractReportsScrollPane1.setBounds(new Rectangle(15, 110, 565, 197));
			contractReportsScrollPane1.setViewportView(getContractReportsTable());
			contractsPanel.add(contractReportsScrollPane1, null);
			
			refreshContractsTable();
		}
		return contractsPanel; 
	}	
	// ontologies panel	
	private JPanel getOntologiesPanel() {
		if (ontologiesPanel == null) {
			ontologiesPanel = new JPanel();
			ontologiesPanel.setLayout(null);
			selectLabel = new JLabel();
			selectLabel.setBounds(new Rectangle(12, 14, 148, 15));
			selectLabel.setText("Foreign item(s)");
			ontologiesPanel.add(selectLabel, null);
			
			ontologiesPanel.add(getMappingClassesPanel(), null);
			
			foreignClassNameLabel = new JLabel();
			foreignClassNameLabel.setBounds(new Rectangle(176, 36, 151, 22));
			foreignClassNameLabel.setText("Foreign Item Class Name");
			ontologiesPanel.add(foreignClassNameLabel, null);
			
			ownClassNameLabel = new JLabel();
			ownClassNameLabel.setBounds(new Rectangle(331, 35, 151, 24));
			ownClassNameLabel.setText("Own Item Class Name");
			ontologiesPanel.add(ownClassNameLabel, null);
			
			foreignClassNameTextField = new JTextField();
			foreignClassNameTextField.setBounds(new Rectangle(177, 60, 151, 20));
			foreignClassNameTextField.setEditable(false);
			foreignClassNameTextField.setText("");
			ontologiesPanel.add(foreignClassNameTextField, null);
			
			ownClassNameTextField = new JTextField();
			ownClassNameTextField.setBounds(new Rectangle(332, 60, 150, 20));
			ownClassNameTextField.setEditable(false);
			ownClassNameTextField.setText("");
			ontologiesPanel.add(ownClassNameTextField, null);
			
			attributesLabel = new JLabel();
			attributesLabel.setBounds(new Rectangle(177, 90, 88, 18));
			attributesLabel.setText("Attributes");
			ontologiesPanel.add(attributesLabel, null);
			
			attributesJScrollPane = new JScrollPane();
			attributesJScrollPane.setBounds(new Rectangle(177, 112, 380, 177));
			attributesJScrollPane.setViewportView(getAttributesTable());
			ontologiesPanel.add(attributesJScrollPane);		
			
			refreshOntologyMappings();
		}
		return ontologiesPanel;
	}
	
	private JPanel getParametersPanel() {
		if (parametersPanel == null) {
			parametersPanel = new JPanel();
			parametersPanel.setLayout(null);		
			parametersJScrollPane = new JScrollPane();
			parametersJScrollPane.setBounds(new Rectangle(9, 12, 570, 295));
			parametersJScrollPane.setViewportView(getParametersTable());
			parametersPanel.add(parametersJScrollPane);		
		}
		return parametersPanel;
	}
	
	// messages panel
	private JPanel getMessagesGUIPanel() {
		if (messagesGUIPanel == null) {
			//messagesGUIPanel = new MessagesGUI(owner);
		}
		return messagesGUIPanel;
	}	
	
	private JTabbedPane getJTabbedPane() {
		if (jTabbedPane == null) {
			jTabbedPane = new JTabbedPane();
			jTabbedPane.setBounds(5, 4, 588, 338);
			jTabbedPane.setOpaque(true);
			
			jTabbedPane.addTab("Business",getBusinessPanel());
			
			jTabbedPane.addTab("Q-Negotiation",getProtocolPanel());
			
			jTabbedPane.addTab("Negotiations",getNegotiationMessagesPanel());
			jTabbedPane.addTab("Contracts",getContractsPanel());
			jTabbedPane.addTab("Ontologies",getOntologiesPanel());
			jTabbedPane.addTab("Parameters",getParametersPanel());	
			jTabbedPane.addTab("Messages", getMessagesGUIPanel());
			
			//remove all null panels 
			for (int i=0; i < jTabbedPane.getTabCount(); i++) {
				if (jTabbedPane.getComponentAt(i) == null) {
					jTabbedPane.removeTabAt(i);
				}
			}
		}
		return jTabbedPane;
	}	
	
	/**
	 * Inserts a new message into the list
	 * @param rec	the message to add
	 */
//	public void insertMsg(ACLMessage rec){
//		messagesGUIPanel.insertMsg(rec);
//	}

	private DefaultMutableTreeNode createNodeNeeds(Vector<Need> needs) {
		DefaultMutableTreeNode needTreeRoot = new DefaultMutableTreeNode("Needs");
		for(int c=0; c<needs.size(); c++) {
			Need need = needs.get(c);
			String name = "[" + need.getType() + "] " + need.getName();

			DefaultMutableTreeNode needTree = new DefaultMutableTreeNode(name);
			setAttributeValuesInTree(needTree, need);
			needTreeRoot.add(needTree);
		}
		return needTreeRoot;
	}

	private DefaultMutableTreeNode createNodeCompetences(Vector<Competence> competences) {
		DefaultMutableTreeNode competenceTreeRoot = new DefaultMutableTreeNode("Competences");
		for(int c=0; c<competences.size(); c++) {
			Competence competence = competences.get(c);
			String name = "[" + competence.getType() + "] " + competence.getName();

			DefaultMutableTreeNode competenceTree = new DefaultMutableTreeNode(name);
			setAttributeValuesInTree(competenceTree, competence);
			competenceTreeRoot.add(competenceTree);
		}
		return competenceTreeRoot;
	}
	
	private void setAttributeValuesInTree(DefaultMutableTreeNode itemTree, Item item) {
		for(int i=0;i<item.getNumberOfAttributes();i++){
			Attribute attribute = (Attribute) item.getAttributes().get(i);			
			DefaultMutableTreeNode attributeTree = new DefaultMutableTreeNode(attribute.getName());
			itemTree.add(attributeTree);
			String domain = "";
			if(attribute.isDiscrete()) {		
				for(int d=0; d<attribute.getDiscreteDomain().size(); d++) {
					domain += (String)attribute.getDiscreteDomain().get(d)+" ";
				}
			} else {
				domain =  attribute.getContinuousDomainMin() + " to " + attribute.getContinuousDomainMax();
			}
			attributeTree.add(new DefaultMutableTreeNode(attribute.getType()));
			attributeTree.add(new DefaultMutableTreeNode(attribute.getPreferredValue().toString() + "(Preferred value)"));
			attributeTree.add(new DefaultMutableTreeNode(domain));
		}
	}

	private JPanel getNeedsSubPanel() {
		if(needsSubPanel == null) {
			needsSubPanel = new JPanel();
			needsSubPanel.setLayout(null);
			needsSubPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createLineBorder(
					new java.awt.Color(102, 102, 102)), "Needs ", javax.swing.border.TitledBorder.CENTER, javax.swing.border.TitledBorder.DEFAULT_POSITION) );
			needsSubPanel.setBounds(5, 12, 265, 295);
			needsSubPanel.setBackground(Color.white);
			
			for(int i=0; i < owner.getNeeds().size(); i++) {
				Need need = owner.getNeeds().get(i);
				JTree treeNeedName = new JTree(new DefaultMutableTreeNode(need.getType()+":"));
				treeNeedName.setBounds(5, (i==0?20:20+(i*28)), 80, 22);
				needsSubPanel.add(treeNeedName,null);			
				JComboBox needComboBox = new JComboBox(NeedComboBoxOptions.values());
				needComboBox.setBounds(120, (i==0?20:20+(i*28)), 100, 22);
				needComboBox.setBackground(Color.white);
				if(!need.isNegotiate()) {
					needComboBox.setSelectedIndex(NeedComboBoxOptions.No.ordinal());
					needComboBox.setToolTipText("Do not negotiate this need");
				} else if(need.isEssential()) {
					needComboBox.setSelectedIndex(NeedComboBoxOptions.Essential.ordinal());
					needComboBox.setToolTipText("This need is critical fo negotiation");
				} else {
					needComboBox.setSelectedIndex(NeedComboBoxOptions.Yes.ordinal());
					needComboBox.setToolTipText("Negotiate this need");
				}
				needComboBox.setName(need.getType());
				needComboBox.addItemListener(new ItemListener() {
					public void itemStateChanged(ItemEvent itemEvent) {
						if(itemEvent.getStateChange() == ItemEvent.SELECTED) {
							JComboBox needComboBox = (JComboBox) itemEvent.getSource();
							int option = needComboBox.getSelectedIndex();
							for(Need need : owner.getNeeds()) {
								if(need.getType().equals(needComboBox.getName())) {
									switch(NeedComboBoxOptions.values()[option]) {
										case Essential:
											need.setNegotiate(true);
											need.setEssential(true);
											needComboBox.setToolTipText("This need is critical fo negotiation");
											break;
										case Yes:
											need.setNegotiate(true);
											need.setEssential(false);
											needComboBox.setToolTipText("Negotiate this need");
											break;
										case No:
											need.setNegotiate(false);
											needComboBox.setToolTipText("Do not negotiate this need");
											break;
									}
								}
							}
						}
					}
				});
				needsSubPanel.add(needComboBox, null);	
			}
		}
		return needsSubPanel;
	}	
	
	private JPanel getTrustSubPanel() {
		if(trustSubPanel == null) {
			trustSubPanel = new JPanel();
			trustSubPanel.setLayout(null);
			trustSubPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createLineBorder(
					new java.awt.Color(102, 102, 102)), "Trust (Sinalpha)", javax.swing.border.TitledBorder.CENTER, javax.swing.border.TitledBorder.DEFAULT_POSITION) );
			trustSubPanel.setBounds(275, 06, 305, 105);

			//topN
			topNLabel = new JLabel();
			topNLabel.setBounds(9, 30, 40, 22);
			topNLabel.setText("Top N: ");
			trustSubPanel.add(topNLabel,null);			
			Vector<String> tops = new Vector<String>();
			tops.add("All");
			tops.add("TNH");
			for (int i=1; i <= 10; i++) {
				tops.add(""+i);
			}
			for (int i=20; i <= 50; i+=10) {
				tops.add(""+i);
			}
			topNComboBox = new JComboBox(tops);
			topNComboBox.setBounds(70, 30, 115, 22);
			topNComboBox.setBackground(Color.white);
			if(owner.getConfigurationArguments().getProperty(QFNegotiationParameters.TOP_N) != null) {
				topNComboBox.setSelectedItem(owner.getConfigurationArguments().getProperty(QFNegotiationParameters.TOP_N));
			} else {
				topNComboBox.setSelectedIndex(0);
			}
			topNComboBox.addItemListener(new ItemListener() {
				public void itemStateChanged(ItemEvent i) {
					if(i.getStateChange() == ItemEvent.SELECTED) {
						if(!topNComboBox.getSelectedItem().equals("All")) {
							useTrustInPreselectionCheckBox.setSelected(true);
						} else {
							useTrustInPreselectionCheckBox.setSelected(false);
						}
						owner.getConfigurationArguments().setProperty(QFNegotiationParameters.TOP_N, String.valueOf(getTopNAgents()));
					}
				}
			});
			trustSubPanel.add(topNComboBox, null);	
			
			mappingLabel = new JLabel();
			mappingLabel.setBounds(9, 62, 60, 22);
			mappingLabel.setText("Mapping: ");
			trustSubPanel.add(mappingLabel,null);	
			
			Vector<String> vec = new Vector<String>();
			for(int i = 0; i < MappingMethod.values().length; i++) {
				vec.add(MappingMethod.values()[i].name());
			}
			mappingMethodComboBox = new JComboBox(vec);
			mappingMethodComboBox.setBounds(new Rectangle(70, 62, 115, 22));
			mappingMethodComboBox.setBackground(Color.white);
			if(owner.getConfigurationArguments().getProperty(QFNegotiationParameters.MAPPING_METHOD) != null) {
				mappingMethodComboBox.setSelectedItem(MappingMethod.values()[Integer.parseInt(owner.getConfigurationArguments().getProperty(QFNegotiationParameters.MAPPING_METHOD))].name());
			} else {
				mappingMethodComboBox.setSelectedItem(QFNegotiationParameters.MAPPING_METHOD_DEFAULT.name());
			}
			mappingMethodComboBox.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent actionEvent) {
					if(mappingMethodComboBox.getSelectedIndex() != -1) {
						owner.getConfigurationArguments().setProperty(QFNegotiationParameters.MAPPING_METHOD, String.valueOf(MappingMethod.values()[mappingMethodComboBox.getSelectedIndex()].ordinal()));
					}
				}
			});
			trustSubPanel.add(mappingMethodComboBox, null);
			
			trustSubPanel.add(getViewTrustedAgents(), null);
		}

		return trustSubPanel;
	}
	
	private JPanel getNegotiationSubPanel() {
		if(negotiationSubPanel == null) {
			negotiationSubPanel = new JPanel();
			negotiationSubPanel.setLayout(null);
			negotiationSubPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createLineBorder(
					new java.awt.Color(102, 102, 102)), "Negotiation", javax.swing.border.TitledBorder.CENTER, javax.swing.border.TitledBorder.DEFAULT_POSITION) );
			negotiationSubPanel.setBounds(275, 130, 305, 178);

			trustUsageLabel = new JLabel();
			trustUsageLabel.setBounds(9, 20, 85, 22);
			trustUsageLabel.setText("Trust Usage (");
			negotiationSubPanel.add(trustUsageLabel,null);	
			
			useContextualFitnessCheckBox = new JCheckBox();
			useContextualFitnessCheckBox.setBounds(84, 20, 128, 22);
			useContextualFitnessCheckBox.setText("ContextualFitness");
			try {
				useContextualFitnessCheckBox.setSelected(Boolean.parseBoolean(owner.getConfigurationArguments().getProperty(QFNegotiationParameters.USE_CONTEXTUAL_FITNESS)));
			} catch(Exception e) {
				useContextualFitnessCheckBox.setSelected(QFNegotiationParameters.USE_CONTEXTUAL_FITNESS_DEFAULT);
			}
			useContextualFitnessCheckBox.setEnabled(true);
			useContextualFitnessCheckBox.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
			useContextualFitnessCheckBox.addChangeListener(new ChangeListener() {
				@Override
				public void stateChanged(ChangeEvent e) {
					owner.getConfigurationArguments().setProperty(QFNegotiationParameters.USE_CONTEXTUAL_FITNESS, String.valueOf(useContextualFitnessCheckBox.isSelected()));
				}
			});
			negotiationSubPanel.add(useContextualFitnessCheckBox, null);		
			
			trustUsageLabel2 = new JLabel();
			trustUsageLabel2.setBounds(212, 20, 5, 22);
			trustUsageLabel2.setText(")");
			negotiationSubPanel.add(trustUsageLabel2,null);	
			
			useTrustInPreselectionCheckBox = new JCheckBox();
			useTrustInPreselectionCheckBox.setBounds(28, 45, 165, 22); //y 38 before  PAAMS 2012
			useTrustInPreselectionCheckBox.setText("Preselection (Top N)");
			try {
				useTrustInPreselectionCheckBox.setSelected(Boolean.parseBoolean(owner.getConfigurationArguments().getProperty(QFNegotiationParameters.USE_TRUST_IN_PRESELECTION)));
			} catch(Exception e) {
				useTrustInPreselectionCheckBox.setSelected(QFNegotiationParameters.USE_TRUST_IN_PRESELECTION_DEFAULT);
			}
			useTrustInPreselectionCheckBox.setEnabled(true);
			useTrustInPreselectionCheckBox.addChangeListener(new ChangeListener() {
				@Override
				public void stateChanged(ChangeEvent e) {
					owner.getConfigurationArguments().setProperty(QFNegotiationParameters.USE_TRUST_IN_PRESELECTION, String.valueOf(useTrustInPreselectionCheckBox.isSelected()));
				}
			});
			negotiationSubPanel.add(useTrustInPreselectionCheckBox, null);
			
			useTrustInProposalEvaluationCheckBox = new JCheckBox();
			useTrustInProposalEvaluationCheckBox.setBounds(28, 68, 165, 22); //y 58 before  PAAMS 2012
			useTrustInProposalEvaluationCheckBox.setText("Proposal evaluation");
			try {
				useTrustInProposalEvaluationCheckBox.setSelected(Boolean.parseBoolean(owner.getConfigurationArguments().getProperty(QFNegotiationParameters.USE_TRUST_IN_PROPOSAL_EVALUATION)));
			} catch(Exception e) {
				useTrustInProposalEvaluationCheckBox.setSelected(QFNegotiationParameters.USE_TRUST_IN_PROPOSAL_EVALUATION_DEFAULT);
			}
			useTrustInProposalEvaluationCheckBox.setEnabled(true);
			useTrustInProposalEvaluationCheckBox.addChangeListener(new ChangeListener() {
				@Override
				public void stateChanged(ChangeEvent e) {
					owner.getConfigurationArguments().setProperty(QFNegotiationParameters.USE_TRUST_IN_PROPOSAL_EVALUATION, String.valueOf(useTrustInProposalEvaluationCheckBox.isSelected()));
				}
			});
			negotiationSubPanel.add(useTrustInProposalEvaluationCheckBox, null);

//			useTrustInContractDraftingCheckBox = new JCheckBox();
//			useTrustInContractDraftingCheckBox.setBounds(28, 78, 165, 22);
//			useTrustInContractDraftingCheckBox.setText("Contract Drafting");
//			try {
//				useTrustInContractDraftingCheckBox.setSelected(Boolean.parseBoolean(owner.getConfigurationArguments().getProperty(QFNegotiationParameters.USE_TRUST_IN_CONTRACT_DRAFTING)));
//			} catch(Exception e) {
//				useTrustInContractDraftingCheckBox.setSelected(QFNegotiationParameters.USE_TRUST_IN_CONTRACT_DRAFTING_DEFAULT);
//			}
//			useTrustInContractDraftingCheckBox.setEnabled(true);
//			useTrustInContractDraftingCheckBox.addChangeListener(new ChangeListener() {
//				@Override
//				public void stateChanged(ChangeEvent e) {
//					owner.getPropertiesArguments().setProperty(QFNegotiationParameters.USE_TRUST_IN_CONTRACT_DRAFTING, String.valueOf(useTrustInContractDraftingCheckBox.isSelected()));
//				}
//			});
//			negotiationPanel.add(useTrustInContractDraftingCheckBox, null);

			contractTypeToolTip = new Vector<String>();
			contractTypeLabel = new JLabel();
			contractTypeLabel.setText("Contract Type:");
			contractTypeLabel.setName("ctLabel");
			contractTypeLabel.setBounds(9, 109, 110, 22); 
			negotiationSubPanel.add(contractTypeLabel,null);
			
			contractTypeComboBox = new JComboBox();
			contractTypeComboBox.setBounds(94, 109, 135, 22);
			contractTypeComboBox.addItem(QFNegotiationParameters.CONTRACT_TYPE_DEFAULT);
			contractTypeComboBox.setBackground(Color.white);
			contractTypeComboBox.addItemListener(new ItemListener() {
				public void itemStateChanged(ItemEvent i) {
					if(i.getStateChange() == ItemEvent.SELECTED) {
						owner.getConfigurationArguments().setProperty(QFNegotiationParameters.CONTRACT_TYPE, contractTypeComboBox.getSelectedItem().toString());
						
						if(contractTypeComboBox.getSelectedIndex() != -1) {
							if(contractTypeToolTip.size() > 0) {
								contractTypeComboBox.setToolTipText(contractTypeToolTip.get(contractTypeComboBox.getSelectedIndex()).toString());
							}
						}
					}
				}
			});
			negotiationSubPanel.add(contractTypeComboBox,null);
			
			negotiationSubPanel.add(getContractTypeNormsButton(), null);
			
			negotiationSubPanel.add(getStartNegotiationButton(), null);
			
			autoModeCheckBox = new JCheckBox();
			autoModeCheckBox.setBounds(210, 142, 90, 22);
			autoModeCheckBox.setText("Auto mode");
			try {
				autoModeCheckBox.setSelected(Boolean.parseBoolean(owner.getConfigurationArguments().getProperty(QFNegotiationParameters.AUTO_MODE)));
			} catch(Exception e) {
				autoModeCheckBox.setSelected(QFNegotiationParameters.AUTO_MODE_DEFAULT);
			}
			autoModeCheckBox.setEnabled(true);
			autoModeCheckBox.addItemListener(new ItemListener() {
				public void itemStateChanged(ItemEvent e) {
					if(e.getStateChange() == ItemEvent.SELECTED || e.getStateChange() == ItemEvent.DESELECTED) {
						owner.setAutoRequestMode(autoModeCheckBox.isSelected());
					}
				}
			});					
			negotiationSubPanel.add(autoModeCheckBox,null);
		}

		return negotiationSubPanel;
	}
	
	public void setAutoModeCheckBox(boolean b) {
		autoModeCheckBox.setSelected(b);
	}
	
	private JButton getContractTypeNormsButton(){
		if (contractTypeNormsButton == null) {
			contractTypeNormsButton = new JButton();
			contractTypeNormsButton.setText("Norms");
			contractTypeNormsButton.setEnabled(true);
			contractTypeNormsButton.setMargin(new Insets(0,0,0,0));
			contractTypeNormsButton.setBounds(235, 109, 50, 22); //.setLocation(245, 198);
			contractTypeNormsButton.setName("normsButton");
			contractTypeNormsButton.addMouseListener(new java.awt.event.MouseAdapter(){
				public void mouseReleased(java.awt.event.MouseEvent e) {
					if(contractTypeComboBox.getSelectedIndex() != -1){
						AID normenv = owner.fetchAgent(ElectronicInstitution.SRV_NORMATIVE_ENVIRONMENT);
						if(normenv != null) {
							owner.addBehaviour(new ShowNorms(owner, normenv, contractTypeComboBox.getSelectedItem().toString()));
						} else {
							owner.logErr("Error: Normative Environment Agent not found");
						}
					}
				}
			});
		}
		return contractTypeNormsButton;
	}
	
	private class ShowNorms extends NEManagementInit_SendApplicableNorms {
		private static final long serialVersionUID = 1L;

		public ShowNorms(Agent agent, AID normenv, String contractType) {
			super(agent, normenv, contractType);
		}
		
		public int onEnd() {
			InfoViewerGUI infoGui = new InfoViewerGUI("Applicable norms for " + contractTypeComboBox.getSelectedItem().toString(), getApplicableNorms());
			infoGui.setVisible(true);
			return 0;
		}
	}	

	/**
	 * Initializes startNegotiationButton
	 */    
	private JButton getStartNegotiationButton() {
		if (startNegotiationButton == null) {
			startNegotiationButton = new JButton();
			startNegotiationButton.setText("Start Negotiation");
			startNegotiationButton.setEnabled(true);
			startNegotiationButton.setBounds(9, 142, 200, 22);
			startNegotiationButton.setName("startButton");
			startNegotiationButton.addMouseListener(new java.awt.event.MouseAdapter() { 
				public void mouseReleased(java.awt.event.MouseEvent e) {  
					owner.requestNegotiate();
				}
			});
		}
		return startNegotiationButton;
	}

	private int getTopNAgents() {
		String s = topNComboBox.getSelectedItem().toString();
		//Sets the percent of agents to negotiate
		if (s.equals("All")) {
			return SendTopAgents.ALL;
		} else if(s.equals("TNH")) {
			return SendTopAgents.NO_HANDICAP;
		}  
		return Integer.parseInt(s);
	}
	
	/**
	 * This method initializes View	
	 * @return javax.swing.JButton	
	 */
	private JButton getViewTrustedAgents() {
		if (trustedAgentsButton == null) {
			trustedAgentsButton = new JButton();
			trustedAgentsButton.setBounds(205, 48, 80, 25);
			trustedAgentsButton.setText("<html><center>Preview</center></html>");
			trustedAgentsButton.setMargin(new Insets(1,1,1,1));
			trustedAgentsButton.addMouseListener(new java.awt.event.MouseAdapter() { 
				public void mouseReleased(java.awt.event.MouseEvent e) {
					AID ctr = owner.fetchAgent(ElectronicInstitution.SRV_CTR, false);
					Need need = (Need)owner.getNeeds().get(0);
					if(ctr != null) {
						owner.addBehaviour(new GetTopAgents(owner, owner.getAID(), null, need, getTopNAgents(), true, Integer.parseInt((String) need.getAttribute("quantity").getPreferredValue()), Integer.parseInt(((String)need.getAttribute("delivery").getPreferredValue())), MappingMethod.values()[mappingMethodComboBox.getSelectedIndex()]));
					} else {
						owner.logErr("Error: CTR Agent not found");
					}			
				}
			});
		}
		return trustedAgentsButton;
	}
	
	private void refreshContractsTable() {
		Vector<String> contractIds = owner.getContractIds();
		for(int i=0; i<contractIds.size(); i++) {
			addContractRow(contractIds.get(i));
		}
	}
	
	public void addContractRow(String cid){
		DefaultTableModel tableModel = (DefaultTableModel) contractsTable.getModel();
		tableModel.addRow(new String [] {cid});
		
		if (owner.getConfigurationArguments().containsKey("maxEntries_contracts") && 
				(contractsTable.getRowCount() > Integer.parseInt(owner.getConfigurationArguments().getProperty("maxEntries_contracts"))) ) {
			
			tableModel.removeRow(0);
			if (contractsTable.getSelectedRow() != -1) {
				contractsTable.setRowSelectionInterval(contractsTable.getSelectedRow(), contractsTable.getSelectedRow());
			} else {
				contractsTable.setRowSelectionInterval(0, 0);
			}
		}
		
		if (contractsTable.getRowCount() == 1 ) {
			contractsTable.setRowSelectionInterval(0, 0);
		}				
	}	
	/**
	 * This method initializes the contracts table	
	 * @return javax.swing.JTable	
	 */
	private JTable getContractsTable() {
		if (contractsTable == null) {
			contractsTable = new JTable();
			contractsTableModel =  new DefaultTableModel(null, new String [] {"Contract ID"}) {  
				private static final long serialVersionUID = 1L;
				public boolean isCellEditable(int row, int col) {  
					return false;  
				}  
			};  			
			contractsTable.setModel(contractsTableModel);
			contractsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			contractsTable.addMouseListener(new java.awt.event.MouseAdapter() {
				public void mouseClicked(java.awt.event.MouseEvent e) {
					if(contractsTable.getSelectedRow() != -1){
						// double click opens the xml contract in the system viewer
						if(e.getClickCount() == 2) {
							// create a file with the xml contract, if possible inside a "tmp" folder
							ContractWrapper cw = owner.getContract(contractsTable.getValueAt(contractsTable.getSelectedRow(), 0).toString());
							if(cw != null) {
								String filename = "";
								File theDir = new File("tmp");
								if (theDir.exists() || theDir.mkdir()) {
									filename = "tmp" + System.getProperty("file.separator");
								}
								filename += "tmp"+System.currentTimeMillis()+".xml";
								cw.save(filename, true);
								// open the file in the system viewer
								try {
									Desktop desktop = null;
									if (Desktop.isDesktopSupported()) {
										desktop = Desktop.getDesktop();
									}
									desktop.open(new File(filename));
								} catch (IOException ioe) {
									ioe.printStackTrace();
								}
							}
						}
					}

				}
			});
			contractsTable.getSelectionModel().addListSelectionListener(new ListenerContractsTable());			
		}
		return contractsTable;
	}
	private class ListenerContractsTable implements ListSelectionListener {
		public void valueChanged(ListSelectionEvent e) {
			if (!e.getValueIsAdjusting()) {
				if (contractsTable.getSelectedRow() != -1) {
					refreshContractReportsTable();
				}
				
			}
		}
	}	
	public void refreshContractReportsTable(String cid) {
		if((contractsTable.getSelectedRow() != -1) && (contractsTable.getValueAt(contractsTable.getSelectedRow(), 0).toString().equals(cid))) {
			refreshContractReportsTable();
		}
	}
	public void refreshContractReportsTable() {
		Vector<Report> contractReports = owner.getContractReports(contractsTable.getValueAt(contractsTable.getSelectedRow(), 0).toString());
		if(contractReports != null) {
			DefaultTableModel contractReportsTableModel =  new DefaultTableModel(null, 
					new String [] {"                                                                                      Events"}) {  
				private static final long serialVersionUID = 1L;
				public boolean isCellEditable(int row, int col) {  
					return false;  
				}  
			};
			
			contractReportsTable.setModel(contractReportsTableModel);			
			for(int i=0; i<contractReports.size(); i++){
				((DefaultTableModel) contractReportsTable.getModel()).addRow(new String [] {contractReports.get(i).toString()});
			}			
			contractReportsTable.getColumnModel().getColumn(0).setPreferredWidth(1700);
		} 
	}	
	/**
	 * This method initializes the contract messages table	
	 * @return javax.swing.JTable	
	 */
	private JTable getContractReportsTable() {
		if (contractReportsTable == null) {
			contractReportsTable = new JTable();
			((JLabel) contractReportsTable.getTableHeader().getDefaultRenderer()).setHorizontalAlignment(JLabel.LEFT);
			contractReportsTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
			contractReportsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			contractReportsTable.addMouseListener(new java.awt.event.MouseAdapter() {
//				public void mouseClicked(java.awt.event.MouseEvent e) {
//					//double click on the report to see details
//					if(e.getClickCount() == 2){
//						if(contractReportsTable.getSelectedRow() != -1){
//							Vector<Report> c = owner.getContractReports(contractsTable.getValueAt(contractsTable.getSelectedRow(), 0).toString());
//							ReportViewerGUI rv = new ReportViewerGUI(c.get(contractReportsTable.getSelectedRow()),messagesGUIPanel);
//							rv.setVisible(true);
//						}
//					}
//				}
			});
		}
		return contractReportsTable;
	}	

	/**
	 * Refreshes messages table
	 */
	private void refreshMessagesTable() {
		Iterator<String> negotiationIds = owner.getNegotiationMessages().keySet().iterator();
		while(negotiationIds.hasNext()) {
			String negotiationId = negotiationIds.next();
			ACLMessage msg = owner.getNegotiationMessages().get(negotiationId).elementAt(0);
			ContentElement ce;
			try {
				ce = owner.getContentManager().extractContent(msg);
				if(ce instanceof CompetenceCall) {
					CompetenceCall cc = (CompetenceCall) ce;
					addNegotiationMessageRow(negotiationId, cc.getNeed().getType(), cc.getContractType());
				}
			} catch (UngroundedException e) {
				e.printStackTrace();
			} catch (CodecException e) {
				e.printStackTrace();
			} catch (OntologyException e) {
				e.printStackTrace();
			}
		}
	}

	private JTable getMessagesTable() {
		if (messagesTable == null) {
			messagesTable = new JTable();
			messagesTableModel =  new DefaultTableModel(null, new String [] {"Negotiation", "Item", "Contract Type"}) {  
				private static final long serialVersionUID = 1L;
				public boolean isCellEditable(int row, int col) {  
					return false;  
				}  
			};  			
			messagesTable.setModel(messagesTableModel);
			messagesTable.getColumnModel().getColumn(0).setPreferredWidth(180);
			messagesTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			messagesTable.getSelectionModel().addListSelectionListener(new ListenerNegotiationsTable());
		}
		return messagesTable;
	}
	
	private class ListenerNegotiationsTable implements ListSelectionListener {
		public void valueChanged(ListSelectionEvent e) {
			if (!e.getValueIsAdjusting()) {
				if (messagesTable.getSelectedRow() != -1) {
					DefaultTableModel messageContentsTableModel =  new DefaultTableModel(null, new String [] {"Performative", "Content"}) {  
						private static final long serialVersionUID = 1L;
						public boolean isCellEditable(int row, int col) {  
							return false;  
						}  
					};
					
					messageContentsTable.setModel(messageContentsTableModel);
					Vector<ACLMessage> messages = (Vector<ACLMessage>) owner.getNegotiationMessages().get(messagesTable.getValueAt(messagesTable.getSelectedRow(), 0));
					for(ACLMessage message: messages ){
						switch (message.getPerformative()) {
						case ACLMessage.CFP:
						case ACLMessage.PROPOSE:
							try {
								ContentElement ce = owner.getContentManager().extractContent(message);
								((DefaultTableModel) messageContentsTable.getModel()).addRow(new String [] {ACLMessage.getPerformative(message.getPerformative()), ce.toString()});					
							} catch(OntologyException oe) {
								oe.printStackTrace();
							} catch(Codec.CodecException ce) {
								ce.printStackTrace();
							}
							break;
						case ACLMessage.ACCEPT_PROPOSAL:
						case ACLMessage.REJECT_PROPOSAL:
							((DefaultTableModel) messageContentsTable.getModel()).addRow(new String [] {ACLMessage.getPerformative(message.getPerformative()), ""});					
						}
					}
					messageContentsTable.getColumnModel().getColumn(0).setWidth(50);
					messageContentsTable.getColumnModel().getColumn(1).setPreferredWidth(360);
				} 
			}
		}
	}

	/**
	 * This method initializes negotiationMessagesTable	
	 * @return javax.swing.JTable	
	 */
	private JTable getMessageContentsTable() {
		if (messageContentsTable == null) {
			messageContentsTable = new JTable();
			messageContentsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			messageContentsTable.addMouseListener(new java.awt.event.MouseAdapter() {
				public void mouseClicked(java.awt.event.MouseEvent e) {
					//double clicking the message shows more details
					if(e.getClickCount() == 2){
						if(messageContentsTable.getSelectedRow() != -1){
							LimitedSizeLinkedHashMap<String,Vector<ACLMessage>> htmp = owner.getNegotiationMessages();
							Vector<ACLMessage> c = (Vector<ACLMessage>) htmp.get(messagesTable.getValueAt(messagesTable.getSelectedRow(), 0));
							MsgViewerGUI mv = new MsgViewerGUI((ACLMessage) c.get(messageContentsTable.getSelectedRow()), owner, EnterpriseAgentGUI.this);
							mv.setVisible(true);
						}
					}
				}
			});
		}
		return messageContentsTable;
	}	
	
	private JTable getAttributesTable() {
		if (attributesTable == null) {
			attributesTable = new JTable();
			attributesTable.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
			attributesTable.setEnabled(false);
			attributesTable.setModel(new DefaultTableModel(new Object [][] { }, new String [] {"Foreign Name","Own Name"}){
				private static final long serialVersionUID = 1L;
			});

		}
		return attributesTable;
	}	
	
	/**
	 * This method initializes mappingClassesList	
	 * 	
	 * @return javax.swing.JList	
	 */
	private JList getMappingClassesList() {
		if (mappingClassesList == null) {
			mappingClassesList = new JList(ontologyMappingsList);
			mappingClassesList.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
				public void valueChanged(javax.swing.event.ListSelectionEvent e) {
					//Delete all values from the Table
					DefaultTableModel dtm =(DefaultTableModel)attributesTable.getModel();
					int countNumRows = attributesTable.getRowCount();
					for(int i = 0; i < countNumRows; i++) {
						dtm.removeRow(0);
					}
					if(mappingClassesList.getSelectedValue() != null) {
						ItemMapping cm = owner.getItemMappings().get((String) mappingClassesList.getSelectedValue());
						if(cm != null) {
							foreignClassNameTextField.setText(cm.getNeedType());
							ownClassNameTextField.setText(cm.getCompetenceType());
							@SuppressWarnings("unchecked")
							Iterator<AttributeMapping> it = cm.getAttributeMappings().iterator();
							while(it.hasNext())
							{
								AttributeMapping att = (AttributeMapping) it.next();
								//Add the respective values to the tables
								dtm.addRow(new Object[]{att.getAtributeName(),cm.getAttributeMappingFromName(att.getAtributeName()).getMappingAttributeName()});
							}
						} else {
							foreignClassNameTextField.setText("no mapping found");
							ownClassNameTextField.setText("no mapping found");
						}
					}
				}
			});
		}
		return mappingClassesList;
	}
	
	/**
	 * This method initializes mappingClassesPanel	
	 * @return javax.swing.JScrollPane	
	 */
	private JScrollPane getMappingClassesPanel() {
		if (mappingClassesPanel == null) {
			mappingClassesPanel = new JScrollPane();
			mappingClassesPanel.setBounds(new Rectangle(12, 39, 160, 251));
			mappingClassesPanel.setViewportView(getMappingClassesList());
		}
		return mappingClassesPanel;
	}	
	
	private JTable getParametersTable() {
		if (parametersTable == null) {
			parametersTable = new JTable();
			DefaultTableModel tableModel =  new DefaultTableModel(null, new String [] {"Name", "Value"}) {  
				private static final long serialVersionUID = 1L;
				public boolean isCellEditable(int row, int col) {  
					return false;  
				}  
			};  			
			parametersTable.setModel(tableModel);

			tableModel.addRow(new String [] {"className", owner.getClass().getName()});
			
			Enumeration<Object> argsKeys = this.owner.getConfigurationArguments().keys();
			argsKeys = Collections.enumeration( new TreeSet<Object>( Collections.list( argsKeys ) ) ); //assortment 	
			while (argsKeys.hasMoreElements()) {
				String name = argsKeys.nextElement().toString();
				tableModel.addRow(new String [] {name, this.owner.getConfigurationArguments().getProperty(name)});				
			}
			parametersTable.getColumnModel().getColumn(1).setPreferredWidth(280);
			parametersTable.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
			parametersTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		}
		return parametersTable;
	}
	
	/**
	 * Fills the contract type combobox
	 * @param l	the list of contract types
	 */
	private void setContractTypes(List l) {
		contractTypeComboBox.removeAllItems();
		contractTypeToolTip.removeAllElements();
		for(int i=0;i<l.size();i++) {
			contractTypeComboBox.addItem(((ContractType) l.get(i)).getName());
			contractTypeToolTip.add(((ContractType) l.get(i)).getComment());
		}
		
		contractTypeComboBox.setSelectedItem(QFNegotiationParameters.CONTRACT_TYPE_DEFAULT);
	}	
	
	private class FillContractTypes extends NEManagementInit_SendContractTypes {
		private static final long serialVersionUID = 1L;

		public FillContractTypes(Agent agent, AID normenv) {
			super(agent, normenv);
		}
		
		public int onEnd() {
			setContractTypes(getContractTypes());
			return 0;
		}
	}
	
	/**
	 * Add a new tab to the GUI
	 * 
	 * @param name	the tab name
	 * @param panel	a new panel for the tab
	 */
	protected void addNewTab(String name, JPanel panel){
		jTabbedPane.addTab(name, panel);
	}
	
	/**
	 * Initializes the button to kill the agent
	 */    
	private JButton getKillButton() {
		if (killButton == null) {
			killButton = new JButton();
			killButton.setText("X");
			killButton.setForeground(Color.red);
			killButton.setHorizontalAlignment(SwingConstants.CENTER);
			killButton.setSize(45,17);
			killButton.setLocation(535, this.getSize().height-55);

			killButton.addMouseListener(new java.awt.event.MouseAdapter() { 
				public void mouseReleased(java.awt.event.MouseEvent e) {
					Object[] options = { "OK", "CANCEL" };
					int answer = JOptionPane.showOptionDialog(mainPanel, "Agent " + owner.getLocalName() + " will be killed. Click OK to continue",
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
	
	/**
	 * Adds Item negotiation to the GUI list
	 * @param need			the need name
	 * @param negotiationId	the negotiation id
	 */
	public void addNegotiationMessageRow(String negotiationId, String need, String contractType){
		DefaultTableModel tableModel = (DefaultTableModel) messagesTable.getModel();
		tableModel.addRow(new String [] {negotiationId, need, contractType});
		
		if (owner.getConfigurationArguments().containsKey("maxEntries_negotiationMessages") && 
				(messagesTable.getRowCount() > Integer.parseInt(owner.getConfigurationArguments().getProperty("maxEntries_negotiationMessages"))) ) {
			
			tableModel.removeRow(0);
			if (messagesTable.getSelectedRow() != -1) {
				messagesTable.setRowSelectionInterval(messagesTable.getSelectedRow(), messagesTable.getSelectedRow());
			} else {
				messagesTable.setRowSelectionInterval(0, 0);
			}			
		}		
		
		if (messagesTable.getRowCount() == 1) {
			messagesTable.setRowSelectionInterval(0, 0);
		}
	}
	
	/**
	 * Refresh the List of ontologies
	 */
	public void refreshOntologyMappings() {
		ontologyMappingsList.removeAllElements();
		for(Iterator<String> i = this.owner.getItemMappings().keySet().iterator(); i.hasNext();) {
			String s = i.next();
			ontologyMappingsList.addElement(s);
		}
	}
	
	
	private class GetTopAgents extends CTRInit_SendTopAgents{
		private static final long serialVersionUID = 4177772329235214488L;

		private OutcomeGenerator.MappingMethod mapMethod;
		private int topNumberOfAgents;
		
		public GetTopAgents(Agent agent, AID requester, Vector<InterestedAgent> interestedAgents, Need need, int topNumberOfAgents, boolean isSelectedContextual, int quantity, int dTime, OutcomeGenerator.MappingMethod mapMethod){
			super(agent, requester, interestedAgents, need, topNumberOfAgents, isSelectedContextual, quantity, dTime, mapMethod);
			this.mapMethod = mapMethod;
			this.topNumberOfAgents = topNumberOfAgents;
		}
		
		public int onEnd() {
			trustedAgentsGUI = new TrustedAgentsGUI(topNumberOfAgents, this.getTopAgents(), mapMethod);
			trustedAgentsGUI.setVisible(true);
			return 0;
		}
		
	}
	
}
package ei.agent.enterpriseagent.gui;

import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JLabel;

import ei.agent.enterpriseagent.enactment.AutomaticEnterpriseAgent;

import java.awt.Color;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

public class AutomaticEnterpriseAgentGUI extends EnterpriseAgentGUI{
	private static final long serialVersionUID = -5772457651326537140L;
	
	private JPanel settingsPanel = null;
	private JPanel obligationFulfillmentSubPanel = null;
	private JPanel livelineViolDenouceSubPanel = null;
	private JPanel deadlineViolDenoucesubPanel = null;	
	
	private AutomaticEnterpriseAgent owner;
	
	private final String valuesComboBoxOptions[] = {"0","10", "20", "30", "40", "50", "60", "70", "80", "90", "100"};
	
	public AutomaticEnterpriseAgentGUI(AutomaticEnterpriseAgent owner){
		super(owner);
		
		this.owner = owner;
		
		initialize();		
	}
	
	private void initialize() {
		this.addNewTab("Settings", getSettingsPanel());
	}	
	
	private JPanel getSettingsPanel() {
		if (settingsPanel == null) { 
			settingsPanel = new JPanel();
			settingsPanel.setLayout(null);
			settingsPanel.setSize(600, 580);
	        settingsPanel.add(getObligationFulfillmentSubPanel(), null);
	        settingsPanel.add(getLivelineViolDenouceSubPanel(), null);		
	        settingsPanel.add(getDeadlineViolDenouceSubPanel(), null);
		}			
		return settingsPanel;
	}	
	
	private JPanel getObligationFulfillmentSubPanel() {
		if(obligationFulfillmentSubPanel == null) {
			obligationFulfillmentSubPanel = new JPanel();
			obligationFulfillmentSubPanel.setLayout(null);
			obligationFulfillmentSubPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createLineBorder(
					new java.awt.Color(102, 102, 102)), "Obligation Fulfillment (%)", javax.swing.border.TitledBorder.CENTER, javax.swing.border.TitledBorder.DEFAULT_POSITION) );
			obligationFulfillmentSubPanel.setBounds(120, 20, 340, 140);
			obligationFulfillmentSubPanel.setBackground(Color.white);
			
			JLabel fulfProbabilityLabel = new JLabel("Fulfillment probability:");
			fulfProbabilityLabel.setBounds(70, 20, 180, 22);
			obligationFulfillmentSubPanel.add(fulfProbabilityLabel,null);
			JLabel delayFulfProbabilityLabel = new JLabel("Delay probability:");
			delayFulfProbabilityLabel.setBounds(70, 48, 180, 22);
			obligationFulfillmentSubPanel.add(delayFulfProbabilityLabel,null);
			
			JLabel fulfMinTimeLabel = new JLabel("Min fulfillment time:");
			fulfMinTimeLabel.setBounds(70, 76, 180, 22);
			obligationFulfillmentSubPanel.add(fulfMinTimeLabel,null);
			JLabel fulfMaxTimeLabel = new JLabel("Max fulfillment time:");
			fulfMaxTimeLabel.setBounds(70, 104, 180, 22);
			obligationFulfillmentSubPanel.add(fulfMaxTimeLabel,null);
			
			JComboBox fulfProbabilityComboBox = new JComboBox(valuesComboBoxOptions);
			fulfProbabilityComboBox.setBounds(226, 20, 54, 22);
			fulfProbabilityComboBox.setBackground(Color.white);
			setValuesComboBox(fulfProbabilityComboBox, owner.getObligation_fulfillment_probability());
			fulfProbabilityComboBox.addItemListener(new ItemListener() {
				public void itemStateChanged(ItemEvent itemEvent) {
					if(itemEvent.getStateChange() == ItemEvent.SELECTED) {
						JComboBox comoBox = (JComboBox) itemEvent.getSource();
						String value = (String) comoBox.getSelectedItem();
						if (validateValues(value)) {
							owner.setObligation_fulfillment_probability( Double.parseDouble((String) value)/100 );
						} else {
							comoBox.setSelectedItem( String.valueOf(Math.round(owner.getObligation_fulfillment_probability()*100)) );
						}							
					} 
				}
			});
			obligationFulfillmentSubPanel.add(fulfProbabilityComboBox, null);
			
			JComboBox delayFulfProbabilityComboBox = new JComboBox(valuesComboBoxOptions);
			delayFulfProbabilityComboBox.setBounds(226, 48, 54, 22);
			delayFulfProbabilityComboBox.setBackground(Color.white);
			setValuesComboBox(delayFulfProbabilityComboBox, owner.getObligation_delayed_fulfillment_probability());
			delayFulfProbabilityComboBox.addItemListener(new ItemListener() {
				public void itemStateChanged(ItemEvent itemEvent) {
					if(itemEvent.getStateChange() == ItemEvent.SELECTED) {
						JComboBox comoBox = (JComboBox) itemEvent.getSource();
						String value = (String) comoBox.getSelectedItem();
						if (validateValues(value)) {
							owner.setObligation_delayed_fulfillment_probability( Double.parseDouble((String) value)/100 );
						} else {
							comoBox.setSelectedItem( String.valueOf(Math.round(owner.getObligation_delayed_fulfillment_probability()*100)) );
						}					
					}
				}
			});
			obligationFulfillmentSubPanel.add(delayFulfProbabilityComboBox, null);	

			JComboBox fulfMinTimeComboBox = new JComboBox(valuesComboBoxOptions);
			fulfMinTimeComboBox.setEditable(true);
			fulfMinTimeComboBox.setBounds(226, 76, 54, 22);
			fulfMinTimeComboBox.setBackground(Color.white);
			setValuesComboBox(fulfMinTimeComboBox, owner.getObligation_fulfillment_min_time());
			fulfMinTimeComboBox.addItemListener(new ItemListener() {
				public void itemStateChanged(ItemEvent itemEvent) {
					if(itemEvent.getStateChange() == ItemEvent.SELECTED) {
						JComboBox comoBox = (JComboBox) itemEvent.getSource();
						String value = (String) comoBox.getSelectedItem();
						if (validateValues(value)) {
							owner.setObligation_fulfillment_min_time( Double.parseDouble((String) value)/100 );
						} else {
							comoBox.setSelectedItem( String.valueOf(Math.round(owner.getObligation_fulfillment_min_time()*100)) );
						}							
					}
				}
			});
			obligationFulfillmentSubPanel.add(fulfMinTimeComboBox, null);	

			JComboBox fulfMaxTimeComboBox = new JComboBox(valuesComboBoxOptions);
			fulfMaxTimeComboBox.setEditable(true);			
			fulfMaxTimeComboBox.setBounds(226, 104, 54, 22);
			fulfMaxTimeComboBox.setBackground(Color.white);
			setValuesComboBox(fulfMaxTimeComboBox, owner.getObligation_fulfillment_max_time());			
			fulfMaxTimeComboBox.addItemListener(new ItemListener() {
				public void itemStateChanged(ItemEvent itemEvent) {
					if(itemEvent.getStateChange() == ItemEvent.SELECTED) {
						JComboBox comoBox = (JComboBox) itemEvent.getSource();
						String value = (String) comoBox.getSelectedItem();
						if (validateValues(value)) {
							owner.setObligation_fulfillment_max_time( Double.parseDouble((String) value)/100 );
						} else {
							comoBox.setSelectedItem( String.valueOf(Math.round(owner.getObligation_fulfillment_max_time()*100)) );
						}							
					}
				}
			});
			obligationFulfillmentSubPanel.add(fulfMaxTimeComboBox, null);	
		}
		return obligationFulfillmentSubPanel;
	}	
	
	private JPanel getLivelineViolDenouceSubPanel() {
		if(livelineViolDenouceSubPanel == null) {
			livelineViolDenouceSubPanel = new JPanel();
			livelineViolDenouceSubPanel.setLayout(null);
			livelineViolDenouceSubPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createLineBorder(
					new java.awt.Color(102, 102, 102)), "Liveline Violations (%)", javax.swing.border.TitledBorder.CENTER, javax.swing.border.TitledBorder.DEFAULT_POSITION) );
			livelineViolDenouceSubPanel.setBounds(40, 180, 245, 120);
			livelineViolDenouceSubPanel.setBackground(Color.white);
			
			JLabel denounceProbabilityLabel = new JLabel("Denounce probability:");
			denounceProbabilityLabel.setBounds(15, 20, 180, 22);
			livelineViolDenouceSubPanel.add(denounceProbabilityLabel,null);
			
			JLabel denounceMinTimeLabel = new JLabel("Min denounce time:");
			denounceMinTimeLabel.setBounds(15, 48, 180, 22);
			livelineViolDenouceSubPanel.add(denounceMinTimeLabel,null);
			
			JLabel denounceMaxTimeLabel = new JLabel("Max denounce time:");
			denounceMaxTimeLabel.setBounds(15, 76, 180, 22);
			livelineViolDenouceSubPanel.add(denounceMaxTimeLabel,null);			

			JComboBox denounceProbabilityComboBox = new JComboBox(valuesComboBoxOptions);
			denounceProbabilityComboBox.setBounds(180, 20, 54, 22);
			denounceProbabilityComboBox.setBackground(Color.white);
			setValuesComboBox(denounceProbabilityComboBox, owner.getLiveline_violations_denounce_probability());
			denounceProbabilityComboBox.addItemListener(new ItemListener() {
				public void itemStateChanged(ItemEvent itemEvent) {
					if(itemEvent.getStateChange() == ItemEvent.SELECTED) {
						JComboBox comoBox = (JComboBox) itemEvent.getSource();
						String value = (String) comoBox.getSelectedItem();
						if (validateValues(value)) {
							owner.setLiveline_violations_denounce_probability( Double.parseDouble((String) value)/100 );
						} else {
							comoBox.setSelectedItem( String.valueOf(Math.round(owner.getLiveline_violations_denounce_probability()*100)) );
						}						
					}
				}
			});
			livelineViolDenouceSubPanel.add(denounceProbabilityComboBox, null);
			
			JComboBox denounceMinTimeComboBox = new JComboBox(valuesComboBoxOptions);
			denounceMinTimeComboBox.setEditable(true);
			denounceMinTimeComboBox.setBounds(180, 48, 54, 22);
			denounceMinTimeComboBox.setBackground(Color.white);
			setValuesComboBox(denounceMinTimeComboBox, owner.getLiveline_violations_denounce_min_time());
			denounceMinTimeComboBox.addItemListener(new ItemListener() {
				public void itemStateChanged(ItemEvent itemEvent) {
					if(itemEvent.getStateChange() == ItemEvent.SELECTED) {
						JComboBox comoBox = (JComboBox) itemEvent.getSource();
						String value = (String) comoBox.getSelectedItem();
						if (validateValues(value)) {
							owner.setLiveline_violations_denounce_min_time( Double.parseDouble((String) value)/100 );
						} else {
							comoBox.setSelectedItem( String.valueOf(Math.round(owner.getLiveline_violations_denounce_min_time()*100)) );
						}							
					}
				}
			});
			livelineViolDenouceSubPanel.add(denounceMinTimeComboBox, null);	

			JComboBox denounceMaxTimeComboBox = new JComboBox(valuesComboBoxOptions);
			denounceMaxTimeComboBox.setEditable(true);			
			denounceMaxTimeComboBox.setBounds(180, 76, 54, 22);
			denounceMaxTimeComboBox.setBackground(Color.white);
			setValuesComboBox(denounceMaxTimeComboBox, owner.getLiveline_violations_denounce_max_time());
			denounceMaxTimeComboBox.addItemListener(new ItemListener() {
				public void itemStateChanged(ItemEvent itemEvent) {
					if(itemEvent.getStateChange() == ItemEvent.SELECTED) {
						JComboBox comoBox = (JComboBox) itemEvent.getSource();
						String value = (String) comoBox.getSelectedItem();
						if (validateValues(value)) {
							owner.setLiveline_violations_denounce_max_time( Double.parseDouble((String) value)/100 );
						} else {
							comoBox.setSelectedItem( String.valueOf(Math.round(owner.getLiveline_violations_denounce_max_time()*100)) );
						}						
					}
				}
			});
			livelineViolDenouceSubPanel.add(denounceMaxTimeComboBox, null);	
		}
		return livelineViolDenouceSubPanel;
	}
	
	private JPanel getDeadlineViolDenouceSubPanel() {
		if(deadlineViolDenoucesubPanel == null) {
			deadlineViolDenoucesubPanel = new JPanel();
			deadlineViolDenoucesubPanel.setLayout(null);
			deadlineViolDenoucesubPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createLineBorder(
					new java.awt.Color(102, 102, 102)), "Deadline Violations (%)", javax.swing.border.TitledBorder.CENTER, javax.swing.border.TitledBorder.DEFAULT_POSITION) );
			deadlineViolDenoucesubPanel.setBounds(298, 180, 245, 120);
			deadlineViolDenoucesubPanel.setBackground(Color.white);			

			JLabel denounceProbabilityLabel = new JLabel("Denounce probability:");
			denounceProbabilityLabel.setBounds(15, 20, 180, 22);
			deadlineViolDenoucesubPanel.add(denounceProbabilityLabel,null);
			
			JLabel denounceMinTimeLabel = new JLabel("Min denounce time:");
			denounceMinTimeLabel.setBounds(15, 48, 180, 22);
			deadlineViolDenoucesubPanel.add(denounceMinTimeLabel,null);
			
			JLabel denounceMaxTimeLabel = new JLabel("Max denounce time:");
			denounceMaxTimeLabel.setBounds(15, 76, 180, 22);
			deadlineViolDenoucesubPanel.add(denounceMaxTimeLabel,null);			

			JComboBox denounceProbabilityComboBox = new JComboBox(valuesComboBoxOptions);
			denounceProbabilityComboBox.setBounds(180, 20, 54, 22);
			denounceProbabilityComboBox.setBackground(Color.white);
			setValuesComboBox(denounceProbabilityComboBox, owner.getDeadline_violations_denounce_probability());
			denounceProbabilityComboBox.addItemListener(new ItemListener() {
				public void itemStateChanged(ItemEvent itemEvent) {
					if(itemEvent.getStateChange() == ItemEvent.SELECTED) {
						JComboBox comoBox = (JComboBox) itemEvent.getSource();
						String value = (String) comoBox.getSelectedItem();
						if (validateValues(value)) {
							owner.setDeadline_violations_denounce_probability( Double.parseDouble((String) value)/100 );
						} else {
							comoBox.setSelectedItem( String.valueOf(Math.round(owner.getDeadline_violations_denounce_probability()*100)) );
						}
					}
				}
			});
			deadlineViolDenoucesubPanel.add(denounceProbabilityComboBox, null);
			
			JComboBox denounceMinTimeComboBox = new JComboBox(valuesComboBoxOptions);
			denounceMinTimeComboBox.setEditable(true);
			denounceMinTimeComboBox.setBounds(180, 48, 54, 22);
			denounceMinTimeComboBox.setBackground(Color.white);
			setValuesComboBox(denounceMinTimeComboBox, owner.getDeadline_violations_denounce_min_time());
			denounceMinTimeComboBox.addItemListener(new ItemListener() {
				public void itemStateChanged(ItemEvent itemEvent) {
					if(itemEvent.getStateChange() == ItemEvent.SELECTED) {
						JComboBox comoBox = (JComboBox) itemEvent.getSource();
						String value = (String) comoBox.getSelectedItem();
						if (validateValues(value)) {
							owner.setDeadline_violations_denounce_min_time( Double.parseDouble((String) value)/100 );
						} else {
							comoBox.setSelectedItem( String.valueOf(Math.round(owner.getDeadline_violations_denounce_min_time()*100)) );
						}
					}
				}
			});
			deadlineViolDenoucesubPanel.add(denounceMinTimeComboBox, null);	

			JComboBox denounceMaxTimeComboBox = new JComboBox(valuesComboBoxOptions);
			denounceMaxTimeComboBox.setEditable(true);			
			denounceMaxTimeComboBox.setBounds(180, 76, 54, 22);
			denounceMaxTimeComboBox.setBackground(Color.white);
			setValuesComboBox(denounceMaxTimeComboBox, owner.getDeadline_violations_denounce_max_time());
			denounceMaxTimeComboBox.addItemListener(new ItemListener() {
				public void itemStateChanged(ItemEvent itemEvent) {
					if(itemEvent.getStateChange() == ItemEvent.SELECTED) {
						JComboBox comoBox = (JComboBox) itemEvent.getSource();
						String value = (String) comoBox.getSelectedItem();
						if (validateValues(value)) {
							owner.setDeadline_violations_denounce_max_time( Double.parseDouble((String) value)/100 );
						} else {
							comoBox.setSelectedItem( String.valueOf(Math.round(owner.getDeadline_violations_denounce_max_time()*100)) );
						}
					}
				}
			});
			deadlineViolDenoucesubPanel.add(denounceMaxTimeComboBox, null);	
		}

		return deadlineViolDenoucesubPanel;
	}
	
	private void setValuesComboBox (JComboBox comboBox, double value) {
		String fulfProbValue = String.valueOf(Math.round(value*100));
		if (isInValuesComboBoxOptions(fulfProbValue)) {
			comboBox.setSelectedItem(fulfProbValue);
		} else {
			comboBox.addItem(fulfProbValue);
			comboBox.setSelectedItem(fulfProbValue);
		}
	}
	private boolean isInValuesComboBoxOptions (String value) {
		for (int i=0; i< valuesComboBoxOptions.length;i++) {
			if (valuesComboBoxOptions[i].equals(value)) {
				return true;
			} 
		}
		return false;		
	}
	
	private boolean validateValues(String v) {
        try {
    		Double value = Double.parseDouble(v);
    		if (value < 0 || value >= 1000) {
    			return false;
    		}
         } catch (NumberFormatException e) {
			return false;
         }		
		return true;
	}
	
}
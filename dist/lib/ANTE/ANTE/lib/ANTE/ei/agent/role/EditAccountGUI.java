package ei.agent.role;

import javax.swing.JPanel;
import javax.swing.JFrame;
import javax.swing.JButton;
import java.awt.Point;
import java.awt.Dimension;
import javax.swing.JTextField;
import javax.swing.JLabel;
import jade.core.AID;

/**
 * GUI for editing an agent's account balance
 */
public class EditAccountGUI extends JFrame {

	private static final long serialVersionUID = 1L;
	private JPanel jContentPane = null;
	private JButton okButton = null;
	private JButton cancelButton = null;
	private JTextField newValueTextField = null;
	private JTextField currentValueTextField = null;
	private JLabel currentValueLabel = null;
	private JLabel newValueLabel = null;
	private BankAgentGUI guiParent = null;
	private BankAgent parent = null;
	private String agent = null;
	private Double value = null;

	/**
	 * This is the default constructor
	 */
	public EditAccountGUI(BankAgentGUI baGUI, BankAgent parent, String agent, Double value) {
		
		super();
		
		this.agent = agent;
		this.parent = parent;
		this.guiParent = baGUI;
		this.value = value;
		
		initialize();
	}

	/**
	 * This method initializes this
	 * 
	 * @return void
	 */
	private void initialize() {
		this.setSize(211, 200);
		this.setContentPane(getJContentPane());
		this.setResizable(false);
		this.setLocationRelativeTo(guiParent);
		this.setTitle(agent);
	}

	/**
	 * This method initializes jContentPane
	 * 
	 * @return javax.swing.JPanel
	 */
	private JPanel getJContentPane() {
		if (jContentPane == null) {
			newValueLabel = new JLabel();
			newValueLabel.setName("");
			newValueLabel.setSize(new Dimension(140, 20));
			newValueLabel.setLocation(new Point(10, 70));
			newValueLabel.setText("New Account Value:");
			currentValueLabel = new JLabel();
			currentValueLabel.setText("Current Account Value:");
			currentValueLabel.setLocation(new Point(10, 10));
			currentValueLabel.setSize(new Dimension(142, 20));
			jContentPane = new JPanel();
			jContentPane.setLayout(null);
			jContentPane.add(getOkButton(), null);
			jContentPane.add(getCancelButton(), null);
			jContentPane.add(getNewValueTextField(), null);
			jContentPane.add(getCurrentValueTextField(), null);
			jContentPane.add(currentValueLabel, null);
			jContentPane.add(newValueLabel, null);
		}
		return jContentPane;
	}

	/**
	 * This method initializes okButton	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getOkButton() {
		if (okButton == null) {
			okButton = new JButton();
			okButton.setText("OK");
			okButton.setSize(new Dimension(74, 23));
			okButton.setLocation(new Point(10, 130));
			okButton.addMouseListener(new java.awt.event.MouseAdapter() {
				public void mousePressed(java.awt.event.MouseEvent e) {
					//update account and add a movement
					try{
						Double current = Double.parseDouble(currentValueTextField.getText());
						Double d = Double.parseDouble(newValueTextField.getText());
						parent.getAccounts().put(new AID(agent,false),d);
						BankMovement bm;
						if(current > d){
							bm = new BankMovement(new AID(agent,false),new AID("USER_UPDATE",false),d-current);
						}
						else{
							bm = new BankMovement(new AID("USER_UPDATE",false),new AID(agent,false),d-current);
						}
						parent.getMovements().add(bm);
						parent.saveAccounts();
						guiParent.updateBalanceRow(new AID(agent,false), d);
						destroyGUI();
					}
					catch(NumberFormatException nfe){
						newValueTextField.setText("#INVALID NUMBER!#");
					}
				}
			});
		}
		return okButton;
	}

	/**
	 * This method initializes cancelButton	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getCancelButton() {
		if (cancelButton == null) {
			cancelButton = new JButton();
			cancelButton.setText("Cancel");
			cancelButton.setLocation(new Point(110, 130));
			cancelButton.setSize(new Dimension(74, 23));
			cancelButton.addMouseListener(new java.awt.event.MouseAdapter() {
				public void mousePressed(java.awt.event.MouseEvent e) {
					destroyGUI();
				}
			});
		}
		return cancelButton;
	}

	/**
	 * Destroys the GUI
	 */
	public void destroyGUI(){
		this.dispose();
	}
	
	/**
	 * This method initializes newValueTextField	
	 * 	
	 * @return javax.swing.JTextField	
	 */
	private JTextField getNewValueTextField() {
		if (newValueTextField == null) {
			newValueTextField = new JTextField();
			newValueTextField.setSize(new Dimension(167, 25));
			newValueTextField.setLocation(new Point(10, 90));
		}
		return newValueTextField;
	}

	/**
	 * This method initializes currentValueTextField	
	 * 	
	 * @return javax.swing.JTextField	
	 */
	private JTextField getCurrentValueTextField() {
		if (currentValueTextField == null) {
			currentValueTextField = new JTextField();
			currentValueTextField.setLocation(new Point(10, 30));
			currentValueTextField.setText(String.valueOf(value));
			currentValueTextField.setEditable(false);
			currentValueTextField.setSize(new Dimension(167, 25));
		}
		return currentValueTextField;
	}

}  //  @jve:decl-index=0:visual-constraint="10,10"

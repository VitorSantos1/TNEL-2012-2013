package ei;

import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.wrapper.StaleProxyException;

import javax.swing.JButton;
import javax.swing.JLayeredPane;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JFrame;
import javax.swing.JComponent;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

public class ElectronicInstitutionGUI extends JFrame {
	private static final long serialVersionUID = 1L;

	private ElectronicInstitutionGUIAgent eiGUIAgent;   // a reference to the agent that will allow the GUI to communicate with the JADE platform

	private JPanel platformServicesPanel = null;
	private JPanel externalAgentsPanel = null;
	private JPanel usersPanel = null;
	private JPanel mainPanel = null;
	private JTabbedPane tabbedPane = null;
	private JLayeredPane platformServicesPane = null;
	private JLayeredPane externalAgentsPane = null;
	private JLayeredPane userAgentsPane = null;
	private JButton launchRMAButton;
	
	private JLabel anteLogoLabel = null;

	private Hashtable<String,Vector<String>> platformServices = new Hashtable<String,Vector<String>>();
	private Hashtable<String,Vector<String>> externalAgents = new Hashtable<String,Vector<String>>();
	private Hashtable<String,DFAgentDescription> userAgents = new Hashtable<String,DFAgentDescription>();

	private JScrollPane usersScrollPane;


	public ElectronicInstitutionGUI(final ElectronicInstitutionGUIAgent eiGUIAgent) {
		super("ANTE: Agreement Negotiation in a Normative and Trust-enabled Environment");

		this.eiGUIAgent = eiGUIAgent;
		
		initialize();
		
		addWindowListener(new WindowListener(){  
			public void windowActivated(WindowEvent arg0) { }
			public void windowClosed(WindowEvent arg0) { }
			public void windowClosing(WindowEvent e) {
				// shutdown platform
				Object[] options = { "OK", "CANCEL" };
				int answer = JOptionPane.showOptionDialog(null, "Platform will shut down. Click OK to continue", "Shutdown Warning",
						JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE, null, options, options[0]);

				if (answer == 0) {
					eiGUIAgent.shutDownPlatform();
				} else {
					setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
				}

			}
			public void windowDeactivated(WindowEvent arg0) { }
			public void windowDeiconified(WindowEvent arg0) { }
			public void windowIconified(WindowEvent arg0) {}
			public void windowOpened(WindowEvent arg0) { }
		});
	}

	private void initialize() {
		this.setResizable(false);
		this.setSize(648, 480);
		this.setLocation(getToolkit().getScreenSize().width/4, getToolkit().getScreenSize().height/4);
		mainPanel = new JPanel();
		mainPanel.setLayout(null);
		anteLogoLabel = new JLabel(new ImageIcon("img/ante.png"));
		anteLogoLabel.setBounds(5, 5, 55, 55);		
		mainPanel.add(anteLogoLabel, null);		
		mainPanel.add(getJTabbedPane(), null);
		mainPanel.setBackground(new Color(252,235,205)); //252,235,205 -> Blanched Almon, 127,255,212 -> Aquamarine1,  204,255,255
		mainPanel.add(getLaunchRMAButton(), null);
		
		// initialize agent structures
		String[] serviceNames = ElectronicInstitution.getAllServiceNames();
		for(int i=0;i<serviceNames.length;i++){
			platformServices.put(serviceNames[i],new Vector<String>());
		}
		String[] externalRoleNames = ElectronicInstitution.getAllExternalRoleNames();
		for(int j=0;j<externalRoleNames.length;j++){
			externalAgents.put(externalRoleNames[j],new Vector<String>());
		}
		userAgents = new Hashtable<String,DFAgentDescription>();
		
		this.setContentPane(mainPanel);
	}

	/**
	 * Update the services and/or roles lists
	 * 
	 * @param dfd	The agent descriptions; for each agent with service changes, a collection of all currently holding services is received
	 */
	protected void updateGUI(DFAgentDescription[] dfd){

		String[] serviceNames = ElectronicInstitution.getAllServiceNames();
		String[] externalRoleNames = ElectronicInstitution.getAllExternalRoleNames();

		// iterate through all agents having descriptions
		for(int i=0; i<dfd.length; i++) {
			// clear GUI roles/services lists for this agent
			String agent = dfd[i].getName().getLocalName();
			for(int j=0;j<serviceNames.length;j++) {
				platformServices.get(serviceNames[j]).removeElement(agent);   // no effect if agent does not have this service
			}
			for(int j=0;j<externalRoleNames.length;j++) {
				externalAgents.get(externalRoleNames[j]).removeElement(agent);   // no effect if agent does not have this role
			}
			userAgents.remove(agent);   // no effect if the agent is not a user agent
			
			// add agent to appropriate structure
			Iterator<?> it = dfd[i].getAllServices();
			while(it.hasNext()) {
				ServiceDescription sd = ((ServiceDescription) it.next());
				if(sd.getType().toString().equals(ElectronicInstitution.ROLE_USER_AGENT)) {   // user agent?
					userAgents.put(agent, dfd[i]);
				} else if(ElectronicInstitution.isValidService(sd.getType().toString())) {   // is this a service?
					platformServices.get(sd.getType().toString()).add(agent);   // add service to GUI list
				} else if(ElectronicInstitution.isValidExternalRole(sd.getType().toString())) {   // or is it a role?
					externalAgents.get(sd.getType().toString()).add(agent);   // add role to GUI list
				}
			}
		}

		// update platform services pane
		platformServicesPane.removeAll();
		String icon;
		Enumeration<String> e = platformServices.keys();
		while(e.hasMoreElements()) {
			String serviceName = e.nextElement();
			if(serviceName.equals(ElectronicInstitution.SRV_NORMATIVE_ENVIRONMENT)) {
				icon = "img/norms.jpg";
			} else if(serviceName.equals(ElectronicInstitution.SRV_ONTOLOGY_MAPPING)) {
				icon = "img/onto2.png";
			} else if(serviceName.equals(ElectronicInstitution.SRV_NEGOTIATION_FACILITATOR)) {
				icon = "img/negotiation_icon.jpg";
			} else if(serviceName.equals(ElectronicInstitution.SRV_NOTARY)) {
				icon = "img/notary-icon.gif";
			} else if(serviceName.equals(ElectronicInstitution.SRV_CTR)) {
				icon = "img/Trust-Handshake-60x60.jpg";
			} else {
				icon = null;
			}
			List<String> serviceAgents = platformServices.get(serviceName);
			for(int i=0; i<serviceAgents.size(); i++) {
				platformServicesPane.add(createButton(serviceAgents.get(i), icon));
			}
		}
		platformServicesPanel.revalidate();
		

		// update external agents pane
		externalAgentsPane.removeAll();
		e = externalAgents.keys();
		while(e.hasMoreElements()) {
			String roleName = e.nextElement();
			if(roleName.equals(ElectronicInstitution.ROLE_BANK)) {
				icon = "img/bank3.png";
			} else if(roleName.equals(ElectronicInstitution.ROLE_DELIVERY_TRACKER)) {
				icon = "img/truck2.png";
			} else if(roleName.equals(ElectronicInstitution.ROLE_MESSENGER)) {
				icon = "img/message.png";
			} else {
				icon = null;
			}
			List<String> agents = externalAgents.get(roleName);
			for(int i=0; i<agents.size(); i++) {
				externalAgentsPane.add(createButton(agents.get(i), icon));
			}
		}
		externalAgentsPanel.revalidate();
		
		// update users pane
		userAgentsPane.removeAll();
		Vector<String> userAgentNames = new Vector<String>(userAgents.keySet());
		Collections.sort(userAgentNames);
		for(int i=0; i<userAgentNames.size(); i++) {
			icon = null;
			DFAgentDescription dfad = userAgents.get(userAgentNames.get(i));
			Iterator<?> it = dfad.getAllServices();
			while(it.hasNext()) {
				ServiceDescription sd = ((ServiceDescription) it.next());
				if(sd.getType().equals(ElectronicInstitution.ROLE_SELLER_AGENT)) {
					if(icon == null) {
						icon = "img/Supplier.jpg";
					} else { // seller and buyer
						icon = "img/buysup.png";
					}
				} else if(sd.getType().equals(ElectronicInstitution.ROLE_BUYER_AGENT)) {
					if(icon == null) {
						icon = "img/buyer.png";
					} else { // // seller and buyer
						icon = "img/buysup.png";
					}
				}
			}
			userAgentsPane.add(createButton(userAgentNames.get(i), icon));
		}
		
		usersPanel.revalidate(); //It's automatically called on update of size, location, or internal layout changes. 
	}

	private JTabbedPane getJTabbedPane() {
		if(tabbedPane == null) {
			tabbedPane = new JTabbedPane();
			tabbedPane.setBounds(45, 45, 550, 370);
			tabbedPane.addTab("Platform Service", getPlatformServicePanel());
			tabbedPane.addTab("External Agents", getExternalAgentsPanel());
			tabbedPane.addTab("Users", getUsersPanel());
		}
		return tabbedPane;
	}

	private JComponent getPlatformServicePanel() {
		if(platformServicesPanel == null) {
			platformServicesPanel = new JPanel();
			platformServicesPanel.setBounds(30, 60, 520, 370);
			platformServicesPanel.add(getPlatformServicesPane());
		}
		return platformServicesPanel;
	}

	private JComponent getExternalAgentsPanel() {
		if(externalAgentsPanel == null) {
			externalAgentsPanel = new JPanel();
			externalAgentsPanel.setBounds(30, 60, 520, 370);
			externalAgentsPanel.add(getExternalAgentsPane());
		}
		return externalAgentsPanel;
	}

	private JComponent getUsersPanel() {
		if(usersPanel == null) {
			usersPanel = new JPanel();
			usersScrollPane = new JScrollPane(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED ,JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
			usersScrollPane.setPreferredSize(new Dimension(550, 370));
			usersScrollPane.setViewportView(getUsersPane());
			usersPanel.add(usersScrollPane);
		}
		return usersPanel;
	}

	private JLayeredPane getPlatformServicesPane() {
		if(platformServicesPane == null) {
			platformServicesPane = new JLayeredPane();
			platformServicesPane.setPreferredSize(new Dimension(520, 370));
			platformServicesPane.setLayout(new FlowLayout(FlowLayout.LEADING));
		}
		return platformServicesPane;
	}
	
	private JLayeredPane getExternalAgentsPane() {
		if(externalAgentsPane == null) {
			externalAgentsPane = new JLayeredPane();
			externalAgentsPane.setPreferredSize(new Dimension(520, 370));
			externalAgentsPane.setLayout(new FlowLayout(FlowLayout.LEADING));
		}
		return externalAgentsPane;
	}
	
	private JLayeredPane getUsersPane() {
		if(userAgentsPane == null) {
			userAgentsPane = new JLayeredPane();
			userAgentsPane.setPreferredSize(new Dimension(520, 370));
			userAgentsPane.setLayout(new FlowLayout(FlowLayout.LEADING));
		}
		return userAgentsPane;
	}
	
	//Create and set up a JButton
	private JButton createButton(String text, String imagePath) {
		JButton button;
		if(imagePath != null) {
			button = new JButton(text, new ImageIcon(imagePath));
		} else {
			button = new JButton(text);
		}
		button.setContentAreaFilled(false);
		button.setPreferredSize(new Dimension(80, 80));
		button.setMargin(new Insets(0,0,0,0));
		button.setVerticalTextPosition(SwingConstants.BOTTOM);
    	button.setHorizontalTextPosition(SwingConstants.CENTER);
    	button.setFont(new Font("Arial", Font.BOLD, 9));
    	button.setBorderPainted(false);
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				eiGUIAgent.askShowGUI(((JButton) e.getSource()).getText());
			}
		});
		return button;
	}
	
	/**
	 * This method initializes the button that launches RMA agents
	 * 
	 * @return javax.swing.JButton
	 */
	private JButton getLaunchRMAButton(){
		if(launchRMAButton == null){
			launchRMAButton = new JButton();
			launchRMAButton.setSize(22,22);
			launchRMAButton.setLocation(573, this.getSize().height-60);
			launchRMAButton.setIcon(new ImageIcon("img/jade.gif", "RMA"));
			launchRMAButton.setToolTipText("Launch new RMA Agent");
			launchRMAButton.addMouseListener(new java.awt.event.MouseAdapter() {
				public void mouseReleased(java.awt.event.MouseEvent e){
					//launch new RMA
					try{
						//generate agent name (in case more than one is launched)
						String rmaName = "rma"+System.currentTimeMillis();
						//create the rma agent
						System.out.println("Launching rma agent: "+rmaName);
						eiGUIAgent.getContainerController().createNewAgent(rmaName, "jade.tools.rma.rma", null).start();
					}
					catch(StaleProxyException spe){
						spe.printStackTrace();
					}
				}
			});
		}
		return launchRMAButton;
	}
}

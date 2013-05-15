package ei.agent.enterpriseagent.gui;

import java.awt.Color;
import java.awt.Dimension;

import jade.content.ContentElement;
import jade.content.lang.sl.SLCodec;
import jade.core.AID;
import jade.domain.FIPANames;
import jade.domain.FIPAService;
import jade.lang.acl.ACLMessage;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;

import ei.ElectronicInstitution;
import ei.agent.enterpriseagent.EnterpriseAgent;

/**
 * Class that represents the interface for CTR Information about a Report
 */
public class ReportGUI extends JFrame {

    /**
	 * 
	 */
	private static final long serialVersionUID = 8167015969905849680L;
	private EnterpriseAgent parent;
	private AID rep = null;

    public ReportGUI(EnterpriseAgent parent) {
    	this.parent = parent;
    	initialize();       
    }
    
    

    /** This method is called from within the constructor to
     * initialize the form.
     * 
     */
    private void initialize() {
    	reportGui = new javax.swing.JFrame();
        reportN = new javax.swing.JLabel();
        agentsInvolved = new javax.swing.JLabel();
        reportType = new javax.swing.JLabel();
        previousRep = new javax.swing.JLabel();
        actualRep = new javax.swing.JLabel();
        agentsInvolvedLabel = new javax.swing.JLabel();
        reportTypeLabel = new javax.swing.JLabel();
        previousRepLabel = new javax.swing.JLabel();
        actualRepLabel = new javax.swing.JLabel();
        
        rep = parent.fetchAgent(ElectronicInstitution.SRV_CTR, false);
        
        reportN.setText("Report");
        agentsInvolved.setText("Agents Involved: ");
        reportType.setText("Report Type: ");
        previousRep.setText("Previous Reputation: ");
        actualRep.setText("Reputation Updated: ");
        
        javax.swing.GroupLayout jFrame1Layout = new javax.swing.GroupLayout(reportGui.getContentPane());
        reportGui.getContentPane().setLayout(jFrame1Layout);
        jFrame1Layout.setHorizontalGroup(
            jFrame1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jFrame1Layout.createSequentialGroup()
                .addGroup(jFrame1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jFrame1Layout.createSequentialGroup()
                        .addComponent(reportN)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED))
                    .addGroup(jFrame1Layout.createSequentialGroup()
                        .addComponent(agentsInvolved)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(agentsInvolvedLabel))
                    .addGroup(jFrame1Layout.createSequentialGroup()
                        .addComponent(reportType)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(reportTypeLabel))
                    .addGroup(jFrame1Layout.createSequentialGroup()
                        .addComponent(previousRep)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(previousRepLabel))
                    .addGroup(jFrame1Layout.createSequentialGroup()
                        .addComponent(actualRep)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(actualRepLabel)))
                .addContainerGap(95, Short.MAX_VALUE))
        );
        jFrame1Layout.setVerticalGroup(
            jFrame1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jFrame1Layout.createSequentialGroup()
                .addGroup(jFrame1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(reportN))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jFrame1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(agentsInvolved)
                    .addComponent(agentsInvolvedLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jFrame1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(reportType)
                    .addComponent(reportTypeLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jFrame1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(previousRep)
                    .addComponent(previousRepLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jFrame1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(actualRep)
                    .addComponent(actualRepLabel)))
        );
    }

        /**
         * Refresh Report GUI
         * 
         * @param report
         */
        public void updateReportGui(String agent, int report, int x, int y)
        {
        	
        	reportGui.dispose();
        	reportGui.setUndecorated(true);
        	reportGui.getContentPane().setBackground(Color.LIGHT_GRAY);
        	reportGui.getRootPane().setBorder(BorderFactory.createEtchedBorder());
        	reportGui.setMinimumSize(new Dimension(200, 110));
        	reportGui.setVisible(true);
        	
        	ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
        	
        	SLCodec codec = new SLCodec();
    		// set the protocol of the request to be sent
    		msg.setProtocol(FIPANames.InteractionProtocol.FIPA_REQUEST);
    		// set the language of the request to be sent
    		msg.setLanguage(codec.getName());
    		// set the ontology of the request to be sent
    		msg.setOntology(ElectronicInstitution.CTR_ONTOLOGY);
    		// add the reputation agent as receiver of the request to be sent
    		msg.addReceiver(rep);
    		// set the requested action to send-agents-with-reputation-value

    	
    		ACLMessage msgInform = null;
    		try {
    			msgInform = FIPAService.doFipaRequestClient(parent, msg, 100000);
    		} catch (Exception e) {
    			e.printStackTrace();
    		}
        	
    		ContentElement ce = null;


            reportN.setText("Report "+report);
        	
        	reportGui.setLocation(x, y);
        	reportGui.repaint();
        	
        }
    
    private javax.swing.JFrame reportGui;
    private javax.swing.JLabel reportN;
    private javax.swing.JLabel agentsInvolved;
    private javax.swing.JLabel reportType;
    private javax.swing.JLabel previousRep;
    private javax.swing.JLabel actualRep;
	private JLabel agentsInvolvedLabel;
	private JLabel reportTypeLabel;
	private JLabel previousRepLabel;
	private JLabel actualRepLabel;

	public void close() {
		reportGui.setVisible(false);
	}
}
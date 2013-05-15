
package ei.agent.gui;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Insets;

/**
 * A GUI to display general information
 */
public class InfoGUI extends JFrame {
	private static final long serialVersionUID = -3354743911593140811L;

	private JTextArea contentTextArea = null;
	
	private String text;
	private String agentName;
	private Component caller;

	public InfoGUI(String text, String agentName, Component caller) {
		super();
		
		this.text = text;
		this.caller = caller;
		this.agentName = agentName;
		
		initialize();
	}
	
	/**
	 * This method initializes this
	 * 
	 * @return void
	 */
	private void initialize() {
		this.setResizable(false);
		this.setSize(260, 140);
		this.setTitle(agentName);
		this.setLocationRelativeTo(caller);

		JScrollPane contentTextJScrollPane = new JScrollPane(getContentTextArea());
		contentTextJScrollPane.setPreferredSize(new Dimension(220, 220));		
		this.add(contentTextJScrollPane, null);
	}

	private JTextArea getContentTextArea() {
		if (contentTextArea == null) {
			contentTextArea = new JTextArea(text);
			contentTextArea.setEditable(false);
			contentTextArea.setLineWrap(true);
			contentTextArea.setWrapStyleWord(true);
			contentTextArea.setFont(new Font("Dialog", Font.BOLD, 12));
			contentTextArea.setMargin(new Insets(30, 8, 5, 8));
		}
		return contentTextArea;
	}	
}

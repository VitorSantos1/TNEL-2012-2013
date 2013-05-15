
package ei.gui;

import jade.util.leap.List;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import javax.swing.text.StyledDocument;

/**
 * A GUI to display information
 */
public class InfoViewerGUI extends JFrame implements ComponentListener{

	private static final long serialVersionUID = -3354743911593140811L;

	private javax.swing.JPanel jContentPane = null;
	
	private JTextPane informationTextPane = null;
	private JScrollPane informationScrollPane = null;
	
	/**
	 * This is the default constructor
	 */
	public InfoViewerGUI(String title, List infos) {
		super();
		initialize(title);
		setText(infos);
	}
	
	private void setText(List list) {
//		informationTextPane.setFont(new Font("Courier", Font.PLAIN, 12));
//		informationTextPane.setText(text.toString());
		
		StyledDocument doc = informationTextPane.getStyledDocument();
		Style def = StyleContext.getDefaultStyleContext().getStyle(StyleContext.DEFAULT_STYLE);
		
		Style regular = doc.addStyle("regular", def);
		StyleConstants.setFontFamily(def, "SansSerif");
		
		Style s = doc.addStyle("heading", regular);
		StyleConstants.setFontFamily(def, "Courier");
		StyleConstants.setBold(s, true);
		StyleConstants.setFontSize(s, 16);

		s = doc.addStyle("tabbedtext", regular);
		StyleConstants.setFontFamily(def, "Courier");
		StyleConstants.setBold(s, true);
		StyleConstants.setForeground(s, Color.RED);
		StyleConstants.setFontSize(s, 14);

		try {
			if(list.size() != 0) {
				String line = list.get(0).toString();
				doc.insertString(doc.getLength(), line + "\n", doc.getStyle("heading"));
			}
			for(int i=1;i<list.size();i++) {
				String line = list.get(i).toString();
				if(line.startsWith("\t") || line.startsWith("    ")) {
					doc.insertString(doc.getLength(), line + "\n", doc.getStyle("tabbedtext"));
				} else {
					doc.insertString(doc.getLength(), line + "\n", doc.getStyle("regular"));
				}
		    }
		} catch (BadLocationException ble) {
		    System.err.println("Couldn't insert initial text into text pane.");
		}
	}
	
	/**
	 * This method initializes this
	 * 
	 * @return void
	 */
	private void initialize(String title) {
		this.setResizable(true);
		this.setMinimumSize(new Dimension(300, 200));
		this.setSize(700, 400);
		this.setContentPane(getJContentPane());
		this.setTitle(title);
		this.setLocationRelativeTo(null);   // null has the effect of placing it at the center of screen
	}
	
	public void componentHidden(ComponentEvent e) {
		
	}

    public void componentMoved(ComponentEvent e) {
		
    }

    //if the window is resized adjusts its components to the new size
    public void componentResized(ComponentEvent e) {
    	informationScrollPane.setBounds(new Rectangle(10, 10, this.getWidth()-27, this.getHeight()-50));
    }

    public void componentShown(ComponentEvent e) {
		
    }
	
	private JScrollPane getInformationScrollPane(){
		if(informationScrollPane == null){
			informationScrollPane = new JScrollPane(getInformationTextPane());
			informationScrollPane.setName("jScrollPane");
			informationScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
			informationScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
			informationScrollPane.setBounds(new Rectangle(10, 10, 509, 164));
		}
		return informationScrollPane;
	}

	private JTextPane getInformationTextPane(){
		if(informationTextPane == null){
			informationTextPane = new JTextPane();
			informationTextPane.setBounds(new Rectangle(10, 10, 470, 140));
			informationTextPane.setEditable(false);
		}
		return informationTextPane;
	}

	/**
	 * This method initializes jContentPane
	 * 
	 * @return javax.swing.JPanel
	 */
	private javax.swing.JPanel getJContentPane() {
		if(jContentPane == null) {
			jContentPane = new javax.swing.JPanel();
			jContentPane.setLayout(null);
			jContentPane.add(getInformationScrollPane(), null);
			jContentPane.addComponentListener(this);
		}
		return jContentPane;
	}

	
	
}  //  @jve:decl-index=0:visual-constraint="10,10" 

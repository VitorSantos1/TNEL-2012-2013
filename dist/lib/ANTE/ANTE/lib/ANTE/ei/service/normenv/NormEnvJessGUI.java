package ei.service.normenv;

import java.awt.Frame;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.TextField;

import javax.swing.JFrame;
import java.awt.BorderLayout;
import java.awt.TextArea;
import java.awt.event.ComponentListener;
import java.awt.event.ComponentEvent;

import jess.JessException;
import jess.Value;
import java.awt.Dimension;

/**
 * A GUI for the normative environment agent
 */
public class NormEnvJessGUI extends JFrame implements ComponentListener {
	
	private static final long serialVersionUID = -3354743911593140811L;
	
	private javax.swing.JPanel jContentPane = null;
	//private JScrollPane jessOutputScrollPane = null;
	private TextField commandTextField = null;
	
	private NormEnvEngine jess;
	
	//private JTextPane jessOutputTextPane = null;
	
	private TextArea jessOutputTextArea = null;
	
	/**
	 * This is the default constructor
	 */
	public NormEnvJessGUI(Frame gui) {
		super();
		initialize();
		
		this.setLocation(new Point(gui.getX()+(gui.getWidth()/2), gui.getY() + (gui.getHeight()/2)));		
	}
	
	protected void setNormEnvEngine(NormEnvEngine jess) {
		this.jess = jess;
	}
	
	/**
	 * This method initializes this
	 * 
	 * @return void
	 */
	private void initialize() {
		this.setResizable(true);
		this.setMinimumSize(new Dimension(300, 300));
		this.setSize(533, 335);
		this.setContentPane(getJContentPane());
		this.setTitle("Normative Environment GUI");
	}
	
	public void componentHidden(ComponentEvent e) {
		
	}

    public void componentMoved(ComponentEvent e) {
		
    }

    //if the window is resized adjusts its components to the new size
    public void componentResized(ComponentEvent e) {
    	jessOutputTextArea.setBounds(new Rectangle(10, 10, this.getWidth()-27, this.getHeight()-90));
    	commandTextField.setBounds(new Rectangle(10, this.getHeight()-68, this.getWidth()-25, 24));
    }

    public void componentShown(ComponentEvent e) {
		
    }
	
	/**
	 * This method initializes jContentPane
	 * 
	 * @return javax.swing.JPanel
	 */
	private javax.swing.JPanel getJContentPane() {
		if(jContentPane == null) {
			jContentPane = new javax.swing.JPanel(new BorderLayout());
			jContentPane.setLayout(null);
			//jContentPane.add(getJessOutputScrollPane(), null);
			jContentPane.add(getJessOutputTextArea(), BorderLayout.NORTH);
			jContentPane.add(getCommandTextField(), BorderLayout.SOUTH);
			//jContentPane.add(getJessOutputTextPane(), null);
			jContentPane.addComponentListener(this);
			
		}
		return jContentPane;
	}
	
	/**
	 * This method initializes the command text field
	 * 
	 * @return javax.swing.JTextField
	 */
	private TextField getCommandTextField() {
		if (commandTextField == null) {
			commandTextField = new TextField();
			commandTextField.setBounds(new Rectangle(10, this.getHeight()-68, this.getWidth()-25, 24));
			commandTextField.addKeyListener(new java.awt.event.KeyAdapter() {
				public void keyReleased(java.awt.event.KeyEvent e) {
					//when the Enter key is pressed...
					if(e.getKeyCode() == java.awt.event.KeyEvent.VK_ENTER) {
						//...send command to jess engine
						jessOutputTextArea.append("---------------------------------------------------------------------------\n");
						jessOutputTextArea.append(commandTextField.getText() + "\n");
						if(jess != null) {
							try {
								Value v = jess.eval(commandTextField.getText());
								if(v.toString() != "nil")
									jessOutputTextArea.append(v.toString() + "\n");
	/*							try {
									//doc.insertString(doc.getLength(), "command sent: "+commandTextField.getText()+" \n", jessOutputTextPane.getStyle("Regular"));
								} catch(BadLocationException ble){
									ble.printStackTrace();
								}
	*/
							} catch(JessException je) {
								jessOutputTextArea.append(je.toString() + "\n");
	/*							try {
									doc.insertString(doc.getLength(), je.toString()+"\n", jessOutputTextPane.getStyle("Red"));
								} catch(BadLocationException ble){
									ble.printStackTrace();
								}
	*/
							}
						} else
							jessOutputTextArea.append("NormEnvEngine not specified\n");
						
						jessOutputTextArea.append("-------------------------\n");
						commandTextField.setText("");
					}
				}
			});
		}
		return commandTextField;
	}

	/**
	 * This method initializes informationEditorPane
	 * 	
	 * @return javax.swing.JEditorPane
	 */
	/*private JTextPane getJessOutputTextPane() {
		if (jessOutputTextPane == null) {
			jessOutputTextPane = new JTextPane();
			jessOutputTextPane.setEditable(false);
			jessOutputTextPane.setBounds(new Rectangle(10, 10, 492, 147));
		}
		return jessOutputTextPane;
	}*/
	
	/**
	 * Gets the information text area
	 * 
	 * @return	the information text area
	 */
	public TextArea getJessOutputConsole(){
		return jessOutputTextArea;
	}
	
	/**
	 * This method initializes jessOutputTextArea
	 * 	
	 * @return java.awt.TextArea
	 */
	private TextArea getJessOutputTextArea() {
		if (jessOutputTextArea == null) {
			jessOutputTextArea = new TextArea();
			jessOutputTextArea.setEditable(false);
			jessOutputTextArea.setBounds(new Rectangle(10, 10, this.getWidth()-27, this.getHeight()-90));
		}
		return jessOutputTextArea;
	}
	
}  //  @jve:decl-index=0:visual-constraint="10,10"

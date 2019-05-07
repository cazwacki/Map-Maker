import java.awt.Component;
import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.SpringLayout;

public class MapPrompt extends JDialog implements ActionListener {

	SpinnerModel dimXModel = new SpinnerNumberModel(30, 1, 65536, 1), 
			dimYModel = new SpinnerNumberModel(30, 1, 65536, 1);
	JSpinner dimX = new JSpinner(dimXModel);
	JSpinner dimY = new JSpinner(dimYModel);
	SpinnerModel spriteSize = new SpinnerNumberModel(32, 8, 512, 1);
	JSpinner spriteSpin = new JSpinner(spriteSize);
	JButton create;
	
	public MapPrompt(JFrame owner, String title) {
		super(owner, title, Dialog.ModalityType.APPLICATION_MODAL);
    	//JFrame frame = new JFrame("Debug");
    	JPanel result = new JPanel();
    	setSize(400, 200);
    	setLocation(80, 80);
    	setResizable(false);
    	SpringLayout sl = new SpringLayout();
    	create = new JButton("Create Map");
    	create.addActionListener(this);
    	JLabel dimXLabel = new JLabel("Number of Columns:"), 
    			dimYLabel = new JLabel("Number of Rows:"),
    			spriteSizeLabel = new JLabel("Sprite Size (#x#):");
    	result.setLayout(sl);
    	constrain(sl, spriteSpin, 125, 150);
    	constrain(sl, dimX, 45, 150);
    	constrain(sl, dimXLabel, 45, 25);
    	constrain(sl, dimY, 85, 150);
    	constrain(sl, dimYLabel, 85, 25);
    	constrain(sl, create, 65, 260);
    	constrain(sl, spriteSpin, 125, 150);
    	constrain(sl, spriteSizeLabel, 125, 23);
    	Component[] cs = {dimX, dimXLabel, dimY, dimYLabel, create, spriteSpin, spriteSizeLabel}; 
    	add(result, cs);
    	getContentPane().add(result);
	}
	
	public void add(JPanel panel, Component[] cs) {
    	for(Component c: cs) {
    		panel.add(c);
    	}
    }
    
    public void constrain(SpringLayout layout, Component c, int northShift, int westShift) {
    	layout.putConstraint(SpringLayout.NORTH, c, northShift, SpringLayout.NORTH, this);
    	layout.putConstraint(SpringLayout.WEST, c, westShift, SpringLayout.WEST, this);
    }
	
    public void render() {
    	setVisible(true);
    }
    
    public int getColumns() {
    	return (Integer)dimXModel.getValue();
    }
    
    public int getRows() {
    	return (Integer)dimYModel.getValue();
    }
    
	public void actionPerformed(ActionEvent arg0) {
		// TODO Auto-generated method stub
		if(arg0.getSource() == create) {
			MainScreen.mapColumns = getColumns(); 
			MainScreen.mapRows = getRows();
			JFrame temp = new JFrame("Paint Your Map");
			temp.setVisible(true);
			temp.requestFocus(); 
			new PaintScreen(temp, MainScreen.mapColumns, MainScreen.mapRows, (Integer)spriteSize.getValue());
			MainScreen.intro_frame.dispose();
			dispose();
		}
	}

}

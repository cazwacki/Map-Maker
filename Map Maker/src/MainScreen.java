import java.awt.GraphicsEnvironment;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SpringLayout;
import javax.swing.UIManager;
import javax.swing.filechooser.FileNameExtensionFilter;


public class MainScreen extends JPanel implements ActionListener {
	
	MapPrompt mapReq = new MapPrompt(new JFrame(), "Create Map");
	static PaintScreen ps = new PaintScreen(new JFrame(), null);
	static JFrame intro_frame;
    public static void main(String[] args) throws Exception {
    	UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    	JPanel intro_panel = new MainScreen();
        intro_frame = new JFrame("JAVA-based Map Creator");
        intro_frame.setVisible(true);
        intro_frame.setSize(500, 250);
        intro_frame.setResizable(false);  
        intro_frame.setLocation((GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDisplayMode().getWidth()-500)/2, (GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDisplayMode().getHeight()-250)/2);
        intro_frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        intro_frame.setContentPane(intro_panel);
    }
			
    static int mapRows;
    static int mapColumns;
    JButton load, new_map;
    JLabel welcome;
    
    public MainScreen() {
        ps.container.setVisible(false);
        ps.partner.container.setVisible(false);
    	SpringLayout buttonLayout = new SpringLayout();
        setLayout(buttonLayout);
        
        welcome = new JLabel("Map Creator v 1.0.0, Filetype .MBD");
        add(welcome);
        buttonLayout.putConstraint(SpringLayout.HORIZONTAL_CENTER, welcome, 0, SpringLayout.HORIZONTAL_CENTER, this);
        buttonLayout.putConstraint(SpringLayout.NORTH, welcome, 250/3, SpringLayout.NORTH, this);
        
        new_map = new JButton("New Map");
        add(new_map);
        new_map.addActionListener(this);
        buttonLayout.putConstraint(SpringLayout.NORTH, new_map, 250/3 + 50, SpringLayout.NORTH, this);
        buttonLayout.putConstraint(SpringLayout.EAST, new_map, -500/2 - 100, SpringLayout.EAST, this);
        
        load = new JButton("Load Map");
        add(load);
        load.addActionListener(this);
        buttonLayout.putConstraint(SpringLayout.NORTH, load, 250/3 + 50, SpringLayout.NORTH, this);
        buttonLayout.putConstraint(SpringLayout.WEST, load, 500/2 + 100, SpringLayout.WEST, this);
    }
    
    public static void prepareChooser(JFileChooser chooser) {
		chooser.setDialogType(JFileChooser.OPEN_DIALOG);
		chooser.setMultiSelectionEnabled(true);
	}
    
    public void initiateMap() {
    	mapReq.render();
    }
	
	public File loadFile() {
		JFileChooser fileSelect = new JFileChooser();
		prepareChooser(fileSelect);
		fileSelect.setDialogTitle("Open Map");
		fileSelect.setFileSelectionMode(JFileChooser.FILES_ONLY);
		fileSelect.addChoosableFileFilter(new FileNameExtensionFilter("Map Build Data", "mbd")); 
		fileSelect.setMultiSelectionEnabled(false);
		fileSelect.setAcceptAllFileFilterUsed(false);
		File uploadFile;
		if(fileSelect.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
			uploadFile = fileSelect.getSelectedFile();
			return uploadFile;
		} else {
			System.out.println("Cancelled Load");
		}
		return null;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		System.out.println(getClass().getSimpleName() + "." + "actionPerformed()");
		if(e.getSource() == load) {
			System.out.println("src = load");
			File file = loadFile();
			if(file != null) {
				ps.loadFromFile(file);
				ps.container.setVisible(true);
				ps.partner.container.setVisible(true);
				intro_frame.dispose();
			}
		}
		if(e.getSource() == new_map) {
			System.out.println("src = new_map");
			initiateMap();
			intro_frame.dispose();
		}
	}
}
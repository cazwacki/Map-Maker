import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.GraphicsEnvironment;
import java.awt.Image;

import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

public class SpriteScreen extends JPanel {
	public static final int SPRITE_LIST_WIDTH =  160;

   public static JFrame container;
   JScrollPane scrollpane = new JScrollPane();
   DefaultListModel<Image> sortModel = new DefaultListModel<Image>();
   JList<Image> sortList = new JList<Image>(sortModel);
   boolean newMap = true;
  
   public SpriteScreen(JFrame ccontainer) {
	   container = ccontainer;
	   ListRenderer renderer = new ListRenderer();
	   renderer.setHorizontalAlignment(JLabel.CENTER);
	   sortList.setLayoutOrientation(JList.VERTICAL_WRAP);
	   sortList.setCellRenderer(renderer);
	   sortList.setVisibleRowCount(-1);
	   container.setVisible(true);
       container.setSize(SPRITE_LIST_WIDTH, GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDisplayMode().getHeight()-100);
       container.setResizable(false);
       container.setLocation(20, 50);
       container.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
       container.setContentPane(this);
       setLayout(new BorderLayout());
       scrollpane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
       scrollpane.setViewportView(sortList);
       scrollpane.setSize(container.getSize());
       add(scrollpane);
   }
   
   public void addSprites(Image[] images) {
	   for(Image image : images) {
		   sortModel.addElement(image.getScaledInstance(64, 64, 0));
	   }
	   sortList.setVisibleRowCount(sortModel.getSize() / 2);
   }
   
   public void addSprite(Image image) {
	   sortModel.addElement(image.getScaledInstance(64, 64, 0));
	   sortList.setVisibleRowCount(sortModel.getSize() / 2);
   }
   
   public void paintComponent(Graphics g)  // draw graphics in the panel
   {
       int width = getWidth();             // width of window in pixels
       int height = getHeight();           // height of window in pixels

       super.paintComponent(g);            // call superclass to make panel display correctly
       
       

       // Drawing code goes here
   }
   
   public class ListRenderer extends DefaultListCellRenderer {
       @Override
       public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
           JLabel label = (JLabel) super.getListCellRendererComponent(
                   list, value, index, isSelected, cellHasFocus);
           label.setText("");
           label.setIcon(new ImageIcon(sortModel.getElementAt(index)));
           label.setHorizontalTextPosition(JLabel.RIGHT);
           return label;
       }
   }

}







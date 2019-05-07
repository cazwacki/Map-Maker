import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.filechooser.FileNameExtensionFilter;

public class PaintScreen extends JPanel implements ActionListener, MouseListener, WindowListener {
	ArrayList<Image> sprites = new ArrayList<Image>();
	ArrayList<ArrayList<Integer>> positions = new ArrayList<ArrayList<Integer>>();
	ArrayList<ArrayList<Rectangle2D>> onScreens = new ArrayList<ArrayList<Rectangle2D>>();

	Graphics2D g2d;
	JScrollPane pain;
	public String savePath = "New Map";
	JMenuItem new_map, open_map, close, save_map, save_map_as, sprite, spritesheet, map_export, extendN, extendE, extendS, extendW, crop;
	SpriteScreen partner = new SpriteScreen(new JFrame("Sprites"));
	public int dimensions = 0, columns, rows;
	String currentPaintPath;
	boolean changesMade = false;
	boolean initiated = false;
	boolean isPainting = true;
	boolean isHolding = false;
	boolean isInkDropping = false;
	double scale = 1;
	public int atrack = 0;
	MapPrompt mapReq = new MapPrompt(new JFrame(), "Create Map");
	JFrame container;
	ArrayList<JRadioButtonMenuItem> zooms = new ArrayList<JRadioButtonMenuItem>();

	Action inkdrop = new AbstractAction("Paint", new ImageIcon(((new ImageIcon("src/drop.png")).getImage()).getScaledInstance(30, 30, java.awt.Image.SCALE_SMOOTH))) {
		public void actionPerformed(ActionEvent e) {
			isInkDropping = true;
			isPainting = false;
		}
	};
	JButton inkdropButton;
	
	Action paint = new AbstractAction("Paint", new ImageIcon(((new ImageIcon("src/paint.png")).getImage()).getScaledInstance(30, 30, java.awt.Image.SCALE_SMOOTH))) {
		public void actionPerformed(ActionEvent e) {
			isPainting = true;
			isInkDropping = false;
		}
	};
	JButton paintButton;

	Action erase = new AbstractAction("Erase", new ImageIcon(((new ImageIcon("src/erase.png")).getImage()).getScaledInstance(30, 30, java.awt.Image.SCALE_SMOOTH))) {
		public void actionPerformed(ActionEvent e) {
			isPainting = false;
			isInkDropping = false;
		}
	};
	JButton eraseButton;

	public PaintScreen(JFrame ccontainer, File loadPath) {
		setLayout(new BorderLayout());
		container = ccontainer;
		container.addWindowListener(this);
		// initiated = true;
		if(loadPath != null) {
			loadFromFile(loadPath);	
		}

		System.out.println("columns = " + columns);
		System.out.println("rows = " + rows);
		System.out.println("dimensions = " + dimensions);

		intrFaceLoad();
	}

	public PaintScreen(JFrame ccontainer, int columns, int rows, int dim) {
		setLayout(new BorderLayout());
		container = ccontainer;
		container.addWindowListener(this);
		this.columns = columns;
		this.rows = rows;
		dimensions = dim;

		intrFaceLoad();

	}

	protected void loadFromFile(File file) {
		try {
			partner.sortModel.clear();
			sprites.clear();
			onScreens.clear();
			positions.clear();
			savePath = file.getAbsolutePath();
			container.setTitle(savePath);
			final ZipFile zipFile = new ZipFile(file);
			final Enumeration<? extends ZipEntry> entries = zipFile.entries();
			int[] colrow = new int[2];
			ArrayList<ArrayList<Integer>> loadPos = new ArrayList<ArrayList<Integer>>();
			int filesProcessed = 0;
			long millisStart = System.currentTimeMillis();
			while (entries.hasMoreElements()) {
				final ZipEntry zipEntry = entries.nextElement();
				final String fileName = zipEntry.getName();
				System.out.println("map archive has file " + fileName);
				if (fileName.endsWith(".txt")) {
					InputStream input = zipFile.getInputStream(zipEntry);
					BufferedReader br = new BufferedReader(new InputStreamReader(input, "UTF-8"));
					String line = br.readLine();
					System.out.println("Read from " + fileName + ": " + line);
					String[] split = line.split(" ");
					int counter = 0;
					for (int i = 0; i < split.length; i++) {
						if (i < 2) {
							colrow[i] = Integer.parseInt(split[i]);
						} else {
							if (counter == 0) {
								loadPos.add(new ArrayList<Integer>());
								onScreens.add(new ArrayList<Rectangle2D>());
							}
							loadPos.get(loadPos.size() - 1).add(Integer.parseInt(split[i]));
							counter++;
							System.out.println(counter);
							if (counter == colrow[1]) {
								System.out.println("Wrapping");
								counter = 0;
							}
							for (int a = 0; a < colrow[1]; a++) {
								onScreens.get(loadPos.size() - 1).add(new Rectangle2D.Double(dimensions * i * scale, dimensions * a * scale, dimensions * scale, dimensions * scale));
							}
						}
					}
					System.out.println("Loaded array:");
					for (int i = 0; i < loadPos.size(); i++) {
						System.out.println(loadPos.get(i));
						System.out.println("Size of above: " + loadPos.get(i).size());
					}
					br.close();
					input.close();
				} else {
					InputStream input = new BufferedInputStream(zipFile.getInputStream(zipEntry));
					Image image = ImageIO.read(input);
					input.close();
					sprites.add(image);
					dimensions = image.getWidth(null);
					partner.addSprite(image.getScaledInstance(32, 32, Image.SCALE_SMOOTH));
				}
				++filesProcessed;
			}
			columns = colrow[0];
			rows = colrow[1];
			positions = loadPos;
			System.out.println("rows = " + colrow[1] + "; columns = " + colrow[0]);
			System.out.println("positions = " + positions);
			zipFile.close();
			changesMade = true;
			long millisEnd = System.currentTimeMillis();
			System.out.println("loading took " + (millisEnd - millisStart) + "ms");
		} catch (final IOException ioe) {
			System.err.println("Unhandled exception:");
			ioe.printStackTrace();
			return;
		}
		resizeMap();
	}

	public void intrFaceLoad() {
		partner.container.requestFocus();
		setPreferredSize(new Dimension(columns * dimensions + 100, rows * dimensions + 100));
		pain = new JScrollPane(this);
		pain.setViewportView(this);
		pain.setHorizontalScrollBarPolicy(
				   JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
				pain.setVerticalScrollBarPolicy(
				   JScrollPane.VERTICAL_SCROLLBAR_ALWAYS); pain.setDoubleBuffered(true);
		pain.addMouseListener(this);
		container.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		JMenuBar menubar = new JMenuBar();

		JMenu file = new JMenu("File");
		file.setMnemonic(KeyEvent.VK_F);

		new_map = new JMenuItem("New Map...");
		new_map.addActionListener(this);
		new_map.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, ActionEvent.CTRL_MASK));

		open_map = new JMenuItem("Open Map");
		open_map.addActionListener(this);
		open_map.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, ActionEvent.CTRL_MASK));

		close = new JMenuItem("Close");
		close.addActionListener(this);
		close.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_4, ActionEvent.ALT_MASK));

		save_map = new JMenuItem("Save Map");
		save_map.addActionListener(this);
		save_map.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, ActionEvent.CTRL_MASK));

		save_map_as = new JMenuItem("Save Map As...");
		save_map_as.addActionListener(this);

		sprite = new JMenuItem("Import Sprite");
		sprite.addActionListener(this);
		sprite.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_I, ActionEvent.CTRL_MASK));

		spritesheet = new JMenuItem("Import Sprite Sheet");
		spritesheet.addActionListener(this);

		map_export = new JMenuItem("Export Map...");
		map_export.addActionListener(this);

		file.add(new_map);
		file.add(open_map);
		file.add(close);
		file.addSeparator();
		file.add(save_map);
		file.add(save_map_as);
		file.addSeparator();
		file.add(sprite);
		file.add(spritesheet);
		file.add(map_export);

		JMenu view = new JMenu("View");
		file.setMnemonic(KeyEvent.VK_V);

		JMenu zoom = new JMenu("Zoom in...");

		ButtonGroup percents = new ButtonGroup();
		for (int i = 25; i <= 200; i += 25) {
			zooms.add(new JRadioButtonMenuItem(i + "%"));
			zooms.get(zooms.size() - 1).addActionListener(this);
			if (i == 100) {
				zooms.get(zooms.size() - 1).setSelected(true);
			}
			percents.add(zooms.get(zooms.size() - 1));
			zoom.add(zooms.get(zooms.size() - 1));
		}

		view.add(zoom);

		JMenu edit = new JMenu("Edit");

		crop = new JMenuItem("Cut Unused Space");
		crop.addActionListener(this);
		edit.add(crop);

		JMenu extend = new JMenu("Extend Map");
		extendN = new JMenuItem("Northwards");
		extendN.addActionListener(this);
		extendE = new JMenuItem("Eastwards");
		extendE.addActionListener(this);
		extendS = new JMenuItem("Southwards");
		extendS.addActionListener(this);
		extendW = new JMenuItem("Westwards");
		extendW.addActionListener(this);
		extend.add(extendN);
		extend.add(extendS);
		extend.add(extendE);
		extend.add(extendW);
		edit.add(extend);
		//
		menubar.add(file);
		menubar.add(edit);
		menubar.add(view);
		container.setJMenuBar(menubar);
		JToolBar toolbar = new JToolBar("test", JToolBar.VERTICAL);
		paintButton = toolbar.add(paint);
		eraseButton = toolbar.add(erase);
		inkdropButton = toolbar.add(inkdrop);
		add(toolbar, BorderLayout.EAST);
		container.add(pain);
		container.setContentPane(pain);
		container.setLocation(100, 25);
		container.setSize(GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDisplayMode().getWidth() - 100,
				GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDisplayMode().getHeight() - 50);
		container.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		container.setVisible(true);
		container.toFront();
		partner.container.toFront();
		System.out.println(getClass().getSimpleName() + ": intrFaceLoad complete.");
		resizeMap();
	}

	public boolean mapInit(Graphics g, Graphics2D g2) {
		boolean mapChanged = false;
		g2.setColor(Color.BLUE);
		if (!initiated/* | (partner.sortList.getSelectedIndex() == -1) */) {
			System.out.println("initiated = " + initiated);
			System.out.println("partner.sortList.getSelectedIndex() = " + partner.sortList.getSelectedIndex());
			for (int i = 0; i < columns; i++) {
				if (!initiated) {
					positions.add(new ArrayList<Integer>());
					onScreens.add(new ArrayList<Rectangle2D>());
					for (int a = 0; a < rows; a++) {
						positions.get(i).add(-1);
						onScreens.get(i).add(new Rectangle2D.Double(dimensions * i * scale, dimensions * a * scale, dimensions * scale, dimensions * scale));
						g2.draw(onScreens.get(i).get(a));
					}
				}
			}
		}
		initiated = true;
		// if (partner.sortList.getSelectedIndex() > -1) {
		for (int i = 0; i < columns; i++) {
			for (int a = 0; a < rows; a++) {
				onScreens.get(i).set(a, new Rectangle2D.Double(dimensions * i * scale, dimensions * a * scale, dimensions * scale, dimensions * scale));
				g2.draw(onScreens.get(i).get(a));
				if (positions.get(i).get(a) > -1) {
					ImageIcon icon = new ImageIcon(sprites.get(positions.get(i).get(a)));
					BufferedImage temp = new BufferedImage(icon.getIconWidth(), icon.getIconHeight(), BufferedImage.TYPE_INT_ARGB);
					Graphics fake = temp.createGraphics();
					// paint the Icon to the BufferedImage.
					icon.paintIcon(null, fake, 0, 0);
					fake.dispose();
					g.drawImage(temp, (int) (dimensions * i * scale), (int) (dimensions * a * scale), (int) (dimensions * scale), (int) (dimensions * scale), null);
				}
			}
		}
		// }
		if (isHolding) {
			System.out.println("b");
			for (int i = 0; i < columns; i++) {
				for (int a = 0; a < rows; a++) {
					Point b = MouseInfo.getPointerInfo().getLocation();
					Point c = this.getLocationOnScreen();
					if (onScreens.get(i).get(a).contains(b.getX() - c.getX(), b.getY() - c.getY())) {
						if (isPainting) {
							positions.get(i).set(a, partner.sortList.getSelectedIndex());
						} else if(isInkDropping ){
							int tilePicked = positions.get(i).get(a);
							partner.sortList.setSelectedIndex(tilePicked);
							if(tilePicked == -1) {
								isPainting = false;
								isInkDropping = false;
								eraseButton.requestFocusInWindow();	
							} else {
								isPainting = true;
								isInkDropping = false;
								paintButton.requestFocusInWindow();
							}
						} else {
							positions.get(i).set(a, -1);
						}
						mapChanged = true;
						if (!container.getTitle().endsWith("*")) {
							container.setTitle(savePath + " *");
						}
					}
				}
			}
		}
		// System.out.println("mapInit returning " + mapChanged);
		return mapChanged;
	}

	private void resizeMap() {
		int width = (int) (dimensions * scale * columns) + 50;
		int height = (int) (dimensions * scale * rows) + 50;
		System.out.println("Resizing to: width = " + width + " height = " + height);
		setPreferredSize(new Dimension(width, height));
		revalidate();
		repaint();

	}

	public void paintComponent(Graphics g) {
		super.paintComponent(g); // call superclass to make panel display
									// correctly
		g2d = (Graphics2D) g;
		if (mapInit(g, g2d)) {
			repaint();
			pain.setPreferredSize(container.getSize());
		}
		if (changesMade = true) {
			// repaint();
			changesMade = false;
		}
		if (container.getTitle().endsWith("*") && container.getDefaultCloseOperation() != JFrame.DO_NOTHING_ON_CLOSE) {
			container.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		} else {
			container.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		}
		pain.revalidate();
		pain.repaint();
	}

	public FileNameExtensionFilter createFilter(String title, String end) {
		return new FileNameExtensionFilter(title, end);
	}

	public JFileChooser overwriteChecker() {
		return new JFileChooser() {
			@Override
			public void approveSelection() {
				File f = getSelectedFile();
				if (f.exists() && getDialogType() == SAVE_DIALOG) {
					int result = JOptionPane.showConfirmDialog(this, "The file exists, overwrite?", "Existing file", JOptionPane.YES_NO_CANCEL_OPTION);
					switch (result) {
					case JOptionPane.YES_OPTION:
						super.approveSelection();
						return;
					case JOptionPane.NO_OPTION:
						return;
					case JOptionPane.CLOSED_OPTION:
						return;
					case JOptionPane.CANCEL_OPTION:
						cancelSelection();
						return;
					}
				}
				super.approveSelection();
			}
		};
	}

	public void handleSaveMap() {
		try {
			if (savePath.equals("New Map")) {
				JFileChooser fileSelect = overwriteChecker();
				fileSelect.setDialogType(JFileChooser.SAVE_DIALOG);
				fileSelect.setDialogTitle("Save As");
				fileSelect.setFileSelectionMode(JFileChooser.FILES_ONLY);
				fileSelect.setFileFilter(new FileNameExtensionFilter("Map Build Data File", "mbd"));
				if (fileSelect.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
					String checkForEnd = fileSelect.getSelectedFile().getAbsolutePath();
					if (!checkForEnd.endsWith(".mbd")) {
						checkForEnd = checkForEnd + ".mbd";
					}
					savePath = checkForEnd;
				} else {
					return;
				}
			}
			FileOutputStream dest = new FileOutputStream(savePath);
			ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(dest));

			// adding images
			for (int i = 0; i < sprites.size(); i++) {
				ImageIcon icon = new ImageIcon(sprites.get(i));
				BufferedImage temp = new BufferedImage(icon.getIconWidth(), icon.getIconHeight(), BufferedImage.TYPE_INT_ARGB);
				Graphics fake = temp.getGraphics();
				icon.paintIcon(null, fake, 0, 0);
				fake.dispose();

				ZipEntry entry = new ZipEntry(i + ".png");
				out.putNextEntry(entry);
				ImageIO.write(temp, "png", out);
			}

			// adding position
			ZipEntry entry = new ZipEntry("data.txt");
			out.putNextEntry(entry);
			out.write((columns + " ").getBytes(Charset.forName("UTF-8")));
			out.write((rows + " ").getBytes(Charset.forName("UTF-8")));
			for (int i = 0; i < columns; i++) {
				String columnAssign = "";
				for (int a = 0; a < rows; a++) {
					columnAssign += positions.get(i).get(a) + " ";
				}
				out.write(columnAssign.getBytes(Charset.forName("UTF-8")));
			}
			out.flush();
			out.close();
			container.setTitle(savePath);
		} catch (FileNotFoundException e1) {
			JOptionPane.showMessageDialog(new JFrame("Failure"), "Unable to save. FileNotFoundException");
			e1.printStackTrace();
		} catch (IOException e1) {
			JOptionPane.showMessageDialog(new JFrame("Failure"), "Unable to save. IOException");
			e1.printStackTrace();
		}

	}

	public int leaveCurrent() {
		if (container.getTitle().endsWith("*")) {
			return JOptionPane.showConfirmDialog(new JFrame(), "Do you want to save before quitting?");
		}
		return -1563118;
	}

	public void handleExtendNorth() {
		try {
			int extension = Integer.parseInt(JOptionPane.showInputDialog(new JFrame("Extend North"), "Number of Tiles Upward"));
			rows += extension;
			for (int a = 0; a < columns; a++) {
				ArrayList<Integer> positionToUpdate = positions.get(a);
				ArrayList<Rectangle2D> squaresToUpdate = onScreens.get(a);
				for (int i = 0; i < extension; i++) {
					positionToUpdate.add(0, -1);
					squaresToUpdate.add(0, new Rectangle2D.Double(a * dimensions * scale, i * dimensions * scale, dimensions * scale, dimensions * scale));
				}
			}
			resizeMap();
		} catch (Throwable t) {
			JOptionPane.showMessageDialog(new JFrame("Error"), "Unable to extend map!");
		}
	}

	public void handleExtendSouth() {
		try {
			int extension = Integer.parseInt(JOptionPane.showInputDialog(new JFrame("Extend South"), "Number of Tiles Downward"));
			rows += extension;
			for (int a = 0; a < columns; a++) {
				ArrayList<Integer> positionToUpdate = positions.get(a);
				ArrayList<Rectangle2D> squaresToUpdate = onScreens.get(a);
				for (int i = 0; i < extension; i++) {
					positionToUpdate.add(-1);
					squaresToUpdate.add(new Rectangle2D.Double(a * dimensions * scale, i * dimensions * scale, dimensions * scale, dimensions * scale));
				}
			}
			resizeMap();
		} catch (Throwable t) {
			JOptionPane.showMessageDialog(new JFrame("Error"), "Unable to extend map!");
		}
	}

	public void handleExtendEast() {
		System.out.println("Debugging extend east cause it doesn't work right.");
		try {
			System.out.println("old number of columns = " + columns);
			int extension = Integer.parseInt(JOptionPane.showInputDialog(new JFrame("Extend East"), "Number of Tiles Right"));
			System.out.println("extension = " + extension);
			for (int a = 0; a < extension; a++) {
				ArrayList<Integer> newPositions = new ArrayList<Integer>();
				ArrayList<Rectangle2D> newRectangles = new ArrayList<Rectangle2D>();
				for (int i = 0; i < rows; i++) {
					newPositions.add(-1);
					newRectangles.add(new Rectangle2D.Double((columns + a) * dimensions, i * dimensions, dimensions, dimensions));
				}
				positions.add(newPositions);
				onScreens.add(newRectangles);
			}
			columns += extension;
			System.out.println("new number of columns = " + columns);
			resizeMap();
		} catch (Throwable t) {
			JOptionPane.showMessageDialog(new JFrame("Error"), "Unable to extend map!");
		}
	}

	public void handleExtendWest() {
		try {
			int extension = Integer.parseInt(JOptionPane.showInputDialog(new JFrame("Extend West"), "Number of Tiles Left"));
			for (int a = 0; a < extension; a++) {
				ArrayList<Integer> newPositions = new ArrayList<Integer>();
				ArrayList<Rectangle2D> newRectangles = new ArrayList<Rectangle2D>();
				for (int i = 0; i < rows; i++) {
					newPositions.add(-1);
					newRectangles.add(new Rectangle2D.Double(a * dimensions * scale, i * dimensions * scale, dimensions * scale, dimensions * scale));
				}
				positions.add(0, newPositions);
				onScreens.add(0, newRectangles);
			}
			columns += extension;
			resizeMap();
		} catch (Throwable t) {
			JOptionPane.showMessageDialog(new JFrame("Error"), "Unable to extend map!");
		}
	}

	public void handleNewMap() {
		int temp = leaveCurrent();
		if (temp == JOptionPane.CANCEL_OPTION) {
			return;
		} else if (temp == JOptionPane.YES_OPTION) {
			handleSaveMap();
			container.dispose();
			partner.container.dispose();
			mapReq.render();
		} else {
			container.dispose();
			partner.container.dispose();
			mapReq.render();
		}
	}

	public void handleOpenMap() {
		if (atrack == 1 || !container.getTitle().endsWith("*")) {
			JFileChooser fileSelect = new JFileChooser();
			fileSelect.setDialogType(JFileChooser.OPEN_DIALOG);
			fileSelect.setMultiSelectionEnabled(true);
			fileSelect.setDialogTitle("Open Map");
			fileSelect.setFileSelectionMode(JFileChooser.FILES_ONLY);
			fileSelect.addChoosableFileFilter(new FileNameExtensionFilter("Map Build Data", "mbd"));
			fileSelect.setMultiSelectionEnabled(false);
			fileSelect.setAcceptAllFileFilterUsed(false);
			File uploadFile = null;
			if (fileSelect.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
				uploadFile = fileSelect.getSelectedFile();
			} else {
				System.out.println("Cancelled Load");
			}
			if (uploadFile != null) {
				loadFromFile(uploadFile);
			}
		}
	}

	public void handleSaveMapAs() {
		JFileChooser fileSelect = overwriteChecker();
		fileSelect.setDialogType(JFileChooser.SAVE_DIALOG);
		fileSelect.setDialogTitle("Save As");
		fileSelect.setFileSelectionMode(JFileChooser.FILES_ONLY);
		fileSelect.setFileFilter(new FileNameExtensionFilter("Map Build Data File", "mbd"));
		if (fileSelect.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
			String checkForEnd = fileSelect.getSelectedFile().getAbsolutePath();
			if (!checkForEnd.endsWith(".mbd")) {
				checkForEnd = checkForEnd + ".mbd";
			}
			savePath = checkForEnd;
			try {

				FileOutputStream dest = new FileOutputStream(checkForEnd);
				ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(dest));

				// adding images
				for (int i = 0; i < sprites.size(); i++) {
					ImageIcon icon = new ImageIcon(sprites.get(i));
					BufferedImage temp = new BufferedImage(icon.getIconWidth(), icon.getIconHeight(), BufferedImage.TYPE_INT_ARGB);
					Graphics fake = temp.getGraphics();
					icon.paintIcon(null, fake, 0, 0);
					fake.dispose();

					ZipEntry entry = new ZipEntry(i + ".png");
					out.putNextEntry(entry);
					ImageIO.write(temp, "png", out);
				}

				// adding position
				ZipEntry entry = new ZipEntry("data.txt");
				out.putNextEntry(entry);
				out.write((columns + " ").getBytes(Charset.forName("UTF-8")));
				out.write((rows + " ").getBytes(Charset.forName("UTF-8")));
				for (int i = 0; i < columns; i++) {
					String columnAssign = "";
					for (int a = 0; a < rows; a++) {
						columnAssign += positions.get(i).get(a) + " ";
					}
					out.write(columnAssign.getBytes(Charset.forName("UTF-8")));
				}
				out.flush();
				out.close();
				JOptionPane.showMessageDialog(new JFrame("Success"), "File Saved!");
				container.setTitle(savePath);
			} catch (FileNotFoundException e1) {
				JOptionPane.showMessageDialog(new JFrame("Failure"), "Unable to save. FileNotFoundException");
				e1.printStackTrace();
			} catch (IOException e1) {
				JOptionPane.showMessageDialog(new JFrame("Failure"), "Unable to save. IOException");
				e1.printStackTrace();
			}
		}

	}

	public void handleMapExport() {
		BufferedImage result = new BufferedImage(columns * dimensions, rows * dimensions, // work
				// these
				// out
				BufferedImage.TYPE_INT_ARGB);
		Graphics temp = result.createGraphics();
		for (int i = 0; i < columns; i++) {
			for (int a = 0; a < rows; a++) {
				if (positions.get(i).get(a) > -1) {
					ImageIcon icon = new ImageIcon(sprites.get(positions.get(i).get(a)));
					icon.paintIcon(null, temp, i * dimensions, a * dimensions);
				}
			}
		}
		temp.dispose();
		try {
			JFileChooser fileSelect = overwriteChecker();
			fileSelect.setDialogType(JFileChooser.SAVE_DIALOG);
			fileSelect.setDialogTitle("Export");
			fileSelect.setFileSelectionMode(JFileChooser.FILES_ONLY);
			fileSelect.setFileFilter(new FileNameExtensionFilter("PNG File", "png"));
			if (fileSelect.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
				String checkForEnd = fileSelect.getSelectedFile().getAbsolutePath();
				if (!checkForEnd.endsWith(".png")) {
					checkForEnd = checkForEnd + ".png";
				}
				ImageIO.write(result, "png", new File(checkForEnd));
				JOptionPane.showMessageDialog(new JFrame("Success"), "Exported to " + checkForEnd + "!");
			}
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}

	public void handleSprite() {
		JFileChooser fileSelect = new JFileChooser();
		fileSelect.setDialogType(JFileChooser.OPEN_DIALOG);
		fileSelect.setDialogTitle("Open File(s)");
		fileSelect.setFileSelectionMode(JFileChooser.FILES_ONLY);
		fileSelect.addChoosableFileFilter(new FileNameExtensionFilter("Image Files", "png", "jpg", "jpeg", "gif"));
		fileSelect.setMultiSelectionEnabled(true);
		fileSelect.setAcceptAllFileFilterUsed(true);
		File[] files;
		if (fileSelect.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
			files = fileSelect.getSelectedFiles();
			for (File imgFile : files) {
				Image image;
				try {
					image = ImageIO.read(imgFile);
					BufferedImage bimg = (BufferedImage) image;
					if (bimg.getHeight() != dimensions || bimg.getWidth() != dimensions) {
						JOptionPane.showMessageDialog(null, "Dimensions Inappropriate! (width?height!=dim)");
					} else {
						sprites.add(image);
						partner.addSprite(image.getScaledInstance(32, 32, Image.SCALE_SMOOTH));
						changesMade = true;
					}
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
		}
	}

	public void handleSpritesheet() {
		JFileChooser fileSelect = new JFileChooser();
		fileSelect.setDialogType(JFileChooser.OPEN_DIALOG);
		fileSelect.setDialogTitle("Open File(s)");
		fileSelect.setFileSelectionMode(JFileChooser.FILES_ONLY);
		fileSelect.addChoosableFileFilter(new FileNameExtensionFilter("Image Files", "png", "jpg", "jpeg", "gif"));
		fileSelect.setMultiSelectionEnabled(true);
		fileSelect.setAcceptAllFileFilterUsed(true);
		File[] files;
		if (fileSelect.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
			files = fileSelect.getSelectedFiles();
			for (File imgFile : files) {
				Image image;
				try {
					image = ImageIO.read(imgFile);
					BufferedImage bimg = (BufferedImage) image;
					if (bimg.getHeight() % dimensions != 0 || bimg.getWidth() % dimensions != 0) {
						JOptionPane.showMessageDialog(null, "Dimensions Inappropriate! (width?height%32!=0)");
					} else if (bimg.getHeight() < dimensions || bimg.getWidth() < dimensions) {
						JOptionPane.showMessageDialog(null, "Dimensions Inappropriate! (width?height<dim)");
					} else {
						for (int y = 0; y < bimg.getHeight() / dimensions; y++) {
							for (int x = 0; x < bimg.getWidth() / dimensions; x++) {
								sprites.add(bimg.getSubimage(x * dimensions, y * dimensions, dimensions, dimensions));
								partner.addSprite(bimg.getSubimage(x * dimensions, y * dimensions, dimensions, dimensions).getScaledInstance(128, 128, Image.SCALE_SMOOTH));
								changesMade = true;
							}
						}
					}
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		}
		// return null;
	}

	public void handleCrop() {
		System.out.println("CHECKING!");
		boolean needsCropping = true;
		//check north
		int currCol = 0;
		while(currCol != columns && needsCropping) {
			for(int i = 0; i < rows; i++) {
				if(positions.get(currCol).get(i) != -1) {
					needsCropping = false;
				}
			}
			if(needsCropping) {
				positions.remove(currCol);
				onScreens.remove(currCol);
				columns--;
				currCol--;
			}
			currCol++;
		}
		needsCropping = true;
		//check south
		currCol = columns - 1;
		while(currCol != 0 && needsCropping) {
			for(int i = 0; i < rows; i++) {
				if(positions.get(currCol).get(i) != -1) {
					needsCropping = false;
				}
			}
			if(needsCropping) {
				positions.remove(currCol);
				onScreens.remove(currCol);
				columns--;
			}
			currCol--;
		}
		needsCropping = true;
		int currRow = 0;
		while(currRow != rows && needsCropping) {
			for(int i = 0; i < columns; i++) {
				if(positions.get(i).get(currRow) != -1) {
					needsCropping = false;
				}
			}
			if(needsCropping) {
				rows--;
				for(int i = 0; i < columns; i++) {
					positions.get(i).remove(currRow);
					onScreens.get(i).remove(currRow);
				}
				currRow--;
				
			}
			currRow++;
		}
		needsCropping = true;
		currRow = rows - 1;
		while(currRow != 0 && needsCropping) {
			for(int i = 0; i < columns; i++) {
				if(positions.get(i).get(currRow) != -1) {
					needsCropping = false;
				}
			}
			if(needsCropping) {
				rows--;
				for(int i = 0; i < columns; i++) {
					positions.get(i).remove(currRow);	
					onScreens.get(i).remove(currRow);
					System.out.println("Removed " + currRow);
				}
			}
			currRow--;
		}
		repaint();
	}
	
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == extendN) {
			handleExtendNorth();
		} else if (e.getSource() == extendS) {
			handleExtendSouth();
		} else if (e.getSource() == extendE) {
			handleExtendEast();
		} else if (e.getSource() == extendW) {
			handleExtendWest();
		} else if (e.getSource() == crop) {
			handleCrop();
		} else if (e.getSource() == new_map) {
			handleNewMap();
		} else if (e.getSource() == open_map) {
			handleOpenMap();
		} else if (e.getSource() == save_map && container.getTitle().endsWith("*")) {
			handleSaveMap();
		} else if (e.getSource() == save_map_as) {
			handleSaveMapAs();
		} else if (e.getSource() == map_export) {
			handleMapExport();
		} else if (e.getSource() == sprite) {
			handleSprite();
		} else if (e.getSource() == spritesheet) {
			handleSpritesheet();
		} else {
			for (JRadioButtonMenuItem item : zooms) {
				if (e.getSource() == item) {
					int scaleChange = Integer.parseInt(item.getText().substring(0, item.getText().length() - 1));
					scale = scaleChange / 100.0;
					resizeMap();
				}
			}
		}
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		// TODO Auto-generated method stub
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseExited(MouseEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mousePressed(MouseEvent e) {
		isHolding = true;
		repaint();
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		isHolding = false;
	}

	@Override
	public void windowActivated(WindowEvent arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void windowClosed(WindowEvent arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void windowClosing(WindowEvent arg0) {
		if (container.getTitle().endsWith("*")) {
			int dialog = JOptionPane.showConfirmDialog(new JFrame(), "Do you want to save before quitting?");
			if (dialog == JOptionPane.YES_OPTION) {
				try {
					JFileChooser fileSelect = overwriteChecker();
					if (savePath.equals("New Map")) {
						fileSelect.setDialogType(JFileChooser.SAVE_DIALOG);
						fileSelect.setDialogTitle("Save As");
						fileSelect.setFileSelectionMode(JFileChooser.FILES_ONLY);
						fileSelect.setFileFilter(new FileNameExtensionFilter("Map Build Data File", "mbd"));
						String checkForEnd = null;
						if (fileSelect.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
							checkForEnd = fileSelect.getSelectedFile().getAbsolutePath();
						} else if (fileSelect.showOpenDialog(this) == JFileChooser.CANCEL_OPTION) {
							atrack = 1;
							return;
						} else {
							System.exit(0);
						}
						if (!checkForEnd.endsWith(".mbd")) {
							checkForEnd = checkForEnd + ".mbd";							
						}
						savePath = checkForEnd;
					}
					System.out.println("I'm writing to:" + savePath);
					FileOutputStream dest = new FileOutputStream(savePath);
					ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(dest));

					// adding images
					for (int i = 0; i < sprites.size(); i++) {
						ImageIcon icon = new ImageIcon(sprites.get(i));
						BufferedImage temp = new BufferedImage(icon.getIconWidth(), icon.getIconHeight(), BufferedImage.TYPE_INT_RGB);
						Graphics fake = temp.getGraphics();
						icon.paintIcon(null, fake, 0, 0);
						fake.dispose();

						ZipEntry entry = new ZipEntry(i + ".png");
						out.putNextEntry(entry);
						ImageIO.write(temp, "png", out);
					}

					// adding position
					ZipEntry entry = new ZipEntry("data.txt");
					out.putNextEntry(entry);
					out.write((columns + " ").getBytes(Charset.forName("UTF-8")));
					out.write((rows + " ").getBytes(Charset.forName("UTF-8")));
					for (int i = 0; i < columns; i++) {
						String columnAssign = "";
						for (int a = 0; a < rows; a++) {
							columnAssign += positions.get(i).get(a) + " ";
						}
						out.write(columnAssign.getBytes(Charset.forName("UTF-8")));
					}
					out.flush();
					out.close();
					dest.close();
					container.setTitle(savePath);
					System.exit(0);
				} catch (FileNotFoundException e1) {
					JOptionPane.showMessageDialog(new JFrame("Failure"), "Unable to save. FileNotFoundException");
					e1.printStackTrace();
				} catch (IOException e1) {
					JOptionPane.showMessageDialog(new JFrame("Failure"), "Unable to save. IOException");
					e1.printStackTrace();
				}
			} else if (dialog == JOptionPane.NO_OPTION) {
				System.exit(0);
			} else {
				return;
			}
		}
	}

	@Override
	public void windowDeactivated(WindowEvent arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void windowDeiconified(WindowEvent arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void windowIconified(WindowEvent arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void windowOpened(WindowEvent arg0) {
		// TODO Auto-generated method stub

	}

}

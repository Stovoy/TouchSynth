import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.JFileChooser;
import javax.swing.SwingUtilities;

import processing.core.PApplet;
import controlP5.Button;
import controlP5.CColor;
import controlP5.ControlEvent;
import controlP5.ControlP5;
import controlP5.DropdownList;
import controlP5.Slider;
import controlP5.Textlabel;

@SuppressWarnings({ "serial" })
public class SongViewer extends PApplet
{
	boolean[][] notes;

	final int width = 16;
	final int windowWidth = 321;
	final int windowHeight = 720;
	final int blockLength = 20;
	boolean turningOn = true;
	boolean moving = false;
	String infoText;
	int xGrid = 0;
	Timer scrollTimer;
	Timer scrollDelayDec;
	
	boolean[][] visible;
	Textlabel menuText;
	boolean scheduled = false;
	
	ControlP5 controlP5;
	DropdownList settings;
	int menuValues[][];
	int menuMins[];
	int menuMaxes[];
	int menuIndex = -1;
	float scrollDelay = 100;
	static String fileName = "";
	static boolean blank = true;
	
	public static void main(String[] args)
	{
		if (args.length == 1)
		{
			File file = new File(args[0]);
			System.out.println(file.length());
			if (file.exists())
			{
				fileName = file.getPath();
				if (file.length() == 2354) blank = false;
			}			
		}
	    PApplet.main(new String[] { "--hide-stop", "SongViewer" });
	}
	public void setup()
	{
		this.setName("Song Viewer");
		//volume, speed
		menuValues = new int[2][48];
		for (int i = 0; i < 48; ++i)
		{
			menuValues[0][i] = 8;
			menuValues[1][i] = 1;
		}
		menuMins = new int[]{1, 1};
		menuMaxes = new int[]{15, 48};
		scrollTimer = new Timer();
		scrollDelayDec = new Timer();
		infoText = "";
		notes = new boolean[24][768];
		visible = new boolean[24][16];
		size(windowWidth, windowHeight);
		for (int i = 0; i < 24; ++i)
		{
			for (int j = 0; j < 48; ++j)
			{
				notes[i][j] = false;
			}
		}
		fill (180, 180, 180);
		rect(0, 0, 320, 239);	
		Point mousePos = checkMousePosition();
		if (mousePos.x > -1)
		{
			textAlign(CENTER);
			infoText = getBlockInfo(mousePos);
			fill(255, 255, 255);
			text(getBlockInfo(mousePos), 160, 20);
		}
		controlP5 = new ControlP5(this);
		CColor controllerColor = new CColor();
		controllerColor.setActive(color(180-32));
		controllerColor.setBackground(color(180));
		controllerColor.setForeground(color(180));
		Button dec = controlP5.addButton("dec", 0, 120, 20, 12, 10);
		dec.setLabel("-");
		dec.setVisible(false);
		dec.setColor(controllerColor);
		Button inc = controlP5.addButton("inc", 0, 190, 20, 12, 10);
		inc.setLabel("+");
		inc.setVisible(false);
		inc.setColor(controllerColor);
		Button open = controlP5.addButton("open", 0, 50, 20, 25, 8);
		open.setLabel("Open");
		Button save = controlP5.addButton("save", 0, 243, 20, 28, 8);
		save.setLabel("Save");
		Button load = controlP5.addButton("load", 0, 243, 48, 28, 8);
		load.setLabel("Load");
		settings = controlP5.addDropdownList("Settings", 137, 30, 48, 100);
		settings.addItem("Tempo", 0);
		settings.addItem("Col. Sets", 1);
		settings.setLabel("Settings");
		menuText = controlP5.addTextlabel("menuText", "", 120, 10);
		Slider s = controlP5.addSlider("xGrid", 0, menuValues[1][0]*16-16, xGrid, 90, 180, 100, 10);
		s.setVisible(false);
		s.setSliderMode(Slider.FLEXIBLE);
		s.setLabel("Position");
		controllerColor.setActive(color(211, 144, 0));
		controllerColor.setBackground(color(55, 160, 200));
		controllerColor.setForeground(color(0, 105, 145));
		s.setColor(controllerColor);
		controlP5.disableShortcuts();
		controlP5.setMoveable(false);
		
		if (fileName != "" && !blank)
			try {load();} 
			catch (IOException e) {}
	}
	public void draw()
	{
		setVisible();
		drawVisible();
		drawTopSquare();
		handleMouseOver();
		if (mousePressed)
			mousePressed();
		else
			moving = false;
	}
	private void drawVisible()
	{
		for (int i = 0; i < 24; ++i)
		{
			for (int j = 0; j < 16; ++j)
			{
				stroke(0,0,0);
				if (visible[i][j])
					fill(255, 102, 0);
				else
					fill(128,128,128);
				rect(j*blockLength, 239+i*blockLength, blockLength, blockLength);
			}
		}
	}
	void drawTopSquare()
	{
		fill (180, 180, 180);
		rect(0, 0, 320, 239);
		textAlign(CENTER);
		fill(255, 255, 255);
		text(infoText, 160, 120);
		drawTouchscreen();
	}
	void drawTouchscreen()
	{
		stroke(0, 0, 0);
		//Main square
		line(50, 0, 50, 239);
		line(270, 0, 270, 239);
	}
	private Point checkMousePosition()
	{
		if (mouseY >= 720 || mouseX >= 320 || mouseX <= 0) return new Point(-1, 0);
		if (mouseY < 240) return new Point(-1, -1);
		int x = mouseX/blockLength;
		int y = (mouseY - 240)/blockLength;
		return new Point(x, y);
	}
	private void setVisible()
	{
		if (menuIndex >= 0) 
			updateMenuText();
		if (xGrid >= menuValues[1][0]*16-16) xGrid = menuValues[1][0]*16-16;
		controlP5.controller("xGrid").setValue(xGrid);
		if (xGrid <= menuValues[1][0]*16-16)
		{
			for (int i = 0; i < 24; ++i)
			{
				for (int j = xGrid; j < xGrid+16; ++j)
				{
					visible[i][j-xGrid] = notes[i][j];
				}
			}
		}
		else
		{
			for (int i = 0; i < 24; ++i)
			{
				for (int j = menuValues[1][0]*16-16; j < menuValues[1][0]*16; ++j)
				{
					System.out.println(menuValues[1][0]);
					visible[i][j-menuValues[1][0]*16-16] = notes[i][j];
				}
			}
		}
	}
	public void mousePressed()
	{
		Point mousePos = checkMousePosition();
		int x, y;
		x = mousePos.x;
		y = mousePos.y;
		if (x > -1)
		{
			if (!moving)
			{
				if (notes[y][x+xGrid]) turningOn = false;
				else turningOn = true;
			}
			moving = true;
			notes[y][x+xGrid] = turningOn;
			stopScrolling();
		}
		if (y == -1)
		{
			fill(255, 102, 0);
			stroke(0, 0, 0);
			if (mouseX > 270)
			{
				controlP5.controller("xGrid").setMousePressed(false);
				rect(270, 0, 320, 239);
				if (!scheduled)
				{
					scrollDelay = 100;
					scrollTimer.scheduleAtFixedRate(new right(), 0, (long) scrollDelay);
					scrollDelayDec.scheduleAtFixedRate(new decScrollDelay(), 0, 100);
					scheduled = true;
				}
			}
			else if (mouseX < 50)
			{
				controlP5.controller("xGrid").setMousePressed(false);
				rect(0, 0, 50, 239);
				if (!scheduled)
				{
					scrollDelay = 100;
					scrollTimer.scheduleAtFixedRate(new left(), 0, (long) scrollDelay);
					scrollDelayDec.scheduleAtFixedRate(new decScrollDelay(), 0, 100);
					scheduled = true;
				}
			}
			else
				stopScrolling();
		}
	}
	private void handleMouseOver()
	{
		Point mousePos = checkMousePosition();
		if (mousePos.x > -1)
		{
			controlP5.controller("xGrid").setMousePressed(false);
			infoText = getBlockInfo(mousePos);
		}
	}
	public void mouseReleased()
	{
		if (mouseY < 240)
		{
			fill (180, 180, 180);
			rect(0, 0, 320, 239);
			if (scheduled)
				stopScrolling();
			drawTopSquare();
		}		
	}
	private void pauseScrolling()
	{
		scrollTimer.cancel();
		scrollTimer = new Timer();	
	}
	private void stopScrolling()
	{
		pauseScrolling();
		scheduled = false;
		scrollDelayDec.cancel();
		scrollDelayDec = new Timer();
	}
	private String getBlockInfo(Point blockPosition)
	{
		  String noteNames[] = {"A3 ", "A#3", "B3 ", "C4 ", "C#4", "D4 ", "D#4", "E4 ", "F4 ", "F#4", "G4 ", "G#4", "A4 ", "A#4", "B4 ", "C5 ", "C#5", "D5 ", "D#5", "E5 ", "F5 ", "F#5", "G5 ", "G#5"};
		  return noteNames[23-blockPosition.y] + "\nColumn: " + (blockPosition.x+1+xGrid);
	}
	class right extends TimerTask
	{
		public void run()
		{
			if (xGrid == menuValues[1][0]*16-16) return;
			++xGrid;
			pauseScrolling();
			scrollTimer.scheduleAtFixedRate(new right(), (long) scrollDelay, (long) scrollDelay);
		}
	}
	class left extends TimerTask
	{
		public void run()
		{
			if (xGrid == 0) return;
			--xGrid;
			pauseScrolling();
			scrollTimer.scheduleAtFixedRate(new left(), (long) scrollDelay, (long) scrollDelay);
		}
	}
	class decScrollDelay extends TimerTask
	{
		public void run()
		{
			scrollDelay = scrollDelay > 10 ? scrollDelay -= 2 : scrollDelay;
		}
	}
	public void controlEvent(ControlEvent theEvent) 
	{
		if (theEvent.isGroup())
		{
			menuIndex = (int) theEvent.group().value();
			menuText.setValueLabel("Value: " + menuValues[menuIndex][xGrid/16]);
			controlP5.controller("inc").setVisible(true);
			controlP5.controller("dec").setVisible(true);
		}
	}
	public void dec()
	{
		if (menuIndex == -1) return;
		else if (menuIndex < 1)
		{
			if (menuValues[menuIndex][xGrid/16] > menuMins[menuIndex]) --menuValues[menuIndex][xGrid/16];
		}
		else
		{
			if (menuValues[1][0] < menuMaxes[menuIndex]) --menuValues[1][0];
			if (menuValues[1][0] == 1) controlP5.controller("xGrid").setVisible(false);
			else controlP5.controller("xGrid").setMax(menuValues[1][0]*16-16);
		}
		updateMenuText();
	}	
	public void inc()
	{
		if (menuIndex == -1) return;
		else if (menuIndex < 1)
		{
			if (menuValues[menuIndex][xGrid/16] < menuMaxes[menuIndex]) ++menuValues[menuIndex][xGrid/16];
		}
		else
		{
			if (menuValues[1][0] < menuMaxes[menuIndex]) ++menuValues[1][0];
			if (menuValues[1][0] >= 2)
			{
				controlP5.controller("xGrid").setMax(menuValues[1][0]*16-16);
				activateScrollBar();
			}
		}
		updateMenuText();
	}
	public void activateScrollBar()
	{
		controlP5.controller("xGrid").setVisible(true);
		controlP5.controller("xGrid").setMax(menuValues[1][0]*16-16);
	}
	public void open(final int action) //0 nothing 1 save 2 load
	{ 
		SwingUtilities.invokeLater(new Runnable()
			{
				public void run()
				{
					try 
					{
						JFileChooser fc = new JFileChooser(sketchPath);
						fc.setFileFilter(new compFileFilter());
						if (fc.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) 
							fileName = fc.getSelectedFile().getPath();
						if (action == 1) save();
						else if (action == 2) load();
					}
					catch (Exception e){}
				}
			}
		);
	}
	private void updateMenuText()
	{
		menuText.setValueLabel("Value: " + menuValues[menuIndex][menuIndex < 2 ? xGrid/16 : 0]);
	}
	public void save() throws IOException
	{
		if (fileName == "") open(1);
		if (fileName == "") return;
		if (!fileName.endsWith(".comp")) fileName += ".comp";
		OutputStream out = new FileOutputStream(fileName);
		final int globalSettings = 241;
		int localSettings[] = new int[48];
		for (int i = 0; i < 48; ++i)
		{
			localSettings[i] = 0;
			localSettings[i] += (menuValues[0][i]) << 1;
			localSettings[i] += 1;
		} 
		out.write(globalSettings);
		for (int i = 0; i < 48; ++i)
			out.write(localSettings[i]);
		out.write(menuValues[1][0]);
		for (int i = 0; i < 24; ++i)
		{
			int position = 0;
			int value = 0;
			for (int j = 0; j < 768; ++j)
			{
				if (position < 8)
					value += (notes[i][j] ? 1 : 0) << 7-position;
				else
					value += (notes[i][j] ? 1 : 0) << 15-position;
				position = (position == 15 ? 0 : position+1);
				if (position == 8) 
				{
					out.write(value);
					value = 0;
				}
				else if (position == 0)
				{
					out.write(value);
					value = 0;
				}
			}
		}
		out.close();
	}
	public void load() throws IOException
	{
		if (fileName == "") open(2);
		if (fileName == "") return;
		InputStream in = new FileInputStream(fileName);
		if (in.available() != 2354) return;
		byte dataB[] = new byte[2354];
		in.read(dataB);
		int data[] = new int[2354];
		int position = 0;
		for (int i = 0; i < 2354; ++i)
			data[i] = (dataB[i] >= 0 ? dataB[i] : 256+dataB[i]);
		dataB = null;
		for (position = 1; position < 49; ++position)
		{
			menuValues[0][position-1] = ((data[position] & 30) >> 1);
		}
		menuValues[1][0] = data[position++];
		if (menuValues[1][0] > 1) activateScrollBar();
		else controlP5.controller("xGrid").setVisible(false);
		for (int i = 0; i < 24; ++i)
		{
			int value = 0;
			for (int j = 0; j < 48; ++j)
			{
				value = data[position++];
				for (int k = 0; k < 8; ++k)
				{
					notes[i][j*16+k] = (value & (1 << (7-k))) != 0 ? true : false;
				}
				value = data[position++];
				for (int k = 0; k < 8; ++k)
				{
					notes[i][j*16+k+8] = (value & (1 << (7-k))) != 0 ? true : false;
				}
			}
		}
		setVisible();
	}
}

class compFileFilter extends javax.swing.filechooser.FileFilter
{
	public boolean accept(File f)
	{
		return f.isDirectory() || f.getName().toLowerCase().endsWith(".comp");
	}
	public String getDescription()
	{
		return ".comp files";
	}
}
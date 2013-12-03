import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import processing.core.PApplet;
import processing.serial.Serial;
import controlP5.Button;
import controlP5.CColor;
import controlP5.ControlEvent;
import controlP5.ControlP5;
import controlP5.Slider;
import controlP5.Textfield;
@SuppressWarnings({ "unused", "serial" })

public class FileManager extends PApplet
{
	private List<String> files;
	private Serial myPort = null;
	private ControlP5 controlP5;
	int listPos;
	int selected = -1;
    String selectedStr;
	boolean renaming = false;

	ArrayList<Byte> data;
	ArrayList<Integer> dataInt;
	boolean doDownload = false, writeData = false;
	int dataPos = 0;
	private boolean doUpload, dataRead = false;
	int previousListPos = 0;
	String currentText = "";
	
	String textDrawn = "";
	int serialPort = -1;
	public static void main(String[] args)
	{
	    PApplet.main(new String[] { "--hide-stop", "FileManager" });
	}
	public void setup()
	{
		size(400, 400);
		readAllFiles();
		if (files.size() == 0)
			currentText = "No compositions found";
		ArrayList <Button> compositions = new ArrayList <Button>();
		
		controlP5 = new ControlP5(this);
		for (int i = 0; i < 20; ++i)
			compositions.add(controlP5.addButton(Integer.toString(i), 0, 0, 20*i, 195, 20));
		for (int i = 0; i < files.size() && i < 20; ++i)
			compositions.get(i).setLabel(files.get(i).substring(0, files.get(i).length()-5));
		for (int i = files.size(); i < 20; ++i)
			compositions.get(i).setVisible(false);
		Slider s = controlP5.addSlider("listPos", 0, (files.size() > 20 ? files.size()-20 : 0), files.size()-1, 195, 0, 10, 400);
		s.setSliderMode(Slider.FLEXIBLE);
		s.setLabelVisible(false);
		s.setLabelVisible(false);
		s.showTickMarks(false);
		readAllFiles();
		if (files.size() > 20)
		{
			s.setNumberOfTickMarks(files.size()-19);
			s.showTickMarks(false);
			s.setMax(files.size()-20);
			s.setValue(files.size()-19);
		}
		controlP5.addButton("upload", 0, 205, 80, 195, 20);
		controlP5.addButton("download", 0, 205, 100, 195, 20);
		controlP5.addButton("delete", 0, 205, 0, 195, 20);
		controlP5.addButton("rename", 0, 205, 20, 195, 20);
		controlP5.addButton("edit", 0, 205, 40, 195, 20);
		controlP5.addButton("deselect", 0, 205, 60, 195, 20);
		Button addFile = controlP5.addButton("addFile", 0, 205, 0, 195, 20);
		addFile.setLabel("add file");
		controlP5.addButton("link", 0, 205, 380, 195, 20);
		controlP5.addButton("refresh", 0, 205, 20, 195, 20);
		Slider p = controlP5.addSlider("progress", 0, 100, 0, 205, 200, 195, 20);
		p.setVisible(false);
		hideFileOptions();
		controlP5.setMoveable(false);
		controlP5.disableShortcuts();
		refresh();
	}	
	public void draw()
	{
		stroke(0);
		line(200, 0, 200, 400);
		if (writeData)
		{
			writeData = false;
			finishDownload();
		}
		if (doDownload)
		{
			while (myPort.available() < 2354) System.out.print("");
			dataPos += myPort.available();
			data.addAll(readByteArrayList());
			if (dataPos == 2354)
			{
				writeData = true;
				doDownload = false;
				dataPos = 0;
				hideAll();
				showFileOptions();
			}
		}
		else if (doUpload)
		{
			if (!dataRead)
				try 
				{
					dataInt = new ArrayList<Integer>();
					InputStream in = new FileInputStream(sketchPath + "\\compositions\\" + files.get(selected));
					byte dataB[] = new byte[2354];
					in.read(dataB);
					for (int i = 0; i < dataB.length; ++i)
						dataInt.add(dataB[i] >= 0 ? dataB[i] : 256+dataB[i]);
					dataRead = true;
					in.close();
					
				} catch (IOException e) {}
			controlP5.controller("progress").setLock(true);
			controlP5.controller("progress").setVisible(true);
			for (int i = 0; i < 128 && dataPos < 2354; ++i)
			{
				myPort.write(dataInt.get(dataPos++));
				try {Thread.sleep(5);} catch(Exception e){};
			}
			getOkay();
			int value = (int) ((int)dataPos/2354.0*100 + 1);
			controlP5.controller("progress").setValue((float) value);
			controlP5.controller("progress").setValueLabel((float) value + "%");

			if (dataPos == 2354)
			{
				dataPos = 0;
				dataRead = false;
				controlP5.controller("progress").setVisible(false);
				doUpload = false;
				hideAll();
				showFileOptions();
			}
		}
		else
			data = new ArrayList<Byte>();
		if (listPos != previousListPos)
		{
			refresh();
			previousListPos = listPos;
		}
		if (!textDrawn.equals(currentText))
		{
			stroke(205);
			fill(205);
			rect(200, 250, 200, 100);
			fill(0);
			textAlign(CENTER);
			text(currentText, width/4*3, height/4*3);
			textDrawn = currentText;
		}
	}
	public void controlEvent(ControlEvent theEvent) 
	{
		if (!theEvent.isGroup())
		{
			if (theEvent.controller().position().x == 0)
			{
				if (theEvent.controller().name().charAt(0) != 'r')
				{
					if (renaming) return;
					if (selected != -1)
						controlP5.controller(Integer.toString(selected)).setColor(theEvent.controller().getColor());
					if (!Integer.toString(selected).equals(theEvent.controller().name()))
					{
						selected = Integer.parseInt(theEvent.controller().name());
						selectedStr = theEvent.controller().label();
						if (theEvent.controller().label().equals(selectedStr))
						{
							CColor highlighted = theEvent.controller().getColor();
							highlighted.setBackground(highlighted.getBackground()+150);
							theEvent.controller().setColor(highlighted);	
						}
					}
					showFileOptions();
				}
				else
				{
					String newName = theEvent.controller().valueLabel().toString();
					File file = new File(sketchPath + "\\compositions\\" + controlP5.controller(Integer.toString((int)theEvent.controller().position().y/20)).label() + ".comp");
					controlP5.controller(Integer.toString((int)theEvent.controller().position().y/20)).setLabel(newName);
					if (!new File(sketchPath + "\\compositions\\" + newName + ".comp").exists()) 
						file.renameTo(new File(sketchPath + "\\compositions\\" + newName + ".comp"));
					else
						file.delete();
					theEvent.controller().remove();
					refresh();
					selected = -1;
					if (selected == -1) showMainOptions();
					else showFileOptions();
					renaming = false;
				}
			}
		}
	}
	private void hideFileOptions()
	{
		fill(205);
		rect(200, 0, 200, 400);
		showMainOptions();
		controlP5.controller("upload").setVisible(false);
		controlP5.controller("download").setVisible(false);
		controlP5.controller("delete").setVisible(false);
		controlP5.controller("rename").setVisible(false);
		controlP5.controller("edit").setVisible(false);
		controlP5.controller("deselect").setVisible(false);
	}
	private void showFileOptions()
	{
		fill(205);
		rect(200, 0, 200, 400);
		hideMainOptions();
		if (myPort != null)
		{
			controlP5.controller("upload").setVisible(true);
			controlP5.controller("download").setVisible(true);
		}
		controlP5.controller("delete").setVisible(true);
		controlP5.controller("rename").setVisible(true);
		controlP5.controller("edit").setVisible(true);	
		controlP5.controller("deselect").setVisible(true);
	}
	private void hideMainOptions()
	{
		controlP5.controller("addFile").setVisible(false);
		controlP5.controller("refresh").setVisible(false);
	}
	private void showMainOptions()
	{
		controlP5.controller("addFile").setVisible(true);
		controlP5.controller("refresh").setVisible(true);
	}
	private void hideAll()
	{
		fill(205);
		rect(200, 0, 200, 400);
		controlP5.controller("upload").setVisible(false);
		controlP5.controller("download").setVisible(false);
		controlP5.controller("delete").setVisible(false);
		controlP5.controller("rename").setVisible(false);
		controlP5.controller("edit").setVisible(false);
		controlP5.controller("deselect").setVisible(false);
		controlP5.controller("addFile").setVisible(false);
		controlP5.controller("refresh").setVisible(false);
	}
	private void addFile()
	{
		Random rand = new Random();
		File file = new File(sketchPath + "\\compositions\\.comp");
		try 
		{
			file.createNewFile();
		} catch (IOException e) {}
		file.deleteOnExit();
		if (files.size() > 20)
			listPos = files.size()-20;
		else
			listPos = 0;
		refresh();
		for (int i = 0; i < files.size() && i < 20; ++i)
		{
			if (controlP5.controller(Integer.toString(i)).label().equals(""))
			{
				selected = i;
				break;
			}
		}
		rename();
	}
	private void upload()
	{
		//myPort.readBytes();
		doUpload = true;
		myPort.write((byte)2); //1 upload, 2 download
	}
	private void download()
	{
		/*Protocol
		 * One byte - Size of song (number of column sets [16 note intervals])
		 * Note data for each column set - 24 words per column set.
		 */
		byte temp[] = new byte[myPort.available()];
		myPort.readBytes(temp);
		myPort.write((byte)1); //1 upload, 2 download
		doDownload = true;
	}
	private void link()
	{
		myPort = null;
		ArrayList <Serial> testPorts = new ArrayList<Serial>();
		for (int i = 0; i < Serial.list().length; ++i)
			testPorts.add(new Serial(this, Serial.list()[i], 38400));
		currentText = "Link failed";
		for (int i = 0; i < Serial.list().length; ++i)
		{
			int j;
			for (j = 0; j < 1000; ++j)
			{
				testPorts.get(i).write((byte)3);
				if (testPorts.get(i).read() == 128) break;
			}
			if (j != 1000)
			{
				serialPort = i;
				myPort = testPorts.get(i);
				controlP5.controller("link").setVisible(false);
				fill(205);
				stroke(205);
				rect(200, 380, 200, 20);
				currentText = "Linked successfully"; 
				break;
			}
		}
		for (int i = 0; i < Serial.list().length; ++i)
		{
			if (i != serialPort) testPorts.get(i).dispose();
		}
	}
	private void finishDownload()
	{
		ArrayList<Integer> convertedData = new ArrayList<Integer>();
		for (int i = 0; i < data.size(); ++i)
			convertedData.add(data.get(i) >= 0 ? data.get(i) : 256+data.get(i));
		File file = new File(sketchPath + "\\compositions\\" + files.get(selected));
		if (file.exists() && file.isFile())
		{
			try 
			{
				OutputStream out = new FileOutputStream(file);				
				for (int i = 0; i < convertedData.size(); ++i)
					out.write(convertedData.get(i));
			}
			catch (IOException e){};
			openSongViewer(sketchPath + "\\compositions\\" + controlP5.controller(Integer.toString(selected)).label() + ".comp");
		}
	}
	
	private void delete()
	{
		File file = new File(sketchPath + "\\compositions\\" + files.get(selected));
		boolean success = false;
		String name = files.get(selected);
		controlP5.controller(Integer.toString(selected)).setVisible(false);
		file.delete();
		files.remove(selected);
		fill(255);
		textAlign(CENTER);
		deselect();
		refresh();
	}
	private void rename()
	{
		if (controlP5.controller("renameDialog") == null)
		{
			File file = new File(sketchPath + "\\compositions\\" + files.get(selected));
			controlP5.controller(Integer.toString(selected)).setVisible(false);
			Textfield tf = controlP5.addTextfield("renameDialog", 0, 20*selected, 195,  20);
			tf.setLabel("");
			tf.keepFocus(true);
			hideAll();
			renaming = true;
		}
	}
	private void deselect()
	{
		controlP5.controller(Integer.toString(selected)).setColor(controlP5.controller("deselect").getColor());
		selected = -1;
		hideFileOptions();
	}
	boolean initListPos = true;
	private void refresh()
	{
		stroke(205);
		fill(205);
		rect(0, 0, 200, 400);
		stroke(0);
		line(200, 0, 200, 400);
		readAllFiles();
		if (files.size() == 0)
		{
			currentText = "No compositions found";
			return;
		}
		else
			currentText = "";
		for (int i = (int) (controlP5.controller("listPos").max()-listPos), j = 0; i < files.size() && j < 20; ++i, ++j)
		{
			controlP5.controller(Integer.toString(j)).setVisible(true);
			controlP5.controller(Integer.toString(j)).setLabel(files.get(i).substring(0, files.get(i).length()-5));
		}
		if (files.size() < 20)
			for (int i = files.size(); i < 20; ++i)
				controlP5.controller(Integer.toString(i)).setVisible(false);
		Slider s = (Slider) controlP5.controller("listPos");
		if (files.size() > 20)
		{
			s.unlock();
			if (s.max() != files.size()-20)
			{
				s.remove();
				s = controlP5.addSlider("listPos", 0, (files.size() > 20 ? files.size()-20 : 0), files.size()-1, 195, 0, 10, 400);
				s.setSliderMode(Slider.FLEXIBLE);
				s.setLabelVisible(false);
				s.setValue(files.size()-19);
				s.setNumberOfTickMarks(files.size()-19);
				s.showTickMarks(false);
			}
			for (int i = 0; i < 20; ++i)
			{
				Button temp = (Button) controlP5.controller(Integer.toString(i));
				CColor normal = controlP5.controller("link").getColor();
				if (temp.label().equals(selectedStr))
				{
					CColor highlighted = normal;
					highlighted.setBackground(highlighted.getBackground()+150);
					temp.setColor(highlighted);
					highlighted.setBackground(highlighted.getBackground()-150);
					selected = i;
				}
				else
					temp.setColor(normal);
			}
		}
		else
		{
			s.setSliderMode(Slider.FIX);
			s.setValue(0);
			s.lock();
		}
	}
	private void giveOkay()
	{
		myPort.write(128);
	}
	private void getOkay()
	{
		while (myPort.available() == 0);
	}
	private ArrayList<Byte> readByteArrayList()
	{
		ArrayList <Byte> data = new ArrayList <Byte>();
		byte[] dataA = myPort.readBytes(); 
		for (byte i : dataA)
			data.add(i);
		return data;
	}
	
	public void readAllFiles()
	{
		files = new ArrayList<String>(Arrays.asList(listFileNames(sketchPath + "\\compositions")));
		for (int i = 0; i < files.size(); ++i)
		{
			if (files.get(i).lastIndexOf('.') == -1)
			{
				files.remove(i--);
				continue;
			}
			if (!files.get(i).substring(files.get(i).lastIndexOf('.')).equals(".comp"))
			{
				files.remove(i--);
			}
		}
	}
	
	String[] listFileNames(String dir) 
	{
		File file = new File(dir);
		if (file.isDirectory()) 
		{
			String names[] = file.list();
			return names;
		} 
		
		
		
		
		else 
		{
			return null;
		}
	}
	public void edit()
	{
		openSongViewer(sketchPath + "\\compositions\\" + controlP5.controller(Integer.toString(selected)).label() + ".comp");
	}
	private void openSongViewer(String fileName)
	{
		String runString = "java -jar \"" + sketchPath + "\\Song Viewer.jar\"";
		if (fileName != "") runString += " " + "\"" + fileName + "\"";
		try {Process p = Runtime.getRuntime().exec(runString); p.waitFor();}
		catch (IOException e) {} catch (InterruptedException e) {}
	}
}

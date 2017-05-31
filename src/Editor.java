import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextPane;
import javax.swing.KeyStroke;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;

public class Editor implements ActionListener {

	// TextAreaPanel currentTab;

	ArrayList<Boolean> savedFiles = new ArrayList<Boolean>();
	JFrame frame;
	JTabbedPane panel;
	File currentFile;
	String[] fileItems = { "New", "Open", "Save", "Save as", "Close", "Exit" };
	String[] editItems = { "Cut", "Copy", "Paste" };
	String[] viewItems = {};
	private HashMap<String,HashSet<String>> keywords = new HashMap<String,HashSet<String>>();
	private String copiedText;
	
	public void runGraphics() throws IOException {

		
		parseKeywords(new File("keywords.txt"));
		
		System.out.println(keywords);
		
		frame = new JFrame("Text Editor");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		panel = new JTabbedPane();

		frame.setContentPane(panel);

		JMenuBar menuBar = new JMenuBar();

		JMenu fileMenu = new EditorMenu("File");

		for (String s : fileItems) {

			JMenuItem item = new JMenuItem(s);
			setKeyMnemonic(item);
			item.addActionListener(this);
			fileMenu.add(item);

		}

		JMenu editMenu = new EditorMenu("Edit");

		for (String s : editItems) {

			JMenuItem item = new JMenuItem(s);
			setKeyMnemonic(item);
			item.addActionListener(this);
			editMenu.add(item);

		}

		JMenu viewMenu = new EditorMenu("Edit");

		for (String s : viewItems) {

			JMenuItem item = new JMenuItem(s);
			setKeyMnemonic(item);
			item.addActionListener(this);
			viewMenu.add(item);

		}

		// menu = new JMenu("Another Menu");

		menuBar.add(fileMenu);

		menuBar.add(editMenu);

		frame.setJMenuBar(menuBar);

		// frame.setSize(new Dimension(400,400));

		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		
		int width = (int)screenSize.getWidth();
		
		int height = (int)screenSize.getHeight();
		
		
		//frame.setMinimumSize(new Dimension(300,600));
		
		frame.setPreferredSize(new Dimension((int)(height/1.25),(int)(height/1.25)));
		
		frame.pack();
		
		int x = width/2-frame.getWidth()/2;
		int y = height/2-frame.getHeight()/2;
		
		frame.setLocation(x, y);
		
		

		handleNew();

		menuBar.setVisible(true);
		frame.setVisible(true);

		// a submenu

	}
	
	public void parseKeywords(File f) throws IOException{
		
		BufferedReader reader = new BufferedReader(new FileReader(f));
		
		StringBuilder builder = new StringBuilder("");
		String next;
		
		while((next = reader.readLine()) != null){
			builder.append(next).append(System.lineSeparator());
		}
		
		String text = builder.toString();
		
		Pattern p = Pattern.compile("(?<=\\[)\\w*(?=\\])");
		
		ArrayList<String> languages = new ArrayList<String>();
		
		Matcher m = p.matcher(text);
		
		while(m.find()){
			System.out.println("found");
			String nextl = m.group();
			System.out.println(nextl);
			languages.add(nextl);
		}
		
		String[] sections = text.split("\\s*\\[\\w*\\]\\s*");
		
		System.out.println(Arrays.toString(sections));
		
		System.out.println("first element : " +sections[0]);
		
		for(int i =0;i<languages.size();i++){
			
			HashSet<String> temp = new HashSet<String>();
			
			String[] keywords = sections[i+1].trim().split("\\s+");
			
			System.out.println(Arrays.toString(keywords));
			for(String keyword: keywords){
				
				temp.add(keyword);
				
			}
			
			this.keywords.put(languages.get(i), temp);
		}
	}
	
	public int getLinesTextPane(JTextPane pane){
		
		int lines = 1;
		
		Pattern p = Pattern.compile("\\\n");
		
		Matcher m = p.matcher(pane.getText());
	
		while(m.find()){
			lines++;
		}
		
		return lines;
	}
	
	
		
		
	
	public void setLineListener(TextAreaPanel p, LineNumberList lines){
		
		JTextPane area = p.getTextArea();
		
		area.getDocument().addDocumentListener(new DocumentListener() {
			
			 public void insertUpdate(DocumentEvent e) {
			        
				 	changeLines();
				 	
			    }
			    public void removeUpdate(DocumentEvent e) {
			        
			    	changeLines();
			    		
			    }
			    public void changedUpdate(DocumentEvent e) {
			    
			    	changeLines();
			    	
			    }
			    
			  
			 public void changeLines(){
				 
				 lines.drawLineNumbers(getLinesTextPane(area));
			 }
			    
			    
		});
	}

	public void setKeyMnemonic(JMenuItem item) {

		switch (item.getText()) {
		case "New":

			item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N,ActionEvent.CTRL_MASK));
			break;
		case "Open":

			item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O,ActionEvent.CTRL_MASK));

			break;
		case "Save":

			item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S,ActionEvent.CTRL_MASK));

			break;
		case "Save as":

			//item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S,ActionEvent.CTRL_MASK & ActionEvent.ALT_MASK));
			break;
		case "Cut":

			item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X,ActionEvent.CTRL_MASK));
			break;
		case "Copy":

			item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C,ActionEvent.CTRL_MASK));
			break;
		case "Paste":

			item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_P,ActionEvent.CTRL_MASK));
			break;
		case "Close":

			item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE,ActionEvent.ALT_MASK));
			break;

		case "Exit":
		
			break;
			
		default:
			
			break;
		}

	}

	public void actionPerformed(ActionEvent e) {

		if (e.getSource() instanceof JMenuItem) {

			JMenuItem source = (JMenuItem) e.getSource();

			switch (source.getText()) {

			case "New":
				
				handleNew();
			
				break;
			case "Open":

				handleOpen();

				break;
			case "Save":

				handleSave();

				break;
			case "Save as":

				handleSaveAs();
				break;
			case "Cut":

				handleCut();
				break;
			case "Copy":

				handleCopy();
				break;
			case "Paste":

				handlePaste();
				break;
			case "Close":

				handleClose();
				break;

			case "Exit":

				frame.dispose();
				break;
			}

		}

	}

	public void handleNew() {

		
		JPanel pane = new JPanel(new BorderLayout());
		
		JPanel temp = new JPanel();
		
		temp.setPreferredSize(new Dimension(50,0));
		
		//System.out.println(temp.getBackground());
		
		LineNumberList lineNumbers = new LineNumberList();
		
		
		lineNumbers.setMargin(new Insets(-2,0,0,0));
		
				
		temp.add(lineNumbers);
		
		pane.add(temp,BorderLayout.WEST);
		
		TextAreaPanel textArea = new TextAreaPanel();
		
		textArea.setIsNew(true);
		
		setLineListener(textArea,lineNumbers);
		
		pane.add(textArea,BorderLayout.CENTER);

		Scroller tab = new Scroller(pane);
		

		tab.getVerticalScrollBar().setUnitIncrement(25);

		panel.addTab("new " + (panel.getTabCount() + 1) + ".txt", tab);
		
		textArea.setFile(new File("new " + (panel.getTabCount() + 1) + ".txt"));

		panel.setSelectedIndex(panel.getTabCount() - 1);

		savedFiles.add(true);

		setSavedListener(textArea, panel.getTabCount() - 1);

	}

	public void handleOpen() {

		JFileChooser fileChooser = new JFileChooser();

		int returned = fileChooser.showOpenDialog(frame);

		if (returned == JFileChooser.APPROVE_OPTION) {

			openFile(fileChooser.getSelectedFile());
		}

		else if (returned == JFileChooser.CANCEL_OPTION) {

		}

		else {

			JOptionPane.showMessageDialog(frame, "Invalid file choice.");

		}

	}

	public void handleSave() {

		if (panel.getTabCount() > 0) {

			TextAreaPanel currentTab = ((Scroller) panel.getSelectedComponent()).getTextArea();

			int selected = panel.getSelectedIndex();

			if (currentTab.getIsNew()) {
				
				JFileChooser fileChooser = new JFileChooser();

				int returned = fileChooser.showSaveDialog(frame);

				if (returned == JFileChooser.APPROVE_OPTION) {

					File file = fileChooser.getSelectedFile();
					
					//String fileExtension = file.getName().split("\\.")
					
					if (file.exists()) {

						returned = JOptionPane.showConfirmDialog(frame,
								"Do you wish to overwrite file " + file.getName(), "Overwrite File",
								JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);

						if (returned == JOptionPane.YES_OPTION) {
							
							String fileName = file.getName();
							
							panel.setTitleAt(selected, fileName);
							
							if(fileName.contains(".")){
								String extension = fileName.split("\\.")[1];
								if(keywords.keySet().contains(extension)){
									currentTab.setKeywords(keywords.get(extension));
								}
							
							
								try{
									currentTab.updateKeywords();
								}
								catch(BadLocationException e){
									e.printStackTrace();
								}
							}
							
							try {
								
								
								FileWriter writer = new FileWriter(file);

								writer.write(currentTab.getText());

								writer.close();

								savedFiles.set(selected, true);

								setSavedListener(currentTab, selected);
							} catch (IOException err) {
								JOptionPane.showMessageDialog(frame, "File could not be saved");
							}
						}

						else if (returned == JOptionPane.NO_OPTION) {

						}
					}

					else {
						
						String fileName = file.getName();
						
						panel.setTitleAt(selected, fileName);
						
						if(fileName.contains(".")){
							String extension = fileName.split("\\.")[1];
							if(keywords.keySet().contains(extension)){
								currentTab.setKeywords(keywords.get(extension));
							}
						
						
							try{
								currentTab.updateKeywords();
							}
							catch(BadLocationException e){
								e.printStackTrace();
							}
						}
						
						try {
							FileWriter writer = new FileWriter(file);

							writer.write(currentTab.getText());

							writer.close();

							savedFiles.set(selected, true);

							setSavedListener(currentTab, selected);
						} catch (IOException err) {
							JOptionPane.showMessageDialog(frame, "File could not be saved");
						}
					}

				}
				
				currentTab.setIsNew(false);
			}

			else {

				try {
					FileWriter writer = new FileWriter(new File(currentTab.getFileName()));

					writer.write(currentTab.getText());

					writer.close();

					savedFiles.set(selected, true);

					setSavedListener(currentTab, selected);

				} catch (IOException err) {
					JOptionPane.showMessageDialog(frame, "File could not be saved");
				}

			}

		}

		else {

			JOptionPane.showMessageDialog(frame, "There is no file to be saved");
		}
	}

	public void setSavedListener(TextAreaPanel currentTab, int selected) {

		currentTab.getTextArea().getDocument().addDocumentListener(new DocumentListener() {

			public void changedUpdate(DocumentEvent e) {
				update();
			}

			public void removeUpdate(DocumentEvent e) {
				update();
			}

			public void insertUpdate(DocumentEvent e) {
				update();
			}

			public void update() {

				System.out.println("selected: " + selected);
				savedFiles.set(selected, false);
				currentTab.getTextArea().getDocument().removeDocumentListener(this);
			}
		});

	}

	public void handleSaveAs() {

		if (panel.getTabCount() > 0) {

			TextAreaPanel currentTab = ((Scroller) panel.getSelectedComponent()).getTextArea();

			int selected = panel.getSelectedIndex();

			JFileChooser fileChooser = new JFileChooser();

			int returned = fileChooser.showSaveDialog(frame);

			if (returned == JFileChooser.APPROVE_OPTION) {

				File file = fileChooser.getSelectedFile();
				
				
				if (file.exists()) {

					returned = JOptionPane.showConfirmDialog(frame, "Do you wish to overwrite file " + file.getName(),
							"Overwrite File", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);

					if (returned == JOptionPane.YES_OPTION) {
						
						String fileName = file.getName();
						
						panel.setTitleAt(selected, fileName);
						
						if(fileName.contains(".")){
							String extension = fileName.split("\\.")[1];
							if(keywords.keySet().contains(extension)){
								currentTab.setKeywords(keywords.get(extension));
							}
						
						
							try{
								currentTab.updateKeywords();
							}
							catch(BadLocationException e){
								e.printStackTrace();
							}
						}
						
						try {
							
							
							FileWriter writer = new FileWriter(file);

							writer.write(currentTab.getText());

							writer.close();

							savedFiles.set(selected, true);

							setSavedListener(currentTab, selected);
						} catch (IOException err) {
							JOptionPane.showMessageDialog(frame, "File could not be saved");
						}
					}

					else if (returned == JOptionPane.NO_OPTION) {

					}
				}

				else {
					
					String fileName = file.getName();
					
					panel.setTitleAt(selected, fileName);
					
					if(fileName.contains(".")){
						String extension = fileName.split("\\.")[1];
						if(keywords.keySet().contains(extension)){
							currentTab.setKeywords(keywords.get(extension));
						}
					
					
						try{
							currentTab.updateKeywords();
						}
						catch(BadLocationException e){
							e.printStackTrace();
						}
					}
				
					try {

						
						FileWriter writer = new FileWriter(file);

						writer.write(currentTab.getText());

						writer.close();

						savedFiles.set(selected, true);

						Files.deleteIfExists(Paths.get(currentTab.getFilePath()));
						
						setSavedListener(currentTab, selected);
					} catch (IOException err) {
						JOptionPane.showMessageDialog(frame, "File could not be saved");
					}
				}

			}
		}

		else {

			JOptionPane.showMessageDialog(frame, "There is no file to be saved");
		}

	}

	public void handleClose() {

		int selected = panel.getSelectedIndex();

		if (savedFiles.get(selected)) {

			System.out.println("this file is already saved");
			panel.remove(selected);

			savedFiles.remove(selected);

		}

		else {

			TextAreaPanel tab = ((Scroller) panel.getSelectedComponent()).getTextArea();
			int returned = JOptionPane.showConfirmDialog(frame, "Do you want to save file " + tab.getFileName(),
					"Save File", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);

			if (returned == JOptionPane.YES_OPTION) {

				handleSave();

				panel.remove(selected);

				savedFiles.remove(selected);

			}

			else if (returned == JOptionPane.NO_OPTION) {

				panel.remove(selected);

				savedFiles.remove(selected);

			}
		}
	}

	public void handleCopy() {

		TextAreaPanel currentTab = ((Scroller) panel.getSelectedComponent()).getTextArea();

		copiedText = currentTab.getTextArea().getSelectedText();

	}

	public void handleCut() {

		TextAreaPanel currentTab = ((Scroller) panel.getSelectedComponent()).getTextArea();

		JTextPane textArea = currentTab.getTextArea();

		copiedText = textArea.getSelectedText();

		textArea.replaceSelection("");

	}

	public void handlePaste() {

		TextAreaPanel currentTab = ((Scroller) panel.getSelectedComponent()).getTextArea();

		JTextPane textArea = currentTab.getTextArea();

		int selecStart = textArea.getSelectionStart();
		int selecEnd = textArea.getSelectionEnd();

		System.out.println(textArea.getSelectedText());

		if (selecStart == selecEnd) {
			try{
				textArea.getStyledDocument().insertString(selecStart, copiedText, null);
			}
			catch(BadLocationException e){
				e.printStackTrace();
			}
		}

		else {
			textArea.replaceSelection(copiedText);
		}
	}

	public void openFile(File file) {

		JPanel pane = new JPanel(new BorderLayout());
		
		JPanel temp = new JPanel();
		
		temp.setPreferredSize(new Dimension(50,0));
		
		//System.out.println(temp.getBackground());
		
		LineNumberList lineNumbers = new LineNumberList();
		
		
		lineNumbers.setMargin(new Insets(-2,0,0,0));
		
				
		temp.add(lineNumbers);
		
		pane.add(temp,BorderLayout.WEST);
		
		TextAreaPanel textArea = new TextAreaPanel();
		
		
		String fileName = file.getName();
		
		
		
		System.out.println(fileName);
		textArea.setFile(file);
		
		setLineListener(textArea,lineNumbers);
		
		pane.add(textArea,BorderLayout.CENTER);

		Scroller tab = new Scroller(pane);

		tab.getVerticalScrollBar().setUnitIncrement(25);

		panel.addTab(file.getName(), tab);

		if (file.exists()) {
			BufferedReader reader = null;

			try {
				reader = new BufferedReader(new FileReader(file));
			} catch (FileNotFoundException err) {
				JOptionPane.showMessageDialog(frame, "That file does not exist");
			}

			StringBuilder input = new StringBuilder("");

			String line;

			try {
				while ((line = reader.readLine()) != null) {

					input.append(line).append("\n");

				}
			} catch (IOException err) {
				err.printStackTrace();
			}

			savedFiles.add(true);
			
			//System.out.println(input.toString());
			
			textArea.setText(input.toString());
			
			
			if(fileName.contains(".")){
				
				String extension = fileName.split("\\.")[1];
				System.out.println("extension: " + extension);
				
				if(keywords.keySet().contains(extension)){
					textArea.setKeywords(keywords.get(extension));
				}
			
			
				try{
					System.out.println("updating keywords");
					textArea.updateKeywords();
					textArea.setKeywordListener();
				}
				catch(BadLocationException e){
					e.printStackTrace();
				}
			}
			
			setSavedListener(textArea, panel.getTabCount() - 1);
			
			
			panel.setSelectedIndex(panel.getTabCount() - 1);

			JOptionPane.showMessageDialog(frame, "You chose file " + textArea.getFileName());

		}

		else {

			JOptionPane.showMessageDialog(frame, "File " + textArea.getFileName() + " does not exist.");

		}
	}

}

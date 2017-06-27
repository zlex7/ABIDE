import java.awt.BorderLayout;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
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
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import javax.swing.text.TabSet;
import javax.swing.text.TabStop;

public class Editor implements ActionListener {

	// TextAreaPanel currentTab;

	//determines which tabs have safed files
	//TODO: change this method of checking whether files are saved. It's prone to mistakes
	ArrayList<Boolean> savedFiles = new ArrayList<Boolean>();

	//overall frame
	JFrame frame;

	//the outer pane containing tabs with text in them
	JTabbedPane panel;

	//This isn't used anywhere
	File currentFile;

	//These are menu items
	String[] fileItems = { "New", "Open", "Save", "Save as", "Close", "Exit" };
	String[] editItems = { "Cut", "Copy", "Paste", "Undo" };
	String[] viewItems = {};
	String[] runItems = {"Run"};
	String[] runasItems = {"Java"};

	//This lays out which extension maps to which programming language
	private HashMap<String,String> extToLang = new HashMap<String,String>();
	//languages supported by this editor
	private HashSet<String> heldLanguages = new HashSet<String>();
	//listener to detect a language the user chooses, and changes the keywords for that file as appropiate
	private LanguageListener languageListener;
	//listener to compile and run programs within the editor
	private RunasListener runasListener;
	//HashMap containing al the keywords for every language
	//Outer String is the language, inner string is the section of keywords, and HashSet actually contains the keywords
	private HashMap<String,HashMap<String,HashSet<String>>> keywords = new HashMap<String,HashMap<String,HashSet<String>>>();
	//variable to hold copied text
	private String copiedText;
	//variable to keep color when copying text
	private Color copiedColor;
	//editor settings
	private String fontName = "monospaced";
	private int fontSize = 16;
	private int tabSize = 5;
	Font font = new Font(fontName,Font.PLAIN,fontSize);
	//this is the variable to hold the entire style of the editor
	private StyleContext styleContext = new StyleContext();


	//This class listens to the select language menu buttons, and changes active keywords depending on what the user selects
	class LanguageListener implements ActionListener{

		public void actionPerformed(ActionEvent e){

			System.out.println("language listener running");
			
			//chosen language
			JMenuItem language = (JMenuItem)e.getSource();

			Scroller scroller = ((Scroller)panel.getSelectedComponent());

			System.out.println(scroller);

			System.out.println(language.getText());

			if(scroller != null){
				TextAreaPanel temp = scroller.getTextArea();
				temp.setKeywords(keywords.get(language.getText()));
				temp.setKeywordListener();
			}
		}
	}

	//class used to compile and run programs within the editor. Attempting to make this an IDE type thing.
	class RunasListener implements ActionListener{

		public void actionPerformed(ActionEvent e){

			System.out.println("Run as listener running");

			JMenuItem runasLanguage = (JMenuItem)e.getSource();

			try{
				compileAndRun(runasLanguage.getText());
			}
			catch(Exception err){
				err.printStackTrace();
			}
		}

		public void compileAndRun(String lang) throws IOException, InterruptedException{

			//switch statement that actually does the work of compiling and running a program
			//TODO: Add console functionality as well as "projects" to organize code
			switch(lang){

			case "Java":

				Runtime runtime = Runtime.getRuntime();

				Process compile = runtime.exec("javac");

				compile.waitFor();

				Process run = runtime.exec("java");
			}
		}

	}

	//The constructor sets all of the styles for the editor
	public Editor(){

		TabSet tabSet = getTabSet();

		Style standard = styleContext.addStyle("standard", null);
		StyleConstants.setFontFamily(standard, fontName);
		StyleConstants.setFontSize(standard,fontSize);
		StyleConstants.setTabSet(standard, tabSet);
		//Style lineNumbers = styleContext.addStyle("lineNumbers",standard);
		//StyleConstants.setAlignment(lineNumbers,StyleConstants.ALIGN_RIGHT);
		Style keywords = styleContext.addStyle("keywords",standard);
		StyleConstants.setBold(keywords, true);
		Style access=styleContext.addStyle("access",keywords);
		StyleConstants.setForeground(access, new Color(5,91,14));
		Style modifiers=styleContext.addStyle("modifiers",keywords);
		StyleConstants.setForeground(modifiers,new Color(5,91,14));
		Style control=styleContext.addStyle("controlflow",keywords);
		StyleConstants.setForeground(control,new Color(6,10,124));
		Style datatypes=styleContext.addStyle("datatypes",keywords);
		StyleConstants.setForeground(datatypes,new Color(58,2,66));
		Style errors=styleContext.addStyle("errors",keywords);
		StyleConstants.setForeground(errors,new Color(6,10,124));
		Style other=styleContext.addStyle("other",keywords);
		StyleConstants.setForeground(other,new Color(5,91,14));

		languageListener = new LanguageListener();
		//runasListener = new RunasListener

	}

	//This does all of the graphics initialization and setting up the actual environment
	public void runGraphics() throws IOException {


		parseKeywords(new File("keywords.txt"));

		System.out.println(keywords);

		//the outer window
		frame = new JFrame("Text Editor");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		//the outer contentPane
		panel = new JTabbedPane();

		frame.setContentPane(panel);

		//contains all of the menu items
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

		JMenu languageMenu = new EditorMenu("Language");

		for(String lang : keywords.keySet()){

			JMenuItem item = new JMenuItem(lang);
			setKeyMnemonic(item);
			item.addActionListener(languageListener);
			languageMenu.add(item);

		}

		JMenu runMenu = new EditorMenu("Run");

		for(String s : runItems){

			JMenuItem item = new JMenuItem(s);
			setKeyMnemonic(item);
			item.addActionListener(this);
			runMenu.add(item);

		}

		JMenu runasMenu = new EditorMenu("Run As");

		for(String s : runasItems){

			JMenuItem item = new JMenuItem(s);
			setKeyMnemonic(item);
			//item.addActionListener(runasListener);
			runasMenu.add(item);

		}

		runMenu.add(runasMenu);


		// menu = new JMenu("Another Menu");

		menuBar.add(fileMenu);

		menuBar.add(editMenu);

		menuBar.add(languageMenu);

		menuBar.add(runMenu);

		frame.setJMenuBar(menuBar);

		//getting size of screen to determine how large the text editor should be
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

		int width = (int)screenSize.getWidth();

		int height = (int)screenSize.getHeight();


		//setting appropiate editor size in proportion to the screen
		frame.setPreferredSize(new Dimension((int)(height/1.25),(int)(height/1.25)));

		frame.pack();

		int x = width/2-frame.getWidth()/2;
		int y = height/2-frame.getHeight()/2;

		//sets editor location relative to screen
		frame.setLocation(x, y);



		handleNew();

		menuBar.setVisible(true);
		frame.setVisible(true);

	}

	//pulling some bs to customize tab size for the editor
	public TabSet getTabSet(){

		Canvas c = new Canvas();

		//sketchy way of getting font width to calculate relative tab size;
		FontMetrics fm = c.getFontMetrics(font);

		int charWidth = fm.charWidth(' ');

		int tabWidth = charWidth*tabSize;

		//this is how many tabs are allowed per line
		TabStop[] tabStops = new TabStop[100];

		for(int i=0;i<tabStops.length;i++){

			tabStops[i] = new TabStop((i+1)*tabWidth);

		}

		TabSet tabSet = new TabSet(tabStops);

		return tabSet;

	}

	public void parseKeywords(File f) throws IOException{

		BufferedReader reader = new BufferedReader(new FileReader(f));

		StringBuilder builder = new StringBuilder("");
		String next;

		while((next = reader.readLine()) != null){
			builder.append(next).append(System.lineSeparator());
		}

		String text = builder.toString();

		//pattern to find the extension and language within the keywords.txt file
		Pattern p = Pattern.compile("(?<=\\[)\\S+ \\.\\S+(?=\\])");

		ArrayList<String> languages = new ArrayList<String>();

		Matcher m = p.matcher(text);

		//finding each language section
		while(m.find()){
			System.out.println("found");
			String[] language = m.group().split(" ");
			System.out.println(language);
			languages.add(language[0]);
			extToLang.put(language[1].substring(1), language[0]);
			heldLanguages.add(language[1].substring(1));
		}

		String[] sections = text.split("\\s*\\[\\S+ \\.\\S+\\]\\s*");

		System.out.println(Arrays.toString(sections));

		System.out.println("first element : " +sections[0]);


		p=Pattern.compile("--[a-zA-Z]+");

		for(int i =0;i<languages.size();i++){

			String textTemp = sections[i+1].trim();
			String[] keywordSections = textTemp.split("--[a-zA-Z]+");

			m = p.matcher(textTemp);

			System.out.println("textTemp: " + textTemp);
			System.out.println("-----");
			System.out.println(Arrays.toString(keywordSections));

			HashMap<String,HashSet<String>> tempMap = new HashMap<String,HashSet<String>>();
			for(int j=1;j<keywordSections.length;j++){

				String[] keywords = keywordSections[j].split("\\s+");

				System.out.println("keywords: " + Arrays.toString(keywords));


				HashSet<String> temp = new HashSet<String>();
				for(String keyword: keywords){

					temp.add(keyword);
				}
				if(m.find()){
					tempMap.put(m.group().substring(2),temp);
				}
				else{
					throw new IOException("keywords.txt format is incorrect");
				}
			}

			//actually putting language with keywords into HashMap<>
			this.keywords.put(languages.get(i), tempMap);
		}
	}

	//calculates number of lines in a textpane
	public int getLinesTextPane(JTextPane pane){

		int lines = 1;

		//calculates based off number of new lines
		Pattern p = Pattern.compile("\\\n");

		Matcher m = p.matcher(pane.getText());

		while(m.find()){
			lines++;
		}

		return lines;
	}




	//this handles making sure the line numbers are calculated correctly
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

	//this handles creating shortcuts for the menu items
	public void setKeyMnemonic(JMenuItem item) {

		switch (item.getText()) {

		case "Run":

			item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R,ActionEvent.CTRL_MASK));
			break;
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

			item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_V,ActionEvent.CTRL_MASK));
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

	//handles the most important menu items
	public void actionPerformed(ActionEvent e) {

		if (e.getSource() instanceof JMenuItem) {

			JMenuItem source = (JMenuItem) e.getSource();

			switch (source.getText()) {

			case "Run":

				handleRun();

				break;
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

	//handles creating a new file
	public void handleNew() {

		//outer panel of this new tab
		JPanel pane = new JPanel(new BorderLayout());

		//this is the line numbers panel
		JPanel temp = new JPanel();

		temp.setPreferredSize(new Dimension(50,0));

		//System.out.println(temp.getBackground());

		//creating the line numbers here

		LineNumberList lineNumbers = new LineNumberList(font);


		lineNumbers.setMargin(new Insets(-2,0,0,0));


		temp.add(lineNumbers);

		pane.add(temp,BorderLayout.WEST);

		TextAreaPanel textArea = new TextAreaPanel(styleContext);

		textArea.setIsNew(true);

		setLineListener(textArea,lineNumbers);

		pane.add(textArea,BorderLayout.CENTER);

		Scroller tab = new Scroller(pane);

		panel.addTab("new " + (panel.getTabCount() + 1) + ".txt", tab);

		textArea.setFile(new File("new " + (panel.getTabCount() + 1) + ".txt"));

		panel.setSelectedIndex(panel.getTabCount() - 1);

		savedFiles.add(true);

		setSavedListener(textArea, panel.getTabCount() - 1);

	}

	//Doesn't handle opening the file.
	//It sets up which file is chosen and then passes this to the openFile() method
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

	//saves a file
	public void handleSave() {

		if (panel.getTabCount() > 0) {

			TextAreaPanel currentTab = ((Scroller) panel.getSelectedComponent()).getTextArea();

			int selected = panel.getSelectedIndex();

			//checking whether tab is brand new or not
			//If file was opened instead of created this would be false
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
								if(heldLanguages.contains(extension)){
									currentTab.setKeywords(keywords.get(extToLang.get(extension)));
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
							if(heldLanguages.contains(extension)){
								currentTab.setKeywords(keywords.get(extToLang.get(extension)));
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

	//this actually sets the listener to change the values in the savedFiles ArrayList<>
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
							if(heldLanguages.contains(extension)){
								currentTab.setKeywords(keywords.get(extToLang.get(extension)));
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
						if(heldLanguages.contains(extension)){
							currentTab.setKeywords(keywords.get(extToLang.get(extension)));
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

	//handles closing a tab/file
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

	//three functions below probably interfere with their built-in counterparts

	//handles copying text. I think this interferes with the built in ctrl-c function
	public void handleCopy() {

		TextAreaPanel currentTab = ((Scroller) panel.getSelectedComponent()).getTextArea();

		JTextPane textArea = currentTab.getTextArea();

		copiedText = textArea.getSelectedText();

		copiedColor = textArea.getSelectedTextColor();
	}

	//handles cutting text. I think this interferes with the built in ctrl-x function
	public void handleCut() {

		TextAreaPanel currentTab = ((Scroller) panel.getSelectedComponent()).getTextArea();

		JTextPane textArea = currentTab.getTextArea();

		copiedText = textArea.getSelectedText();

		textArea.replaceSelection("");

	}

	//handles pasting text. I think this interferes with the built in ctrl-v function
	public void handlePaste() {

		TextAreaPanel currentTab = ((Scroller) panel.getSelectedComponent()).getTextArea();

		JTextPane textArea = currentTab.getTextArea();

		int selecStart = textArea.getSelectionStart();
		int selecEnd = textArea.getSelectionEnd();

		System.out.println(textArea.getSelectedText());

		if (selecStart == selecEnd) {
			try{
				Document d = textArea.getDocument();
				d.insertString(selecStart, copiedText, null);
				//textArea.setCaretPosition(selecStart+copiedText.length());
			}
			catch(BadLocationException e){
				e.printStackTrace();
			}
		}

		else {
			textArea.replaceSelection(copiedText);
			System.out.println("setting caret position");
			textArea.setCaretPosition(selecStart+copiedText.length());
		}
	}

	//actually handles opening the file.
	public void openFile(File file) {

		//outer panel with text in it
		JPanel pane = new JPanel(new BorderLayout());

		JPanel temp = new JPanel();

		temp.setPreferredSize(new Dimension(50,0));

		//creating line numbers area
		LineNumberList lineNumbers = new LineNumberList(font);


		lineNumbers.setMargin(new Insets(-2,0,0,0));


		temp.add(lineNumbers);

		pane.add(temp,BorderLayout.WEST);

		TextAreaPanel textArea = new TextAreaPanel(styleContext);

		String fileName = file.getName();

		System.out.println(fileName);
		textArea.setFile(file);

		setLineListener(textArea,lineNumbers);

		pane.add(textArea,BorderLayout.CENTER);

		Scroller tab = new Scroller(pane);

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

				if(heldLanguages.contains(extension)){
					textArea.setKeywords(keywords.get(extToLang.get(extension)));
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


	//TODO: May handle running programs. That may be the job of RunasListener Instead.
	public void handleRun(){



	}

}

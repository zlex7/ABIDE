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
import  java.awt.GridBagConstraints;
import  java.awt.GridBagLayout;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.Stack;
import java.util.LinkedList;
import java.util.Iterator;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextPane;
import javax.swing.JButton;
import javax.swing.JLabel;
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
import javax.swing.plaf.*;
import javax.swing.UIManager;
import javax.swing.ImageIcon;
import java.awt.Container;
import javax.swing.BorderFactory;
import javax.swing.border.EmptyBorder;
import javax.swing.JTextPane;
import javax.swing.JLabel;
import javax.swing.JSpinner;
import javax.swing.SpinnerListModel;
import javax.swing.SpringLayout;


import java.lang.Thread;

public class Editor implements ActionListener {


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
	String[] editItems = { "Cut", "Copy", "Paste", "Undo", "Redo" };
	String[] viewItems = {"Find/Replace"};
	String[] runItems = {"Run"};
	String[] runasItems = {"Java"};
	String[] preferenceItems = {"Settings"};

	String[] fontSizes = new String[100];

	//This lays out which extension maps to which programming language
	private HashMap<String,String> extToLang = new HashMap<String,String>();
	//languages supported by this editor
	private HashSet<String> heldLanguages = new HashSet<String>();
	//listener to detect a language the user chooses, and changes the keywords for that file as appropiate
	private LanguageListener languageListener;
	//listener to compile and run programs within the editor
	private RunasListener runasListener;

	private FileOpenListener fileOpenListener;

	private ButtonCloseListener buttonCloseListener;
	//HashMap containing al the keywords for every language
	//Outer String is the language, inner string is the section of keywords, and HashSet actually contains the keywords
	private HashMap<String,HashMap<String,HashSet<String>>> keywords = new HashMap<String,HashMap<String,HashSet<String>>>();
	//variable to hold copied text

	private Stack<String> recentFiles = new Stack<String>();
	private String copiedText;
	//variable to keep color when copying text
	private Color copiedColor;

	private Stack<String[]> pastChanges = new Stack<String[]>();

	//editor settings
	private String fontName = "monospaced";
	private int fontSize = 22;
	private int tabSize = 5;
	Font font = new Font(fontName,Font.PLAIN,fontSize);
	//this is the variable to hold the entire style of the editor
	private StyleContext styleContext = new StyleContext();

	private static Color greenTheme = new Color(84,130,0);
	private static Color blackTheme = new Color(38,38,38);
	private static Color greyTheme = new Color(63, 65, 68);
	private static Color whiteTheme = new Color(211, 211, 211);
	private static Color yellowTheme = new Color(226,244,66);
	private static Color transparentTheme = new Color(1f,0f,0f,0f);
	private static Font fontTheme = new javax.swing.plaf.FontUIResource("Consolas",Font.PLAIN,20);


	private static Color blueWords = new Color(99, 146, 216);
	private static Color pinkWords = new Color(217, 66, 244);
	private static Color greenWords = new Color(23, 137, 53);
	private static Color purpleWords = new Color(87, 50, 168);
	private static Color yellowWords = new Color(28, 188, 0);
	private static Color orangeWords = new Color(229, 161, 36);
	private static Color redWords = new Color(175, 14, 14);
	//This class listens to the select language menu buttons, and changes active keywords depending on what the user selects


	class SettingsListener implements ActionListener{

		public void actionPerformed(ActionEvent e){



		}

	}

	class ButtonCloseListener implements ActionListener{


		public void actionPerformed(ActionEvent e){

			CrossButton source = (CrossButton)e.getSource();

			System.out.println("parent: " + source.getParent().getParent());
			panel.remove(source.getParentTab());

		}
	}
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
				temp.getTextArea().getDocument().removeDocumentListener(temp.getDocListener());
				temp.setKeywords(keywords.get(language.getText()));
				temp.setKeywordListener();
				setLineListener(temp,temp.getLineNumbers());
			}
		}
	}

	//class used to compile and run programs within the editor. Attempting to make this an IDE type thing.
	class RunasListener implements ActionListener{

		public void actionPerformed(ActionEvent e){

			System.out.println("Run as listener running");

			JMenuItem runasLanguage = (JMenuItem)e.getSource();

			try{
				String text = runasLanguage.getText();

				if(text.equals("Run")){

					TextAreaPanel currentArea = ((Scroller)panel.getSelectedComponent()).getTextArea();

					String extension = currentArea.getFileExtension();

					extension = extension.substring(0,1).toUpperCase() + extension.substring(1).toLowerCase();

					compileAndRun(extension);
				}

				else{

					compileAndRun(text);
				}
			}

			catch(Exception err){
				err.printStackTrace();
			}

		}

		public void compileAndRun(String lang) throws IOException, InterruptedException{


			TextAreaPanel textArea = ((Scroller)panel.getSelectedComponent()).getTextArea();

			String filePath = textArea.getFilePath();

			System.out.println("file path: " + filePath);

			filePath = filePath.substring(0,filePath.lastIndexOf("\\"));

			System.out.println("file path: " + filePath);

			String fileName = textArea.getFileName();

			fileName = fileName.substring(0,fileName.indexOf("."));

			System.out.println("file name: " + fileName);

			//switch statement that actually does the work of compiling and running a program
			//TODO: Add console functionality as well as "projects" to organize code
			switch(lang){

			case "Java":

				RunThread runProgram = new RunThread(filePath,fileName);

				runProgram.start();

				break;

			default:

				JOptionPane.showMessageDialog(frame, "That file extension is not supported.");

				break;
			}
		}

	}

	//The constructor sets all of the styles for the editor
	public Editor(){

		//setUIFont(new javax.swing.plaf.FontUIResource("Consolas",Font.PLAIN,20));
		//UIManager.put("Menu.foreground", whiteTheme);
		//UIManager.put("Menu.selectionBackground", greyTheme);
		//UIManager.put("Menu.background", blackTheme);
		//UIManager.put("Menu.border", blackTheme);
		//UIManager.put("Menu.opaque", true);
		UIManager.put("TabbedPane.selected", blackTheme);
		UIManager.put("TabbedPane.border", blackTheme);
	  UIManager.put("TabbedPane.contentBorderInsets", new Insets(0, 0, 0, 0));
		UIManager.put("TabbedPane.contentOpaque", true);
		UIManager.put("TabbedPane.tabInsets", new Insets(0, 15, 20, 15));
		UIManager.put("TabbedPane.shadow", greyTheme);
		UIManager.put("TabbedPane.borderHightlightColor", greyTheme);
	   UIManager.put("TabbedPane.darkShadow", greyTheme);
	 UIManager.put("TabbedPane.light", greyTheme);
	 UIManager.put("TabbedPane.selectHighlight", greyTheme);
	 UIManager.put("TabbedPane.unselectedTabHighlight", greyTheme);
	  UIManager.put("TabbedPane.tabsOverlapBorder", true);
	// UIManager.put("TabbedPane.darkShadow", greyTheme);
	 UIManager.put("TabbedPane.focus", greyTheme);
	 UIManager.put("TabbedPane.font", fontTheme);
		//UIManager.put("MenuBar.background", blackTheme);
		//UIManager.put("MenuBar.foreground", whiteTheme);
		//UIManager.put("MenuBar.border", blackTheme);
		//UIManager.put("MenuItem.background", blackTheme);
		//UIManager.put("MenuItem.foreground", whiteTheme);
	////	UIManager.put("MenuItem.selectionBackground", greyTheme);
	//	UIManager.put("MenuItem.border", blackTheme);
	 UIManager.put("MenuItem.font", new FontUIResource("Arial",Font.PLAIN,18));
	 UIManager.put("Menu.font", new FontUIResource("Arial",Font.PLAIN,18));
	UIManager.put("ScrollPane.border",blackTheme);
	UIManager.put("ScrollBar.thumb", new ColorUIResource(blackTheme));
	UIManager.put("ScrollBar.thumbDarkShadow", new ColorUIResource(blackTheme));
	UIManager.put("ScrollBar.thumbShadow", new ColorUIResource(blackTheme));
	UIManager.put("ScrollBar.thumbHighlight", new ColorUIResource(blackTheme));
	UIManager.put("ScrollBar.thumbHighlight", new ColorUIResource(blackTheme));
	UIManager.put("ScrollBar.trackHighlight", new ColorUIResource(greyTheme));
	UIManager.put("ScrollBar.trackHighlightForeground", new ColorUIResource(greyTheme));

	UIManager.put("ScrollBar.track", new ColorUIResource(greyTheme));
	UIManager.put("ScrollBar.background", new ColorUIResource(greyTheme));
	UIManager.put("ScrollBar.foreground", new ColorUIResource(blackTheme));
	UIManager.put("ScrollBar.border", new ColorUIResource(blackTheme));
	UIManager.put("ScrollBar.thumbForeground", new ColorUIResource(blackTheme));
	//UIManager.put("ScrollBar.opaque",  false);


	UIManager.put("TextPane.background", new ColorUIResource(blackTheme));
	UIManager.put("TextPane.inactiveBackground", new ColorUIResource(blackTheme));
	UIManager.put("TextPane.border", new ColorUIResource(blackTheme));
	//UIManager.put("Panel.background", blackTheme);
	UIManager.put("TextArea.background",new ColorUIResource(greyTheme));


		TabSet tabSet = getTabSet();

		Style standard = styleContext.addStyle("standard", null);
		StyleConstants.setFontFamily(standard, fontName);
		StyleConstants.setFontSize(standard,fontSize);
		StyleConstants.setTabSet(standard, tabSet);
		StyleConstants.setForeground(standard,whiteTheme);
		//Style lineNumbers = styleContext.addStyle("lineNumbers",standard);
		//StyleConstants.setAlignment(lineNumbers,StyleConstants.ALIGN_RIGHT);
		Style keywords = styleContext.addStyle("keywords",standard);
		StyleConstants.setBold(keywords, true);
		Style access = styleContext.addStyle("access",keywords);
		StyleConstants.setForeground(access, blueWords);
		Style modifiers = styleContext.addStyle("modifiers",keywords);
		StyleConstants.setForeground(modifiers,pinkWords);
		Style control = styleContext.addStyle("controlflow",keywords);
		StyleConstants.setForeground(control,greenWords);
		Style datatypes = styleContext.addStyle("datatypes",keywords);
		StyleConstants.setForeground(datatypes,blueWords);
		Style errors = styleContext.addStyle("errors",keywords);
		StyleConstants.setForeground(errors,blueWords);
		Style other = styleContext.addStyle("other",keywords);
		StyleConstants.setForeground(other,blueWords);
		Style strings = styleContext.addStyle("strings",keywords);
		StyleConstants.setForeground(strings,yellowWords);
		Style classes = styleContext.addStyle("classes",keywords);
		StyleConstants.setForeground(classes,orangeWords);
		Style periods = styleContext.addStyle("periods",keywords);
		StyleConstants.setForeground(periods,redWords);
		Style comments = styleContext.addStyle("comments",keywords);
		StyleConstants.setForeground(comments,greyTheme);



		languageListener = new LanguageListener();
		fileOpenListener = new FileOpenListener();
		buttonCloseListener = new ButtonCloseListener();
		runasListener = new RunasListener();

		for(int i =0;i<fontSizes.length;i++){

			fontSizes[i]=Integer.toString(i+1);
		}
	}

	public static void setUIFont (javax.swing.plaf.FontUIResource f){
    java.util.Enumeration keys = UIManager.getDefaults().keys();
    while (keys.hasMoreElements()) {
      Object key = keys.nextElement();
      Object value = UIManager.get (key);
      if (value != null && value instanceof javax.swing.plaf.FontUIResource)
        UIManager.put (key, f);
      }
    }

	//This does all of the graphics initialization and setting up the actual environment
	public void runGraphics() throws IOException {


		parseKeywords(new File("../keywords.txt"));

		System.out.println(keywords);

		parseRecentFiles(new File("../recent.txt"));

		//the outer window
		frame = new JFrame("ABIDE");

		ImageIcon JT = new ImageIcon("../images/JT.png");

		frame.setIconImage(JT.getImage());

		frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

		frame.addWindowListener(new java.awt.event.WindowAdapter() {
   		 	@Override
   		 	public void windowClosing(java.awt.event.WindowEvent windowEvent) {

        		if (JOptionPane.showConfirmDialog(frame,
            	"Are you sure you want to close this window?", "Really Closing?",
            	JOptionPane.YES_NO_OPTION,
            	JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION){

							PrintWriter recentWriter = null;

							try{

								recentWriter = new PrintWriter(new File("../recent.txt"));
							} catch(FileNotFoundException e){

								e.printStackTrace();

								System.out.println("recent.txt was deleted");

							}
							String[] files = new String[recentFiles.size()];

							int i=files.length-1;

							while(!recentFiles.isEmpty()){

								files[i] = recentFiles.pop();
								i--;
							}

							for(i=0;i<files.length;i++){


								if(i!=files.length-1){

									recentWriter.println(files[i]);

								}

								else{

									recentWriter.print(files[i]);

								}
							}

							recentWriter.close();

            	frame.dispose();
           	 	System.exit(0);
       		 	}
    		}
		});

		//the outer contentPane
		panel = new JTabbedPane();

		panel.setOpaque(true);

		panel.setBackground(greyTheme);

		panel.setForeground(whiteTheme);

 	  panel.setBorder(new EmptyBorder(0, 0, 0, 0));

		frame.setContentPane(panel);

		//contains all of the menu items
		JMenuBar menuBar = new JMenuBar();

		JMenu fileMenu = new EditorMenu("<html><p style='margin-top:0px;'>File");

		JMenu openasMenu = new EditorMenu("Open Recent");

	  Iterator<String> it = recentFiles.iterator();

		while (it.hasNext()){
			JMenuItem item = new JMenuItem(it.next());
			item.addActionListener(fileOpenListener);
			openasMenu.add(item);
		}


		for (String s : fileItems) {

			JMenuItem item = new JMenuItem(s);
			setKeyMnemonic(item);
			item.addActionListener(this);
			fileMenu.add(item);

			if(s.equals("Open")){

				fileMenu.add(openasMenu);
			}

		}





		JMenu editMenu = new EditorMenu("<html><p style='margin-top:0px;'>Edit");

		for (String s : editItems) {

			JMenuItem item = new JMenuItem(s);
			setKeyMnemonic(item);
			item.addActionListener(this);
			editMenu.add(item);

		}

		JMenu viewMenu = new EditorMenu("<html><p style='margin-top:0px;'>View");

		for (String s : viewItems) {

			JMenuItem item = new JMenuItem(s);
			setKeyMnemonic(item);
			item.addActionListener(this);
			viewMenu.add(item);

		}

		JMenu languageMenu = new EditorMenu("<html><p style='margin-top:0px;'>Language");

		for(String lang : keywords.keySet()){

			JMenuItem item = new JMenuItem(lang);
			setKeyMnemonic(item);
			item.addActionListener(languageListener);
			languageMenu.add(item);

		}

		JMenu runMenu = new EditorMenu("<html><p style='margin-top:0px;'>Run");

		for(String s : runItems){

			JMenuItem item = new JMenuItem(s);
			setKeyMnemonic(item);
			if(s.equals("Run")){

				item.addActionListener(runasListener);
			}
			runMenu.add(item);

		}

		JMenu runasMenu = new EditorMenu("Run As");



		for(String s : runasItems){

			JMenuItem item = new JMenuItem(s);
			setKeyMnemonic(item);
			item.addActionListener(runasListener);
			runasMenu.add(item);

		}

		JMenu preferenceMenu = new EditorMenu("<html><p style='margin-top:0px;'>Preferences");

		for(String s : preferenceItems){

			JMenuItem item = new JMenuItem(s);
			setKeyMnemonic(item);
			item.addActionListener(this);
			preferenceMenu.add(item);

		}

		runMenu.add(runasMenu);

		// menu = new JMenu("Another Menu");

		menuBar.add(fileMenu);

		menuBar.add(editMenu);

		menuBar.add(viewMenu);

		menuBar.add(languageMenu);

		menuBar.add(runMenu);

		menuBar.add(preferenceMenu);
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

		if(f.exists()){
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
		else{

			f.createNewFile();
		}
	}


	public void parseRecentFiles(File f) throws IOException{

		if(f.exists()){

			BufferedReader reader = new BufferedReader(new FileReader(f));

			String next;

			while((next = reader.readLine()) != null){

					System.out.println("recent file: " + next);
					recentFiles.push(next);

				}
		}

		else{

			f.createNewFile();

		}
	}



	//this handles making sure the line numbers are calculated correctly
	public void setLineListener(TextAreaPanel p, LineNumberList lines){

		JTextPane area = p.getTextArea();

		Document d = area.getDocument();

		DocumentListener dlistener = new DocumentListener() {

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

			 int lineNums = 1;

			//calculates based off number of new lines
			Pattern p = Pattern.compile("\\\n");
			Matcher m  = null;
			try{
				m = p.matcher(d.getText(0,d.getLength()));
			}
			catch(BadLocationException e){
				e.printStackTrace();
			}
			while(m.find()){
				lineNums++;
			}

			//System.out.println("updating number of lines to: " + lineNums);

			lines.updateLineNumbers(lineNums);

			}


		};

		p.setDocListener(dlistener);

		d.addDocumentListener(dlistener);
	}

	class FileOpenListener implements ActionListener{

		public void actionPerformed(ActionEvent e){

 			JMenuItem source = (JMenuItem)e.getSource();

 			String filePath = source.getText();

 			openFile(new File(filePath));


		}

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
		case "Find/Replace":

			item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F,ActionEvent.CTRL_MASK));
			break;
		case "Paste":

			item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_V,ActionEvent.CTRL_MASK));
			break;

		case "Undo":

			item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z,ActionEvent.CTRL_MASK));
			break;
		case "Redo":

			item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Y,ActionEvent.CTRL_MASK));
			break;
		case "Preferences":

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
	public void actionPerformed(ActionEvent e){

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
			case "Undo":

				handleUndo();
				break;
			case "Redo":

				handleRedo();
				break;
			case "Find/Replace":

				handleFind();
				break;
			case "Settings":

				handlePreferences();
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

		pane.setBorder(BorderFactory.createEmptyBorder(0,0,0,0));

		//this is the line numbers panel
		JPanel temp = new JPanel();

		temp.setBackground(blackTheme);

		//temp.setPreferredSize(new Dimension(75,0));

		//System.out.println(temp.getBackground());

		//creating the line numbers here

		LineNumberList lineNumbers = new LineNumberList(font);


		//lineNumbers.setMargin(new Insets(-5,0,0,0));


		temp.add(lineNumbers);

		pane.add(temp,BorderLayout.WEST);

		TextAreaPanel textArea = new TextAreaPanel(styleContext);

		textArea.setIsNew(true);

		try{
			textArea.setKeywordListener();
		}
		catch(Exception e){
			e.printStackTrace();
		}

		textArea.setLineNumbers(lineNumbers);

		setLineListener(textArea,lineNumbers);

		pane.add(textArea,BorderLayout.CENTER);

		Scroller tab = new Scroller(pane);

		String title = "new " + (panel.getTabCount() + 1) + ".txt  ";

		panel.addTab(title, tab);

		int index = panel.getTabCount()-1;

		TitlePanel pnlTab = new TitlePanel();
		pnlTab.setOpaque(false);
		pnlTab.setBorder(BorderFactory.createEmptyBorder(10, 0, -4, 0));
		JLabel lblTitle = new JLabel(title);
		lblTitle.setFont(font);
		lblTitle.setForeground(whiteTheme);

		pnlTab.setLabel(lblTitle);
		//lblTitle.setMargin(new Insets(10,0,0,0));
		CrossButton btnClose = new CrossButton((Scroller)panel.getComponentAt(index));
		btnClose.addActionListener(buttonCloseListener);


		pnlTab.add(lblTitle, BorderLayout.WEST);

		pnlTab.add(btnClose,BorderLayout.EAST);

		panel.setTabComponentAt(index, pnlTab);

		textArea.setFile(new File("new " + (panel.getTabCount() + 1) + ".txt"));

		panel.setSelectedIndex(panel.getTabCount() - 1);

		savedFiles.add(true);

		setSavedListener(textArea);

	}

	//Doesn't handle opening the file.
	//It sets up which file is chosen and then passes this to the openFile() method
	public void handleOpen() {

		JFileChooser fileChooser = new JFileChooser();

		int returned = fileChooser.showOpenDialog(frame);

		if (returned == JFileChooser.APPROVE_OPTION) {

			File chosenFile = fileChooser.getSelectedFile();

			recentFiles.add(chosenFile.getAbsolutePath());
			openFile(chosenFile);
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

					recentFiles.add(file.getAbsolutePath());

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

							}

							try {


								FileWriter writer = new FileWriter(file);

								writer.write(currentTab.getText());

								writer.close();

								savedFiles.set(selected, true);

								setSavedListener(currentTab);
							} catch (IOException err) {
								JOptionPane.showMessageDialog(frame, "File could not be saved");
							}
							currentTab.setIsNew(false);
							if(currentTab.getIsChanged()){

								panel.setTitleAt(panel.getSelectedIndex(),panel.getTitleAt(panel.getSelectedIndex()).substring(1));
							}
							currentTab.setIsChanged(false);
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
						}

						try {
							FileWriter writer = new FileWriter(file);

							writer.write(currentTab.getText());

							writer.close();

							savedFiles.set(selected, true);

							setSavedListener(currentTab);
						} catch (IOException err) {
							JOptionPane.showMessageDialog(frame, "File could not be saved");
						}
					}


					int selectedPane = panel.getSelectedIndex();
					System.out.println("selected: " + selected);

					TitlePanel titlePanel = (TitlePanel)panel.getTabComponentAt(selectedPane);

					JLabel title = titlePanel.getLabel();

					if(title.getText().charAt(0)=='*'){
						System.out.println("removing asterisk");
						title.setText(file.getName()+"  ");

					}

					currentTab.setFile(file);

				}

			}

			else {

				try {

					System.out.println("saving fileName : " + currentTab.getFileName() + "fsdf");

					FileWriter writer = new FileWriter(new File(currentTab.getFilePath()));

					writer.write(currentTab.getText());

					writer.close();

					savedFiles.set(selected, true);

					setSavedListener(currentTab);

				} catch (IOException err) {
					JOptionPane.showMessageDialog(frame, "File could not be saved");
				}

				int selectedPane = panel.getSelectedIndex();
				System.out.println("selected: " + selected);

				TitlePanel titlePanel = (TitlePanel)panel.getTabComponentAt(selectedPane);

				JLabel title = titlePanel.getLabel();

				if(title.getText().charAt(0)=='*'){
					System.out.println("removing asterisk");
					title.setText(currentTab.getFileName()+"  ");

				}

				else{

					title.setText(currentTab.getFileName());
				}


			}

		}

		else {

			JOptionPane.showMessageDialog(frame, "There is no file to be saved");
		}
	}

	//this actually sets the listener to change the values in the savedFiles ArrayList<>
	public void setSavedListener(TextAreaPanel currentTab) {

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

				System.out.println("SAVE LISTENER BEING CALLED");
				currentTab.setIsChanged(true);
				int selected = panel.getSelectedIndex();
				System.out.println("selected: " + selected);

				TitlePanel titlePanel = (TitlePanel)panel.getTabComponentAt(selected);

				JLabel title = titlePanel.getLabel();

				if(! (title.getText().charAt(0)=='*')){
					System.out.println("adding asterisk to title");
					System.out.println("new title is : " + "*"+title.getText());
					title.setText("*"+title.getText());

				}
				//currentTab.getTextArea().getDocument().removeDocumentListener(this);
				savedFiles.set(selected,false);
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

				recentFiles.add(file.getAbsolutePath());


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
						}

						try {


							FileWriter writer = new FileWriter(file);

							writer.write(currentTab.getText());

							writer.close();

							savedFiles.set(selected, true);

							setSavedListener(currentTab);
						} catch (IOException err) {
							JOptionPane.showMessageDialog(frame, "File could not be saved");
						}
						currentTab.setIsNew(false);

						if(currentTab.getIsChanged()){

							//panel.setTitleAt(panel.getSelectedIndex(),panel.getTitleAt(panel.getSelectedIndex()).substring(1));
						}
						currentTab.setIsChanged(false);

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

					}

					try {


						FileWriter writer = new FileWriter(file);

						writer.write(currentTab.getText());

						writer.close();

						savedFiles.set(selected, true);

						Files.deleteIfExists(Paths.get(currentTab.getFilePath()));

						setSavedListener(currentTab);
					} catch (IOException err) {
						JOptionPane.showMessageDialog(frame, "File could not be saved");
					}
				}

				if(currentTab.getIsChanged()){

					//panel.setTitleAt(panel.getSelectedIndex(),panel.getTitleAt(panel.getSelectedIndex()).substring(1));
				}
				currentTab.setIsChanged(false);
				currentTab.setIsNew(false);

			}
		}

		else {

			JOptionPane.showMessageDialog(frame, "There is no file to be saved");
		}

	}

	//handles closing a tab/file
	public void handleClose() {

		handleClose(panel.getSelectedIndex());
	}

	public void handleClose(int selected){


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
				textArea.setCaretPosition(selecStart+copiedText.length());
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

	public void handleUndo() {


				long start = System.currentTimeMillis();


		TextAreaPanel currentText = ((Scroller)panel.getSelectedComponent()).getTextArea();




		try{
			currentText.undo();
		}
		catch(Exception e){

			e.printStackTrace();
		}

		long stop = System.currentTimeMillis();

		System.out.println("handleUndo took : " + (stop-start) + " milliseconds.");
	}

	public void handleRedo() {

			long start = System.currentTimeMillis();


		TextAreaPanel currentText = ((Scroller)panel.getSelectedComponent()).getTextArea();


		try{
			currentText.redo();
		}
		catch(Exception e){
			e.printStackTrace();
		}


		long stop = System.currentTimeMillis();

		System.out.println("handleRedo took : " + (stop-start) + " milliseconds.");
	}




	//actually handles opening the file.
	public void openFile(File file) {


		if(file.exists()){


			//outer panel with text in it
			JPanel pane = new JPanel(new BorderLayout());

			JPanel temp = new JPanel();

			temp.setBackground(blackTheme);

			//temp.setPreferredSize(new Dimension(50,0));

			//creating line numbers area
			LineNumberList lineNumbers = new LineNumberList(font);


			//lineNumbers.setMargin(new Insets(-5,0,0,0));


			temp.add(lineNumbers);

			pane.add(temp,BorderLayout.WEST);

			TextAreaPanel textArea = new TextAreaPanel(styleContext);

			String fileName = file.getName();

			System.out.println(fileName);
			textArea.setFile(file);

			pane.add(textArea,BorderLayout.CENTER);

			Scroller tab = new Scroller(pane);

			panel.addTab(file.getName(), tab);

			int index = panel.getTabCount()-1;

			TitlePanel pnlTab = new TitlePanel();
			pnlTab.setOpaque(false);
			pnlTab.setBorder(BorderFactory.createEmptyBorder(10, 0, -4, 0));
			JLabel lblTitle = new JLabel(file.getName()+"  ");
			lblTitle.setFont(font);
			lblTitle.setForeground(whiteTheme);

			pnlTab.setLabel(lblTitle);
			//lblTitle.setMargin(new Insets(10,0,0,0));
			CrossButton btnClose = new CrossButton((Scroller)panel.getComponentAt(index));
			btnClose.addActionListener(buttonCloseListener);


			pnlTab.add(lblTitle, BorderLayout.WEST);

			pnlTab.add(btnClose,BorderLayout.EAST);

			panel.setTabComponentAt(index, pnlTab);

			BufferedReader reader = null;

			try {
				reader = new BufferedReader(new FileReader(file));
			} catch (FileNotFoundException err) {
				JOptionPane.showMessageDialog(frame, "That file does not exist");
			}

			StringBuilder input = new StringBuilder("");

			String line;

			int lines = 0;

			try {
				while ((line = reader.readLine()) != null) {

					input.append(line).append("\n");
					lines++;

				}
			} catch (IOException err) {
				err.printStackTrace();
			}

			savedFiles.add(true);

			//System.out.println(input.toString());

			textArea.setText(input.toString());

			System.out.println("drawing lines in openfile()");



			if(fileName.contains(".")){

				String extension = fileName.split("\\.")[1];
				System.out.println("extension: " + extension);

				if(heldLanguages.contains(extension)){
					textArea.setKeywords(keywords.get(extToLang.get(extension)));
					lineNumbers.drawLineNumbers(lines);
				}

				else{
					lineNumbers.drawLineNumbers(lines);
				}


				try{
					textArea.setKeywordListener();
				}
				catch(Exception e){
					e.printStackTrace();
				}
			}

			else{
				textArea.setKeywordListener();
				lineNumbers.drawLineNumbers(lines);
			}

			textArea.setLineNumbers(lineNumbers);

			setLineListener(textArea,lineNumbers);

			setSavedListener(textArea);


			panel.setSelectedIndex(panel.getTabCount() - 1);

			JOptionPane.showMessageDialog(frame, "You chose file " + textArea.getFileName());

		}

		else {

			JOptionPane.showMessageDialog(frame, "File " + file.getName() + " does not exist.");

		}
	}


	public void handleFind(){

		TextAreaPanel currentArea = ((Scroller)panel.getSelectedComponent()).getTextArea();

		String sentence = JOptionPane.showInputDialog(frame,"Enter word to find: ",null);

		currentArea.highlight(sentence);

	}


	public void handlePreferences(){

		JPanel pane = new JPanel(new SpringLayout());

		pane.setBorder(BorderFactory.createEmptyBorder(0,0,0,0));


		//initialize font size setting
		JLabel fontSizeLabel = new JLabel("font size: ");

		SpinnerListModel fontSizeModel = new SpinnerListModel(fontSizes);

		JSpinner fontSizeSpinner = new JSpinner(fontSizeModel);

		fontSizeLabel.setLabelFor(fontSizeSpinner);

		pane.add(fontSizeSpinner);


		//initialize tab size setting
		JLabel tabSizeLabel = new JLabel("tab size: ");

		SpinnerListModel tabSizeModel = new SpinnerListModel(fontSizes);

		JSpinner tabSizeSpinner = new JSpinner(tabSizeModel);

		tabSizeLabel.setLabelFor(tabSizeSpinner);

		pane.add(tabSizeSpinner);


		int numPairs = 2;

		SpringUtilities.makeCompactGrid(pane,
                                        numPairs, 2, //rows, cols
                                        10, 10,        //initX, initY
                                        6, 10);       //xPad, yPad

		Scroller tab = new Scroller(pane);

		String title = "settings.styles";

		panel.addTab(title, tab);

		int index = panel.getTabCount()-1;

		panel.setSelectedIndex(index);

		/*

		JFrame preferences = new JFrame("Preferences");

		SettingsPanel main = new SettingsPanel();

		preferences.setContentPane(main);

		preferences.setSize(new Dimension(300,300));

		preferences.setVisible(true);

		*/

	}
	//TODO: May handle running programs. That may be the job of RunasListener Instead.
	public void handleRun(){



	}

}

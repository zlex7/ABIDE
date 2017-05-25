import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

public class Editor implements ActionListener {

	// TextAreaPanel currentTab;

	ArrayList<Boolean> savedFiles = new ArrayList<Boolean>();
	JFrame frame;
	JTabbedPane panel;
	File currentFile;
	String[] fileItems = { "New", "Open", "Save", "Save as", "Close", "Exit" };
	String[] editItems = { "Cut", "Copy", "Paste" };
	String[] viewItems = {};
	private String copiedText;

	public void runGraphics() {

		frame = new JFrame("Text Editor");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		panel = new JTabbedPane();
		

		frame.setContentPane(panel);

		JMenuBar menuBar = new JMenuBar();

		JMenu fileMenu = new EditorMenu("File");

		for (String s : fileItems) {

			JMenuItem item = new JMenuItem(s);
			item.addActionListener(this);
			fileMenu.add(item);

		}

		JMenu editMenu = new EditorMenu("Edit");

		for (String s : editItems) {

			JMenuItem item = new JMenuItem(s);
			item.addActionListener(this);
			editMenu.add(item);

		}

		JMenu viewMenu = new EditorMenu("Edit");

		for (String s : viewItems) {

			JMenuItem item = new JMenuItem(s);
			item.addActionListener(this);
			viewMenu.add(item);

		}

		// menu = new JMenu("Another Menu");

		menuBar.add(fileMenu);

		menuBar.add(editMenu);

		frame.setJMenuBar(menuBar);

		// frame.setSize(new Dimension(400,400));

		frame.setPreferredSize(new Dimension(1000, 750));
		frame.pack();

		menuBar.setVisible(true);
		frame.setVisible(true);

		// a submenu

	}

	public void actionPerformed(ActionEvent e) {

		if (e.getSource() instanceof JMenuItem) {

			JMenuItem source = (JMenuItem) e.getSource();

			switch (source.getText()) {

			case "New":

				try {
					handleNew();
				} catch (IOException err) {
					err.printStackTrace();
				}
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

	public void handleNew() throws IOException {

		TextAreaPanel textArea = new TextAreaPanel();

		Scroller tab = new Scroller(textArea);
		
		tab.getVerticalScrollBar().setUnitIncrement(25);

		panel.addTab("new " + (panel.getTabCount() + 1) + ".txt", tab);

		panel.setSelectedIndex(panel.getTabCount()-1);
		
		savedFiles.add(true);
		
		setSavedListener(textArea, panel.getTabCount()-1);

	}

	public void handleOpen() {

		JFileChooser fileChooser = new JFileChooser();

		int returned = fileChooser.showOpenDialog(frame);

		if (returned == JFileChooser.APPROVE_OPTION) {

			currentFile = fileChooser.getSelectedFile();

			openFile(currentFile);
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

			if (currentFile == null) {

				JFileChooser fileChooser = new JFileChooser();

				int returned = fileChooser.showSaveDialog(frame);

				if (returned == JFileChooser.APPROVE_OPTION) {

					File file = fileChooser.getSelectedFile();

					if (file.exists()) {

						returned = JOptionPane.showConfirmDialog(frame,
								"Do you wish to overwrite file " + file.getName(), "Overwrite File",
								JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);

						if (returned == JOptionPane.YES_OPTION) {
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
			}

			else {

				try {
					FileWriter writer = new FileWriter(currentFile);

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

					try {

						FileWriter writer = new FileWriter(file);

						writer.write(currentTab.getText());

						writer.close();

						savedFiles.set(selected, true);

						Files.deleteIfExists(Paths.get(currentFile.getAbsolutePath()));
						
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
			int returned = JOptionPane.showConfirmDialog(frame, "Do you want to save file " + tab.getName(),
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

		JTextArea textArea = currentTab.getTextArea();

		copiedText = textArea.getSelectedText();

		textArea.replaceRange("", textArea.getSelectionStart(), textArea.getSelectionEnd());

	}

	public void handlePaste() {

		TextAreaPanel currentTab = ((Scroller) panel.getSelectedComponent()).getTextArea();

		JTextArea textArea = currentTab.getTextArea();

		int selecStart = textArea.getSelectionStart();
		int selecEnd = textArea.getSelectionEnd();

		System.out.println(textArea.getSelectedText());

		if (selecStart == selecEnd) {
			textArea.insert(copiedText, selecStart);
		}

		else {
			textArea.replaceRange(copiedText, selecStart, selecEnd);
		}
	}

	public void openFile(File file) {

		TextAreaPanel textArea = new TextAreaPanel();

		Scroller tab = new Scroller(textArea);
		
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
			System.out.println(input.toString());
			textArea.setText(input.toString());

			setSavedListener(textArea, panel.getTabCount()-1);
			
			panel.setSelectedIndex(panel.getTabCount()-1);
			
			JOptionPane.showMessageDialog(frame, "You chose file " + currentFile.getName());

		}

		else {

			JOptionPane.showMessageDialog(frame, "File " + currentFile.getName() + " does not exist.");

		}
	}

}

import java.awt.Dimension;
import java.awt.TextArea;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;

public class Editor implements ActionListener {

	JFrame frame;
	TextAreaPanel panel;
	File currentFile;
	String[] fileItems = { "New", "Open", "Save", "Save as" };
	String[] editItems = { "Cut", "Copy", "Paste" };
	private String copiedText;
	public void runGraphics() {

		frame = new JFrame("Text Editor");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		panel = new TextAreaPanel();

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
		
		if(e.getSource() instanceof JMenuItem){
			
			JMenuItem source = (JMenuItem)e.getSource();
		
		switch(source.getText()){
		
		case "New":
			
			try{
				handleNew();
			}catch(IOException err){
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
		}
		
		}
		
		
	}
	
	public void handleNew() throws IOException{
		JFileChooser fileChooser = new JFileChooser();
		
		int returned = fileChooser.showDialog(frame, "Create");
		
		if(returned == JFileChooser.APPROVE_OPTION){
			
			currentFile = fileChooser.getSelectedFile();
			
			//int last = currentFile.getAbsolutePath().lastIndexOf('.');
			//String extension = currentFile.getAbsolutePath().substring(last);
			
			//File newFile = new File(currentFile.getName());
			PrintWriter writer = new PrintWriter(new FileWriter(currentFile));
			
			
			writer.print("");
			
			writer.close();
		}
	}
	
	public void handleOpen(){
		JFileChooser fileChooser = new JFileChooser();
		
		int returned = fileChooser.showOpenDialog(frame);
		
		if(returned == JFileChooser.APPROVE_OPTION){
			
			currentFile = fileChooser.getSelectedFile();
			
			
			if(currentFile.exists()){
				BufferedReader reader = null;
			
				try{
					reader = new BufferedReader(new FileReader(currentFile));
				}
				catch(FileNotFoundException err){
					JOptionPane.showMessageDialog(frame, "That file does not exist");
				}
			
				StringBuilder input = new StringBuilder("");
			
				String line;
				
				try{
					while((line = reader.readLine()) != null){
			
						input.append(line).append("\n");
			
					}
				}
				catch(IOException err){
					err.printStackTrace();
				}
			
				panel.setText(input.toString());
			
				JOptionPane.showMessageDialog(frame, "You chose file " + currentFile.getName());
				
			}
			
			else{
				
				JOptionPane.showMessageDialog(frame, "File " + currentFile.getName() + " does not exist.");
				
			}
		}
		
		else if(returned == JFileChooser.CANCEL_OPTION){
			
			
		}
		
		else{
			
			JOptionPane.showMessageDialog(frame, "Invalid file choice.");
			
		}
	}
	
	public void handleSave(){
		
		if(currentFile == null){
			
			JFileChooser fileChooser = new JFileChooser();
			
			int returned = fileChooser.showSaveDialog(frame);
			
			if(returned == JFileChooser.APPROVE_OPTION){
				
				File file = fileChooser.getSelectedFile();
				
				if(file.exists()){
					
					returned = JOptionPane.showConfirmDialog(frame, "Do you wish to overwrite file " + file.getName(),"Overwrite File",JOptionPane.YES_NO_CANCEL_OPTION,JOptionPane.QUESTION_MESSAGE);
					
					if(returned==JOptionPane.YES_OPTION){
						try{
							FileWriter writer = new FileWriter(file);
						
							writer.write(panel.getText());
						
							writer.close();
						}
						catch(IOException err){
							JOptionPane.showMessageDialog(frame,"File could not be saved");
						}
					}
					
					else if(returned == JOptionPane.NO_OPTION){
						
					}
				}
				
				else{
					
					try{
						FileWriter writer = new FileWriter(file);
					
						writer.write(panel.getText());
					
						writer.close();
					}
					catch(IOException err){
						JOptionPane.showMessageDialog(frame,"File could not be saved");
					}
				}
				
				
				
			}
		}
		
		else{
			
			try{
				FileWriter writer = new FileWriter(currentFile);
			
				writer.write(panel.getText());
			
				writer.close();
			}
			catch(IOException err){
				JOptionPane.showMessageDialog(frame,"File could not be saved");
			}
			
		}
	}
	
	public void handleCopy(){
		
		copiedText = panel.getTextArea().getSelectedText();
		
	}
	
	public void handleCut(){
		
		TextArea textArea = panel.getTextArea();
		
		copiedText = textArea.getSelectedText();
		
		textArea.replaceRange("", textArea.getSelectionStart(), textArea.getSelectionEnd());
		
		
	}
	public void handlePaste(){
		
		TextArea textArea = panel.getTextArea();
		
		int selecStart = textArea.getSelectionStart();
		int selecEnd = textArea.getSelectionEnd();
		
		System.out.println(textArea.getSelectedText());
		
		if(selecStart==selecEnd){
			textArea.insert(copiedText, selecStart);
		}
		
		else{
			textArea.replaceRange(copiedText, selecStart, selecEnd);
		}
	}
}

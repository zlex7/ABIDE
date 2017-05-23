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

	public void actionPerformed(ActionEvent e){
		
		if(e.getSource() instanceof JMenuItem){
			
			JFileChooser fileChooser;
			int returned;
			JMenuItem source = (JMenuItem)e.getSource();
		
		switch(source.getText()){
		
		case "New":
			
			fileChooser = new JFileChooser();
			
			returned = fileChooser.showDialog(frame, "Create");
			
			if(returned == JFileChooser.APPROVE_OPTION){
				
				currentFile = fileChooser.getSelectedFile();
				
				currentFile = new File(currentFile.getName());
			}
		case "Open":
			
			fileChooser = new JFileChooser();
			
			returned = fileChooser.showOpenDialog(frame);
			
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
			
			break;
		case "Save":
			
			if(currentFile == null){
				
				fileChooser = new JFileChooser();
				
				returned = fileChooser.showSaveDialog(frame);
				
				if(returned == JFileChooser.APPROVE_OPTION){
					
					File file = fileChooser.getSelectedFile();
					
					if(file.exists()){
						
						JOptionPane.showConfirmDialog(frame, "Do you wish to overwrite file " + file.getName());
						
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
			
			break;
		case "Save as":
			
			break;
		case "Cut":
			
			break;
		case "Copy":
			
			break;
		case "Paste":
			
			break;
		}
		
		}
		
		
	}
}

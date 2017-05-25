import java.awt.BorderLayout;
import java.awt.Graphics;
import java.awt.TextArea;

import javax.swing.JPanel;
import javax.swing.JTextArea;

public class TextAreaPanel extends JPanel{
	
	JTextArea textArea = new JTextArea();
	
	public TextAreaPanel(){
		
		setLayout(new BorderLayout());
		add(textArea);
		
	}
	
	public void setText(String input){
		
		textArea.setText(input);
		
	}
	
	public String getText(){
		
		return textArea.getText();
		
	}
	
	public JTextArea getTextArea(){
		
		return textArea;
		
	}
	
	public void paintComponent(Graphics g){
		
		super.paintComponent(g);
			
	}

}

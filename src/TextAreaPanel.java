import java.awt.BorderLayout;
import java.awt.Graphics;
import java.awt.TextArea;

import javax.swing.JPanel;

public class TextAreaPanel extends JPanel{
	
	TextArea textArea = new TextArea();
	
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
	
	public TextArea getTextArea(){
		
		return textArea;
		
	}
	
	public void paintComponent(Graphics g){
		
		super.paintComponent(g);
			
	}

}

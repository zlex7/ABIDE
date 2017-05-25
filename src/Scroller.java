import java.awt.Component;

import javax.swing.JScrollPane;

public class Scroller extends JScrollPane{
	
	private TextAreaPanel textArea;
	public Scroller(TextAreaPanel textArea){
		
		setViewportView(textArea);
		
		this.textArea = textArea;
		
	}
	
	public void setViewportView(Component c){
		
		super.setViewportView(c);
		
		this.textArea = (TextAreaPanel)c;
	}
	public TextAreaPanel getTextArea(){
		
		return textArea;
		
	}

}

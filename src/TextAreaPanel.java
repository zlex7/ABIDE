import java.awt.BorderLayout;
import java.awt.TextArea;

import javax.swing.JPanel;

public class TextAreaPanel extends JPanel{
	
	TextArea textArea = new TextArea();
	
	public TextAreaPanel(){
		
		setLayout(new BorderLayout());
		add(textArea);
		
	}

}

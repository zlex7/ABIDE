import java.awt.Component;
import java.awt.Color;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;

public class Scroller extends JScrollPane{

	TextAreaPanel textArea;

	public Scroller(JPanel panel){

		setViewportView(panel);

		textArea = (TextAreaPanel)panel.getComponent(1);

		getVerticalScrollBar().setUnitIncrement(10);

		setOpaque(true);

		getVerticalScrollBar().setForeground(Color.BLACK);

		getVerticalScrollBar().setBackground(Color.BLACK);
	}

	public TextAreaPanel getTextArea(){

		return textArea;

	}

	public void setViewportView(TextAreaPanel c){

		super.setViewportView(c);

	}

	public void setViewportView(Component c){

		super.setViewportView(c);

	}


}

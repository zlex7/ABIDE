import java.awt.Color;
import java.awt.Dimension;

import javax.swing.JTextArea;

public class LineNumberList extends JTextArea{

	private JTextArea area;
	public LineNumberList() {
		
		
		//setPreferredSize(new Dimension(50,0));
		
		setBackground(new Color(238,238,238));
		
		setForeground(new Color(138,138,92));
		
		setEditable(false);
		
		drawLineNumbers(getLineCount());
		
		System.out.println(getLineCount());
		
		//setText("fdsjfidfjsdkjlfjdslfjdsfsdd\nfdfs]\nf\ndf\ndfs\nfds");

	}
	
	public void drawLineNumbers(int lines){
		
		StringBuilder lineNumbers = new StringBuilder("");
		
		for(int i=1; i<=lines;i++){
			
			lineNumbers.append(i).append("\n");
			
		}
		
		setText(lineNumbers.toString());
	}
	
	
}

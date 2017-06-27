import java.awt.Color;
import java.awt.Font;
import java.awt.ComponentOrientation;
import javax.swing.JTextArea;


//this class is what draws the line numbers on the left side of the editor
public class LineNumberList extends JTextArea{

	public LineNumberList(Font font) {
		
		
		//setPreferredSize(new Dimension(50,0));

		//setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);


		//setting a bunch of style stuff.
		//font is same as main editor font
		setFont(font);
		
		setBackground(new Color(238,238,238));
		
		setForeground(new Color(138,138,92));
		
		setEditable(false);
		
		drawLineNumbers(getLineCount());
		
		System.out.println(getLineCount());

	}
	
	//draws the line numbers based off of an input
	//in this case the input is just getLineCount(), which conveniently returns how many lines there are in a textarea
	public void drawLineNumbers(int lines){
		
		StringBuilder lineNumbers = new StringBuilder("");
		
		for(int i=1; i<=lines;i++){
			

			lineNumbers.append(i).append("\n");
			
		}
		
		setText(lineNumbers.toString());
	}
	
	
}

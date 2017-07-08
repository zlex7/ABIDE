import java.awt.Color;
import java.awt.Font;
import java.awt.ComponentOrientation;
import javax.swing.JTextArea;
import javax.swing.text.DefaultCaret;
import java.awt.Insets;

//this class is what draws the line numbers on the left side of the editor
public class LineNumberList extends JTextArea{


	private Color blackTheme = new Color(38,38,38);
	private Color greyTheme = new Color(128, 129, 135);
	private Color whiteTheme = new Color(239,237,230);

	public LineNumberList(Font font) {


		//setPreferredSize(new Dimension(50,0));

		//setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);


		//setting a bunch of style stuff.
		//font is same as main editor font
		setFont(font);

	//	setBackground(new Color(238,238,238));

//		setForeground(new Color(138,138,92));

		setEditable(false);

		drawLineNumbers(getLineCount());

		System.out.println(getLineCount());

		setOpaque(true);

		setBackground(blackTheme);

		setForeground(whiteTheme);

		setMargin(new Insets(-5,10,10,0));

		DefaultCaret caret = (DefaultCaret)getCaret();

		caret.setUpdatePolicy(DefaultCaret.NEVER_UPDATE);

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

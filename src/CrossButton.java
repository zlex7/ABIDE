import javax.swing.JButton;
import javax.swing.BorderFactory;
import javax.swing.border.EmptyBorder;
import java.awt.event.MouseAdapter;
import java.awt.BorderLayout;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.event.MouseEvent;
import java.awt.Graphics;

public class CrossButton extends JButton{
	
	private static Color greyTheme = new Color(128, 129, 135);
	private static Color blackTheme = new Color(38,38,38);
	private static Color whiteTheme = new Color(239,237,230);

	private String fontName = "monospaced";
	private int fontSize = 26;
	private int tabSize = 5;
	Font font = new Font(fontName,Font.BOLD,fontSize);


	Scroller parentTab;

	public CrossButton(Scroller parentTab){

		this.parentTab = parentTab;
		setFocusPainted(false);
		setContentAreaFilled(false);
		setText("X");
		setFont(font);
		setOpaque(false);
		setForeground(greyTheme);
		setBackground(blackTheme);
		setBorder(BorderFactory.createEmptyBorder());

		addMouseListener( 

			new MouseAdapter(){

				public void mouseEntered(MouseEvent evt){

					setForeground(whiteTheme);
				}

				public void mouseExited(MouseEvent evt){

					setForeground(greyTheme);
				}
		});

	}


	public Scroller getParentTab(){

		return parentTab;
	}

	@Override
    public void paintComponent(Graphics g) {

    super.paintComponent(g);

    if (isSelected()) {
        setBorder(BorderFactory.createEmptyBorder());
    }

    }

}
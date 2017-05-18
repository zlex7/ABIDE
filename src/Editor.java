import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;

public class Editor {

	
	public void runGraphics(){
		
		
		JFrame frame = new JFrame("Text Editor");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		JPanel panel = new TextAreaPanel();
		
		
		JMenuBar menuBar = new JMenuBar();	
		

		
		JMenu menu = new EditorMenu();
		
		menuBar.add(menu);
		
		frame.setJMenuBar(menuBar);
		
		
		
		frame.setContentPane(panel);
		//frame.setSize(new Dimension(400,400));
		frame.pack();
		
		menuBar.setVisible(true);
		frame.setVisible(true);
		
	}
	
}

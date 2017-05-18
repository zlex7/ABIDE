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
		
		frame.setJMenuBar(menuBar);
		
		JMenu menu = new JMenu();
		
		JMenuItem item = new JMenuItem("File");
		JMenuItem item2 = new JMenuItem("Edit");
		JMenuItem item3 = new JMenuItem("View");
		
		item.setName("EREJRWJEKLRJEWLJR");
		
		menu.add(item);
		menu.add(item2);
		menu.add(item3);
		
		menuBar.add(menu);
		
		
		
		frame.setContentPane(panel);
		//frame.setSize(new Dimension(400,400));
		frame.pack();
		frame.setVisible(true);
		
	}
	
}

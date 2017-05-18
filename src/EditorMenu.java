import javax.swing.JMenu;
import javax.swing.JMenuItem;

public class EditorMenu extends JMenu {
	
	public EditorMenu(){
		
		JMenuItem item = new JMenuItem("File");
		JMenuItem item2 = new JMenuItem("Edit");
		JMenuItem item3 = new JMenuItem("View");
		
		add(item);
		add(item2);
		add(item3);
		
	}
	
}
